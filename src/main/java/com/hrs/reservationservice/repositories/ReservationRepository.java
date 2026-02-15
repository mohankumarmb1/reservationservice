package com.hrs.reservationservice.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrs.reservationservice.entities.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByCustomerId(Long customerId);
	
	List<Reservation> findByPaymentId(Long paymentId);

}
