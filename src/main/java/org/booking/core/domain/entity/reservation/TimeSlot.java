package org.booking.core.domain.entity.reservation;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;


public record TimeSlot(LocalTime startTime, LocalTime endTime) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return startTime + "-" + endTime;
	}
}
