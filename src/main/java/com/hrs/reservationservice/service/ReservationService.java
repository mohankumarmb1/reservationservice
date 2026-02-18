package com.hrs.reservationservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrs.reservationservice.dto.Hotel;
import com.hrs.reservationservice.dto.Notification;
import com.hrs.reservationservice.dto.Payment;
import com.hrs.reservationservice.enums.PaymentStatus;
import com.hrs.reservationservice.enums.PaymentType;
import com.hrs.reservationservice.enums.ReservationStatus;
import com.hrs.reservationservice.exceptions.HotelNotFoundException;
import com.hrs.reservationservice.exceptions.ReservationAlreadyExistException;
import com.hrs.reservationservice.exceptions.ReservationNotFoundException;
import com.hrs.reservationservice.feign.HotelFeign;
import com.hrs.reservationservice.feign.PaymentFeign;
import com.hrs.reservationservice.model.Reservation;
import com.hrs.reservationservice.proxy.IReservationService;
import com.hrs.reservationservice.repository.ReservationRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ReservationService implements IReservationService
{
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HotelFeign hotelFeign;

    @Autowired
    private PaymentFeign paymentFeign;

    @Autowired
    private KafkaTemplate<String,Object> template;

    Logger logger= LoggerFactory.getLogger(ReservationService.class);


    public Reservation reserve(Reservation reservation)
    {
        if(reservationRepository.findByUserId(reservation.getUserId()).stream().anyMatch(reserve -> reserve.getReservationDate() == reservation.getReservationDate()))
        {
            throw new ReservationAlreadyExistException("Reservation already exists for given date : "+reservation.getReservationDate());
        }
        else
        {
            logger.info("calling external service hotel-management to check room availability");
            Hotel hotel=null;

            try
            {
                hotel= hotelFeign.getHotel(reservation.getHotelId()).getBody();
            }
            catch(HotelNotFoundException e)
            {
                logger.error("Unable to fetch the Hotel for given hotel ID");
                throw new HotelNotFoundException("Hotel not found");
            }
            catch(RuntimeException e)
            {
                logger.error("external call failed : "+e.getMessage());
                logger.error(e.toString());

                return null;
            }

            if(hotel!=null && hotel.getAvailableRoom() > 0)
            {
                try
                {
                    logger.info("calling external service hotel-management to allocate room");
                    hotel.setAvailableRoom(hotel.getAvailableRoom()-1);
                    ResponseEntity<Hotel> updatedResponse=hotelFeign.updateHotel(hotel);
                    if(!updatedResponse.getStatusCode().is2xxSuccessful())
                    {
                        throw new RuntimeException("Unable to allocated hotel room");
                    }

                    try
                    {
                        logger.info("Calling payment service");
                        Payment payment=new Payment();
                        payment.setReservationId(reservation.getId());
                        payment.setPaymentType(PaymentType.PAYMENT);
                        payment.setStatus(PaymentStatus.INITIATED);
                        payment.setAmount(hotel.getPrice());
                        ResponseEntity<Payment> paymentResponse=paymentFeign.pay(payment);

                        if(!paymentResponse.getStatusCode().is2xxSuccessful())
                        {
                            logger.error("Unable to process external payment call");
                            throw new RuntimeException("Unable to process external payment call");
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("external call failed : "+e.getMessage());
                        throw new RuntimeException("external call failed : "+e.getMessage());
                    }

                    logger.info("Payment initiated");
                    Reservation newReservation= reservationRepository.save(reservation);
                    System.out.println(newReservation);

                    return newReservation;
                }
                catch(HotelNotFoundException e)
                {
                    logger.error("Unable to fetch the Hotel for given hotel ID");
                     throw new HotelNotFoundException("Unable to fetch the Hotel for given hotel ID");
                }
                catch(RuntimeException e)
                {
                    logger.error("external call failed : "+e.getMessage());
                    throw new RuntimeException("external call failed : "+e.getMessage());
                }
            }


        }
         throw new RuntimeException("Unable to allocated hotel room");
    }

    public Reservation getReservation(Long id) throws ReservationNotFoundException {
        return reservationRepository.findById(id).stream().findAny().orElseThrow(ReservationNotFoundException::new);
    }


    public List<Reservation> getAllReservation()
    {
        return reservationRepository.findAll();
    }


    public Reservation updateReservation(Reservation reservation) throws ReservationNotFoundException
    {
        if(reservationRepository.existsById(reservation.getId()))
        {
            try
            {
                return reservationRepository.save(reservation);
            }
            catch(Exception e)
            {
                return null;
            }
        }
        else
        {
            throw new ReservationNotFoundException("Reservation not found");
        }

    }

    @Override
    public Reservation cancelReservation(Long id) throws ReservationNotFoundException
    {
        if(!reservationRepository.existsById(id))
        {
            throw new ReservationNotFoundException("Reservation not found");
        }
        else
        {

            try
            {
                Reservation reservation=reservationRepository.findById(id).get();

                logger.info("calling external service hotel-management to get the hotel details");
                Hotel hotel=null;

                try
                {
                    hotel= hotelFeign.getHotel(reservation.getHotelId()).getBody();
                }
                catch(HotelNotFoundException e)
                {
                    logger.error("Unable to fetch the Hotel for given hotel ID");
                    throw new HotelNotFoundException("Hotel not found");
                }
                catch(RuntimeException e)
                {
                    logger.error("external call failed : "+e.getMessage());
                    logger.error(e.toString());

                    throw  new RuntimeException("external call failed : "+e.getMessage());
                }

                if(hotel!=null)
                {
                    logger.info("Calling payment service for refund");
                    Payment payment=new Payment();
                    payment.setReservationId(id);
                    payment.setPaymentType(PaymentType.REFUND);
                    payment.setStatus(PaymentStatus.INITIATED);
                    payment.setAmount(hotel.getPrice());
                    ResponseEntity<Payment> paymentResponse=paymentFeign.pay(payment);

                    if(!paymentResponse.getStatusCode().is2xxSuccessful())
                    {
                        logger.error("Unable to process external payment refund call");
                        throw new RuntimeException("Unable to process external payment refund call");
                    }
                }
            }
            catch (Exception e)
            {
                logger.error("external call failed : "+e.getMessage());
                throw new RuntimeException("external call failed : "+e.getMessage());
            }
            throw new RuntimeException("Something went wrong");
        }
    }


    public boolean deleteReservation(Long id) throws ReservationNotFoundException
    {
        if(reservationRepository.existsById(id))
        {
            try
            {
                reservationRepository.deleteById(id);
                return true;
            }
            catch(Exception e)
            {
                return false;
            }
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Reservation not found", new ReservationNotFoundException("Reservation not found"));
        }

    }


    @KafkaListener(topics = "HotelPaymentTopic",groupId = "br-group")
    public void paymentStatusEvent(ConsumerRecord paymentKafkaMessage) throws JsonProcessingException {
        Payment paymentMessage=new ObjectMapper().readValue(paymentKafkaMessage.value().toString(),Payment.class);
        logger.info("Message consumed : "+paymentMessage);
        Reservation reservation=reservationRepository.findById(paymentMessage.getReservationId()).get();
        if(paymentMessage.getStatus().equals(PaymentStatus.COMPLETED))
        {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            updateReservation(reservation);
            logger.info("Updated reservation : CONFIRMED");
            this.notify(reservation.getUserId(),"Reservation confirmed on successful payment of Rs."+paymentMessage.getAmount());
        }
        else
        {
            reservation.setStatus(ReservationStatus.CANCELLED);
            logger.info("calling external service hotel-management to update room availability");
            Hotel hotel= hotelFeign.getHotel(reservation.getHotelId()).getBody();

            if(hotel!=null)
            {
                hotel.setAvailableRoom(hotel.getAvailableRoom()+1);
            }

            ResponseEntity<Hotel> updatedResponse=hotelFeign.updateHotel(hotel);
            if(!updatedResponse.getStatusCode().is2xxSuccessful())
            {
                throw new RuntimeException("Unable to update hotel room availability");
            }

            updateReservation(reservation);
            logger.info("Updated reservation : CANCELLED");
            this.notify(reservation.getUserId(),"Reservation cancelled successfully");

        }
    }

    private void notify(Long userId, String message)
    {
        logger.info("sending notification via Kafka");
        Notification notification=new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setTimestamp(new Date());

        try
        {
            ObjectMapper mapper=new ObjectMapper();
            CompletableFuture<SendResult<String,Object>> response= template.send("HotelNotificationTopic",mapper.writeValueAsString(notification));
            response.whenComplete((result,ex)->{
                if (ex == null) {
                    logger.info("Sent message with offset=[" + result.getRecordMetadata().offset() + "]");
                } else {
                    logger.info("Unable to send message due to : " + ex.getMessage());
                }
            });
        }
        catch(Exception e)
        {
            logger.error("Unable to push notification to Kafka");
            throw new RuntimeException(e.getMessage());
        }
    }
}
