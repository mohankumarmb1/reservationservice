package com.hrs.reservationservice.services.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrs.reservationservice.entities.Reservation;
import com.hrs.reservationservice.exceptions.ReservationNotFoundException;
import com.hrs.reservationservice.models.ReservationDto;
import com.hrs.reservationservice.models.ReservationInfoDto;
import com.hrs.reservationservice.models.RoomDto;
import com.hrs.reservationservice.repositories.ReservationRepository;
import com.hrs.reservationservice.services.HotelServiceFeign;
import com.hrs.reservationservice.services.ReservationService;

@Service
public class ReservationServiceImpl implements ReservationService {

	private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private HotelServiceFeign hotelServiceFeign;

	/**
	 * Retrieves a reservation with unique reservation id
	 */
	@Override
	public ReservationDto getReservationById(@Valid long id) {
		Optional<Reservation> reservation = reservationRepository.findById(id);
		if (reservation.isPresent())
			return mapper.map(reservation.get(), ReservationDto.class);
		else
			throw new ReservationNotFoundException("Reservation with id: " + id + " not found");
	}

	/**
	 * Retrieves all the existing reservations with unique customer id
	 */
	@Override
	public List<ReservationDto> getReservationsByCustomerId(@Valid long customerId) {
		List<ReservationDto> reservations = new ArrayList<>();
		reservationRepository.findByCustomerId(customerId).forEach(reservation -> {
			reservations.add(mapper.map(reservation, ReservationDto.class));
		});
		return reservations;
	}

	/**
	 * Retrieves all the existing reservations with payment id
	 */
	@Override
	public List<ReservationDto> getReservationsByPaymentId(@Valid long paymentId) {
		List<ReservationDto> reservations = new ArrayList<>();
		reservationRepository.findByPaymentId(paymentId).forEach(reservation -> {
			reservations.add(mapper.map(reservation, ReservationDto.class));
		});
		return reservations;
	}

	/**
	 * Makes a new reservation
	 */
	@Override
	public ReservationDto createReservation(@Valid ReservationDto reservationDto) {

		ReservationInfoDto reservationInfoDto = reservationDto.getReservationInfoDto();
		boolean available = hotelServiceFeign.checkAvailability(reservationInfoDto.getRoomType());

		if (available) {
			RoomDto room = new RoomDto();
			room.setCustomerId(reservationDto.getCustomerId());
			room.setOccupancyStartDate(reservationDto.getStartDate());
			room.setOccupancyEndDate(reservationDto.getEndDate());
			room.setNumberOfRooms(reservationInfoDto.getNumberOfRooms());
			room.setNumberOfAdults(reservationInfoDto.getNumberOfAdults());
			room.setNumberOfChildren(reservationInfoDto.getNumberOfChildren());

			RoomDto createdRoom = hotelServiceFeign.createRoom(room, reservationInfoDto.getRoomType());
			reservationDto.setHotelId(createdRoom.getId());

			Reservation reservation = mapper.map(reservationDto, Reservation.class);
			return mapper.map(reservationRepository.save(reservation), ReservationDto.class);

		} else {
			throw new ReservationNotFoundException(
					"Room not available with room type: " + reservationInfoDto.getRoomType());
		}

	}

	/**
	 * Updates a reservation with unique reservation id
	 */
	@Override
	public ReservationDto updateReservation(@Valid long id, @Valid ReservationDto reservationDto) {
		Optional<Reservation> reservationFound = reservationRepository.findById(id);

		if (reservationFound.isPresent()) {
			Optional<Reservation> updatedReservation = reservationFound.map(existingReservation -> {
				Reservation reservation = mapper.map(reservationDto, Reservation.class);
				return reservationRepository.save(existingReservation.updateWith(reservation));
			});

			return mapper.map(updatedReservation.get(), ReservationDto.class);
		} else
			throw new ReservationNotFoundException("Reservation with id: " + id + " not found");
	}

	/**
	 * Deletes a reservation with unique reservation id
	 */
	@Override
	public void deleteReservation(@Valid long id) {
		if (getReservationById(id) != null) {
			reservationRepository.deleteById(id);
			log.info("Reservation deleted Successfully");
		} else {
			throw new ReservationNotFoundException("Reservation with id: " + id + " not found");
		}
	}

}
