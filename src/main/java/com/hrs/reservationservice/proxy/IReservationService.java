package com.hrs.reservationservice.proxy;


import com.hrs.reservationservice.exceptions.ReservationNotFoundException;
import com.hrs.reservationservice.model.Reservation;
import java.util.List;

public interface IReservationService
{

    public Reservation reserve(Reservation reservation);

    public Reservation getReservation(Long id) throws ReservationNotFoundException;


    public List<Reservation> getAllReservation();


    public Reservation updateReservation(Reservation reservation) throws ReservationNotFoundException;

    public Reservation cancelReservation(Long id) throws ReservationNotFoundException;
    public boolean deleteReservation(Long id) throws ReservationNotFoundException;

}
