package com.hrs.reservationservice.services;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.hrs.reservationservice.models.RoomDto;
import com.hrs.reservationservice.models.RoomType;

@FeignClient(name = "hotel-management-service")
public interface HotelServiceFeign {

	@GetMapping("/api/checkAvailability")
	Boolean checkAvailability(@RequestParam @Valid RoomType roomType);

	@GetMapping("/api/retrieve/{id}")
	RoomDto getRoomById(@PathVariable("id") @Valid long id);

	@PostMapping("/api/create")
	RoomDto createRoom(@RequestBody @Valid RoomDto room, @RequestParam @Valid RoomType type);

	@PutMapping("/api/update/{id}")
	RoomDto updateRoom(@PathVariable("id") @Valid long id, @RequestBody @Valid RoomDto room);

	@DeleteMapping("/api/delete/{id}")
	RoomDto deleteRoom(@PathVariable("id") @Valid long id);

}
