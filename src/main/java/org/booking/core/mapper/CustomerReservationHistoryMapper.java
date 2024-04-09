package org.booking.core.mapper;

import org.booking.core.domain.entity.history.UserReservationHistory;
import org.booking.core.domain.entity.reservation.Reservation;
import org.booking.core.request.ReservationRequest;
import org.booking.core.request.UserReservationHistoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class CustomerReservationHistoryMapper {


	static CustomerReservationHistoryMapper INSTANCE = Mappers.getMapper(CustomerReservationHistoryMapper.class);

	abstract UserReservationHistoryRequest toDto(UserReservationHistory obj);

	abstract Set<Reservation> mapToEntitySet(Set<ReservationRequest> dtoSet);

	abstract Set<ReservationRequest> mapToDtoSet(Set<Reservation> entitySet);





}