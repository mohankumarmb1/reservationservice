package com.hrs.reservationservice.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.hrs.reservationservice.models.ReservationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicProducer {

	@Value("${producer.config.reservation.topic.name}")
	private String reservationTopicName;

	@Value("${producer.config.cancellation.topic.name}")
	private String cancellationTopicName;

	private final KafkaTemplate<String, ReservationDto> kafkaTemplate;

	public void sendReservation(ReservationDto reservation) {
		log.info("Room Reservation : {}", reservation.toString());
		kafkaTemplate.send(reservationTopicName, reservation);
	}

	public void sendCancellation(ReservationDto reservation) {
		log.info("Room Cancellation : {}", reservation.toString());
		kafkaTemplate.send(cancellationTopicName, reservation);
	}

}