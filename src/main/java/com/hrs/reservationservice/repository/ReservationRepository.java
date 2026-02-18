package com.hrs.reservationservice.repository;

import com.hrs.reservationservice.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long>
{
    List<Reservation> findByUserId(Long userId);
}
