package org.booking.core.response;

import lombok.Data;

import java.util.Set;

@Data
public class UserReservationHistoryResponse {
	private Long id;
	private Set<ReservationResponse> reservations;
	private Long customerId;
	private String eventType;
	private String eventTime;
	private String details;

}