package com.hrs.reservationservice.models;

import java.math.BigDecimal;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Class representing a Payment in hotel reservation system.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

	@ApiModelProperty(notes = "Unique identifier of the Payment.", example = "1")
	private Long id;

	@ApiModelProperty(notes = "Unique identifier of the Customer.", example = "1", required = true)
	@NotNull(message = "Invalid customerId: CustomerId may not be null.")
	private Long customerId;
	
	@ApiModelProperty(notes = "Unique identifier of the Reservation.", example = "1", required = true)
	@NotNull(message = "Invalid reservationId: ReservationId may not be null.")
	private Long reservationId;

	@ApiModelProperty(notes = "Amount to be paid.", example = "1000.00", required = true)
	@NotNull(message = "Invalid amount: Payment amount may not be null.")
	private BigDecimal amount;

	@ApiModelProperty(notes = "Payment gateway provider.", example = "CARD", required = true)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid provider: Payment provider may not be null.")
	private PaymentProviderType provider;

	@ApiModelProperty(notes = "Type of the payment.", example = "REFUND", required = true)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid type: Payment type may not be null.")
	private PaymentType type;

	@ApiModelProperty(notes = "Status of the payment.", example = "SUCCESS", required = true)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Invalid status: Payment status may not be null.")
	private PaymentStatus status;

	@ApiModelProperty(notes = "Card number for card payment.", example = "432156789876")
	private String cardNumber;

	@ApiModelProperty(notes = "Card holder name for card payment.", example = "RAJESH")
	private String cardHolderName;

	@ApiModelProperty(notes = "Cvv for card payment.", example = "456")
	private String cvv;

	@ApiModelProperty(notes = "Virtual payment address for UPI payment.", example = "9876543210@paytm")
	private String virtualPaymentAddress;

	@ApiModelProperty(notes = "User comments on UPI payment.", example = "Mobile Recharge")
	private String userComments;

}
