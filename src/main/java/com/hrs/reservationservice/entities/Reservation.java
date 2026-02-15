package com.hrs.reservationservice.entities;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.hrs.reservationservice.models.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservation")
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Invalid customerId: CustomerId may not be null.")
	private Long customerId;

	private Long hotelId;

	private Long paymentId;

	@NotNull(message = "Invalid startDate: Start Date may not be null.")
	private LocalDate startDate;

	@NotNull(message = "Invalid endDate: End Date may not be null.")
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid status: Reservation status may not be null.")
	private ReservationStatus status;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "reservation_info_id", referencedColumnName = "id")
	@NotNull(message = "Invalid reservationInfo: Reservation details may not be null.")
	private ReservationInfo reservationInfo;

	public Reservation updateWith(Reservation reservation) {
		return new Reservation(this.id, reservation.customerId, reservation.hotelId, reservation.paymentId,
				reservation.startDate, reservation.endDate, reservation.status, reservation.reservationInfo);
	}

}
