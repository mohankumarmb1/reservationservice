package com.hrs.reservationservice.model;

import com.hrs.reservationservice.enums.ReservationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Reservation
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long hotelId;
    private Integer roomNumber;
    private Date reservationDate;
    private ReservationStatus status;
}
