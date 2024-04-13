package org.booking.core.service;

import org.booking.core.response.BusinessServiceResponse;

public interface BusinessService {

	BusinessServiceResponse findById(Long businessServiceId);

}
