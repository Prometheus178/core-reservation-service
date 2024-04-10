package org.booking.core.integration.request;

import lombok.Data;

@Data
public class BusinessRequest {
	private String type;
	private String name;
	private String address;
	private String description;
	private BusinessHoursRequest businessHours;

}
