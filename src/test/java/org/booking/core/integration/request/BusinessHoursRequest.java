package org.booking.core.integration.request;

import lombok.Data;

@Data
public class BusinessHoursRequest {
	private String openTime;
	private String closeTime;
}