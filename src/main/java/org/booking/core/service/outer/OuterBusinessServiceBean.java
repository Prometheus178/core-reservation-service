package org.booking.core.service.outer;

import lombok.RequiredArgsConstructor;
import org.booking.core.service.BusinessServiceResponse;
import org.booking.core.service.request.OkHttpRequestService;
import org.springframework.stereotype.Service;

import static org.booking.core.service.request.OkHttpRequestService.HTTP_BOOKING_CORE_APP_8080;

@RequiredArgsConstructor
@Service
public class OuterBusinessServiceBean implements OuterBusinessService {

	private final OkHttpRequestService okHttpRequestService;

	@Override
	public BusinessServiceResponse getBusinessServiceResponse(Long businessServiceId) {
		String url = HTTP_BOOKING_CORE_APP_8080 + "/api/v1/inner/managements/" + businessServiceId;
		return okHttpRequestService.getRequest(url, BusinessServiceResponse.class);
	}
}
