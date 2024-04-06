package org.booking.core.service.appointment;

import org.booking.core.domain.entity.history.UserReservationHistory;

public interface UserReservationHistoryService {

	UserReservationHistory findByUserEmail(String userEmail);
	void save(UserReservationHistory history);
}
