package com.hrs.reservationservice.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrs.reservationservice.messaging.TopicProducer;
import com.hrs.reservationservice.models.ReservationDto;
import com.hrs.reservationservice.services.ReservationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(produces = "application/json", value = "Operations pertaining to manage reservations in hotel reservation system")
@RestController
@RequestMapping("/api")
public class ReservationController {
	private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

	@Autowired
	private ReservationService reservationService;

	private TopicProducer topicProducer;

	public ReservationController(TopicProducer producer) {
		this.topicProducer = producer;
	}

	@GetMapping("/welcome")
	private ResponseEntity<String> displayWelcomeMessage() {
		return new ResponseEntity<>("Welcome to reservation service !!", HttpStatus.OK);
	}

	@GetMapping("/retrieve/{id}")
	@ApiOperation(value = "Retrieve the reservation with the specified reservation id", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved reservation"),
			@ApiResponse(code = 404, message = "Reservation with specified reservation id not found"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	private ResponseEntity<ReservationDto> getReservationById(@PathVariable("id") @Valid long id) {
		ReservationDto reservation = reservationService.getReservationById(id);
		return new ResponseEntity<>(reservation, HttpStatus.OK);
	}

	@PostMapping("/reserve")
	@ApiOperation(value = "Register a new reservation", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Successfully registered the reservation"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	private ResponseEntity<ReservationDto> makeReservation(@RequestBody @Valid ReservationDto reservation) {

		ReservationDto savedReservation = reservationService.createReservation(reservation);

		log.info("Reservation made successfully hence publishing the reservation event.");
		topicProducer.sendReservation(savedReservation);

		return new ResponseEntity<>(savedReservation, HttpStatus.CREATED);
	}

	@PutMapping("/cancel/{id}")
	@ApiOperation(value = "Cancel a reservation", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully cancelled reservation"),
			@ApiResponse(code = 404, message = "Reservation with specified reservation id not found"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	public ResponseEntity<ReservationDto> cancelReservation(@PathVariable("id") @Valid long id,
			@RequestBody @Valid ReservationDto reservation) {

		ReservationDto updatedReservation = reservationService.updateReservation(id, reservation);

		log.info("Reservation cancelled successfully hence publishing the cancellation event.");
		topicProducer.sendCancellation(updatedReservation);

		return new ResponseEntity<>(updatedReservation, HttpStatus.OK);
	}

	@PutMapping("/update/{id}")
	@ApiOperation(value = "Update a reservation information", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated reservation information"),
			@ApiResponse(code = 404, message = "Reservation with specified reservation id not found"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	public ResponseEntity<ReservationDto> updateReservation(@PathVariable("id") @Valid long id,
			@RequestBody @Valid ReservationDto reservation) {
		return new ResponseEntity<>(reservationService.updateReservation(id, reservation), HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	@ApiOperation(value = "Delete a reservation", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Successfully deleted reservation information"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	private ResponseEntity<String> deleteReservation(@PathVariable("id") @Valid long id) {
		reservationService.deleteReservation(id);
		return new ResponseEntity<>("Reservation deleted successfully", HttpStatus.OK);
	}

}
