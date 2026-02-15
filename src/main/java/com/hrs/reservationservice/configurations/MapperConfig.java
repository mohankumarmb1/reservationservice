package com.hrs.reservationservice.configurations;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hrs.reservationservice.entities.Reservation;
import com.hrs.reservationservice.entities.ReservationInfo;
import com.hrs.reservationservice.models.ReservationDto;
import com.hrs.reservationservice.models.ReservationInfoDto;

@Configuration
public class MapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	/**
	 * Mapping for converting ReservationInfoDto into ReservationInfo on the
	 * Reservation. To use: modelMapper.addMappings(reservationMap);
	 * 
	 */
	PropertyMap<ReservationDto, Reservation> reservationMap = new PropertyMap<ReservationDto, Reservation>() {
		protected void configure() {
			using(dtoToEntityConverter).map(source.getReservationInfoDto()).setReservationInfo(null);
		}
	};

	/**
	 * Mapping for converting ReservationInfo into ReservationInfoDto on the
	 * ReservationDto. To use: modelMapper.addMappings(reservationDtoMap);
	 * 
	 */
	PropertyMap<Reservation, ReservationDto> reservationDtoMap = new PropertyMap<Reservation, ReservationDto>() {
		protected void configure() {
			using(entityToDtoConverter).map(source.getReservationInfo()).setReservationInfoDto(null);
		}
	};

	/**
	 * Converter for converting a ReservationInfoDto on the DTO into ReservationInfo
	 * 
	 */
	Converter<ReservationInfoDto, ReservationInfo> dtoToEntityConverter = new Converter<ReservationInfoDto, ReservationInfo>() {
		public ReservationInfo convert(MappingContext<ReservationInfoDto, ReservationInfo> context) {
			ReservationInfoDto dto = context.getSource();
			return modelMapper().map(dto, ReservationInfo.class);
		}
	};

	/**
	 * Converter for converting a ReservationInfo into ReservationInfoDto on the DTO
	 * 
	 */
	Converter<ReservationInfo, ReservationInfoDto> entityToDtoConverter = new Converter<ReservationInfo, ReservationInfoDto>() {
		public ReservationInfoDto convert(MappingContext<ReservationInfo, ReservationInfoDto> context) {
			ReservationInfo info = context.getSource();
			return modelMapper().map(info, ReservationInfoDto.class);
		}
	};

}