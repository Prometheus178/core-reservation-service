package org.booking.core.repository;

import org.booking.core.domain.entity.history.UserReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReservationHistoryService extends JpaRepository<UserReservationHistory, Long> {

	Optional<UserReservationHistory> findByUserEmail(String userEmail);
}
