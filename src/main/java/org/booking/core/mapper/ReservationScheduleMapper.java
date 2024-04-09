package org.booking.core.mapper;


import org.booking.core.domain.entity.reservation.ReservationSchedule;
import org.booking.core.request.ReservationScheduleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReservationScheduleMapper {

	ReservationScheduleMapper INSTANCE = Mappers.getMapper(ReservationScheduleMapper.class);

	ReservationSchedule toEntity(ReservationScheduleRequest dto);


}
