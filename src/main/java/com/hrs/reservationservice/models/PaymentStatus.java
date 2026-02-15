package com.hrs.reservationservice.models;

import java.util.Random;

public enum PaymentStatus {
	PENDING, SUCCESS, FAILED;

	private static final Random random = new Random();

	public static PaymentStatus randomPaymentStatus() {
		PaymentStatus[] status = values();
		return status[random.nextInt(status.length)];
	}

}
