package com.hrs.reservationservice.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.hrs.reservationservice.models.PaymentDto;
import com.hrs.reservationservice.models.PaymentStatus;
import com.hrs.reservationservice.models.ReservationDto;
import com.hrs.reservationservice.models.ReservationStatus;
import com.hrs.reservationservice.services.ReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopicListener {

	@Value("${consumer.config.payment.topic.name}")
	private String paymentTopicName;

	@Value("${consumer.config.refund.topic.name}")
	private String refundTopicName;

	@Autowired
	private ReservationService reservationService;

	@KafkaListener(id = "${consumer.config.payment.topic.name}", topics = "${consumer.config.payment.topic.name}", groupId = "${consumer.config.group-id}")
	public void consumePayment(ConsumerRecord<String, PaymentDto> payload) {
		log.info("Topic : {}", paymentTopicName);
		log.info("Key : {}", payload.key());
		log.info("Headers : {}", payload.headers());
		log.info("Partion : {}", payload.partition());
		log.info("Payment : {}", payload.value());

		PaymentDto paymentDto = payload.value();

		ReservationDto reservation = reservationService.getReservationById(paymentDto.getReservationId());
		reservation.setPaymentId(paymentDto.getId());

		if (paymentDto.getStatus().equals(PaymentStatus.SUCCESS))
			reservation.setStatus(ReservationStatus.BOOKED);
		else if (paymentDto.getStatus().equals(PaymentStatus.FAILED))
			reservation.setStatus(ReservationStatus.RESERVED);
		else if (paymentDto.getStatus().equals(PaymentStatus.PENDING))
			reservation.setStatus(ReservationStatus.RESERVED);

		reservationService.updateReservation(reservation.getId(), reservation);

	}

	@KafkaListener(id = "${consumer.config.refund.topic.name}", topics = "${consumer.config.refund.topic.name}", groupId = "${consumer.config.group-id}")
	public void consumeRefund(ConsumerRecord<String, PaymentDto> payload) {
		log.info("Topic : {}", refundTopicName);
		log.info("Key : {}", payload.key());
		log.info("Headers : {}", payload.headers());
		log.info("Partion : {}", payload.partition());
		log.info("Payment : {}", payload.value());

		PaymentDto paymentDto = payload.value();

		ReservationDto reservation = reservationService.getReservationById(paymentDto.getReservationId());

		if (paymentDto.getStatus().equals(PaymentStatus.SUCCESS))
			reservation.setStatus(ReservationStatus.CANCELLED);
		else if (paymentDto.getStatus().equals(PaymentStatus.FAILED)) {
			// Add retry logic
			reservation.setStatus(ReservationStatus.RESERVED);
		} else if (paymentDto.getStatus().equals(PaymentStatus.PENDING)) {
			// Check payment status again
			reservation.setStatus(ReservationStatus.RESERVED);
		}

		reservationService.updateReservation(reservation.getId(), reservation);

	}

}