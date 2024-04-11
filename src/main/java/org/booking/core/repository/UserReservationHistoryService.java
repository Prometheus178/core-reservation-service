package org.booking.core.repository;

import org.booking.core.domain.entity.history.UserReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReservationHistoryService extends JpaRepository<UserReservationHistory, Long> {

	UserReservationHistory findByUserEmail(String userEmail);
}
