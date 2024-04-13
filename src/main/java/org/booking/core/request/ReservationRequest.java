package org.booking.core.request;

import lombok.Data;


@Data
public class ReservationRequest {

	private Long businessId;
	private Long businessServiceId;
	private String employeeEmail;
	private String bookingTime;
	private DurationRequest duration;

}
