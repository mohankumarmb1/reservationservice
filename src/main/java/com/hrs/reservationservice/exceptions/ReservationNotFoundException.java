package com.hrs.reservationservice.exceptions;

public class ReservationNotFoundException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReservationNotFoundException() {
		super();
	}

	public ReservationNotFoundException(String errorMessage) {
		super(errorMessage);
	}

}
