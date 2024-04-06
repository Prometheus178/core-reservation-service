package org.booking.core.service;

import lombok.Getter;
import lombok.Setter;
import org.booking.core.domain.response.BusinessResponse;

import java.math.BigDecimal;

@Getter
@Setter
public class BusinessServiceResponse {

	private String name;
	private String description;
	private BigDecimal price;
	private int duration;
	private String modifiedByUser;

	private BusinessResponse business;
}
