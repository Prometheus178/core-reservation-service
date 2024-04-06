package org.booking.core.domain.response;

import lombok.Data;
import org.booking.core.service.BusinessServiceResponse;

import java.util.Set;

@Data
public class BusinessResponse {
	private Long id;
	private String type;
	private String name;
	private String address;
	private String description;
	private BusinessHoursResponse businessHours;
	private Set<BusinessServiceResponse> businessServices;
	private Set<String> employees;

}
