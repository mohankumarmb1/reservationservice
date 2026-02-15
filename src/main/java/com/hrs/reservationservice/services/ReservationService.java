package com.hrs.reservationservice.services;

import java.util.List;

import javax.validation.Valid;

import com.hrs.reservationservice.models.ReservationDto;

public interface ReservationService {

	ReservationDto getReservationById(@Valid long id);

	List<ReservationDto> getReservationsByCustomerId(@Valid long customerId);
	
	List<ReservationDto> getReservationsByPaymentId(@Valid long paymentId);

	ReservationDto createReservation(@Valid ReservationDto reservation);

	ReservationDto updateReservation(@Valid long id, @Valid ReservationDto reservation);

	void deleteReservation(@Valid long id);

}
