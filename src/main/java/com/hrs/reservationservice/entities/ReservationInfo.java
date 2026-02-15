package com.hrs.reservationservice.entities;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.hrs.reservationservice.models.PaymentProviderType;
import com.hrs.reservationservice.models.PaymentType;
import com.hrs.reservationservice.models.RoomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservation_info")
public class ReservationInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid roomType: Room type may not be null.")
	private RoomType roomType;

	@NotNull(message = "Invalid numberOfRooms: Number of rooms may not be null.")
	private Integer numberOfRooms;

	@NotNull(message = "Invalid numberOfAdults: Number of adults may not be null.")
	private Integer numberOfAdults;

	@NotNull(message = "Invalid numberOfChildren: Number of children may not be null.")
	private Integer numberOfChildren;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid provider: Payment provider may not be null.")
	private PaymentProviderType provider;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid paymentType: Payment type may not be null.")
	private PaymentType paymentType;

	@NotNull(message = "Invalid amount: Payment amount may not be null.")
	private BigDecimal paymentAmount;

	private String cardNumber;

	private String cardHolderName;

	private String cvv;

	private String virtualPaymentAddress;

	private String userComments;

	@OneToOne(mappedBy = "reservationInfo")
	private Reservation reservation;

}
