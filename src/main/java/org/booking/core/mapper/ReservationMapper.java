package org.booking.core.mapper;

import org.booking.core.domain.entity.reservation.Reservation;
import org.booking.core.domain.request.ReservationRequest;
import org.booking.core.domain.response.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class ReservationMapper {

	static ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);

	public abstract ReservationResponse toResponse(Reservation obj);

	public abstract Reservation toEntity(ReservationRequest dto);

}
