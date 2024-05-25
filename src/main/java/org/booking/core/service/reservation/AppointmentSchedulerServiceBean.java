package org.booking.core.service.reservation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.booking.core.actions.AppointmentAction;
import org.booking.core.domain.entity.history.UserReservationHistory;
import org.booking.core.domain.entity.reservation.Duration;
import org.booking.core.domain.entity.reservation.Reservation;
import org.booking.core.domain.entity.reservation.ReservationSchedule;
import org.booking.core.domain.entity.reservation.TimeSlot;
import org.booking.core.lock.RedisDistributedLock;
import org.booking.core.mapper.ReservationMapper;
import org.booking.core.repository.ReservationRepository;
import org.booking.core.repository.ReservationScheduleRepository;
import org.booking.core.repository.UserReservationHistoryService;
import org.booking.core.request.ReservationRequest;
import org.booking.core.response.BusinessHoursResponse;
import org.booking.core.response.BusinessResponse;
import org.booking.core.response.BusinessServiceResponse;
import org.booking.core.response.ReservationResponse;
import org.booking.core.service.UserService;
import org.booking.core.service.notification.ReservationNotificationManager;
import org.booking.core.service.outer.OuterBusinessService;
import org.booking.core.service.reservation.cache.CachingAppointmentSchedulerService;
import org.booking.core.util.KeyUtil;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.booking.core.config.kafka.KafkaTopicConfig.NOTIFICATION_IN_0;

@RequiredArgsConstructor
@Log
@Service
public class AppointmentSchedulerServiceBean implements AppointmentSchedulerService {

	public static final String RESERVED = "Reserved";
	private final ReservationRepository reservationRepository;
	private final CachingAppointmentSchedulerService cachingAppointmentSchedulerService;
	private final UserReservationHistoryService userReservationHistoryRepository;
	private final ReservationMapper reservationMapper;
	private final ReservationScheduleRepository reservationScheduleRepository;
	private final RedisDistributedLock redisDistributedLock;
	private final UserService userService;
	private final ReservationNotificationManager reservationNotificationManager;
	private final OuterBusinessService outerBusinessService;

	@Override
	public List<TimeSlot> findAvailableSlots(Long businessServiceId, LocalDate date) {
		BusinessServiceResponse businessService = outerBusinessService.getBusinessServiceResponse(businessServiceId);
		long start = System.currentTimeMillis();
		log.info("findAvailableTimeSlotsByKey");
		log.info("start" + start);
		List<TimeSlot> availableTimeSlotsByDay =
				cachingAppointmentSchedulerService.findAvailableTimeSlotsByKey(KeyUtil.generateKey(date, businessServiceId));
		long end = System.currentTimeMillis();
		log.info("end " + end);
		log.info("result: " + (end - start));
		if (availableTimeSlotsByDay.isEmpty()) {
			List<TimeSlot> availableTimeSlots = computeTimeSlots(businessService);
			log.info("Try to save computed time slots");
			cachingAppointmentSchedulerService.saveAvailableTimeSlotsByKey(KeyUtil.generateKey(date,
					businessServiceId), availableTimeSlots);
			log.info("Saved computed time slots");
			return availableTimeSlots;
		}
		return availableTimeSlotsByDay;

	}

	@Override
	public ReservationResponse reserve(ReservationRequest reservationRequest) {
		Reservation reservation = reservationMapper.toEntity(reservationRequest);
		long start = System.currentTimeMillis();
		log.info("locking");
		log.info("start" + start);
		Reservation savedReservation = reserve(reservation);
		long end = System.currentTimeMillis();
		log.info("end " + end);
		log.info("result: " + (end - start));
		reservationNotificationManager.sendNotification(NOTIFICATION_IN_0,
				AppointmentAction.CREATED_RESERVATION.getValue(), savedReservation);
		return reservationMapper.toResponse(savedReservation);
	}


	@Override
	public ReservationResponse modifyReservation(Long reservationId, ReservationRequest reservationRequest) {
		Reservation existReservation = reservationRepository.findById(reservationId).orElseThrow(EntityNotFoundException::new);
		Reservation reservation = reservationMapper.toEntity(reservationRequest);
		String lockName = getComputedLockName(reservation);
		RLock lock = redisDistributedLock.getLock(lockName);
		try {
			boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
			if (locked) {
				log.info("Locked: " + lockName);
				existReservation.setCanceled(true);
				log.info(String.format("Reservation id=%s canceled" , existReservation.getId()) );
				updateTimeSlotsInCache(reservation.getBusinessServiceId(), existReservation.getDuration(),
						reservation.getDuration(),
						reservation.getBookingTime().toLocalDate());
				Reservation savedReservation = reserve(reservation);
				reservationNotificationManager.sendNotification(NOTIFICATION_IN_0,
						AppointmentAction.MODIFIED_RESERVATION.getValue(), savedReservation);
				return reservationMapper.toResponse(savedReservation);
			} else {
				throw new RuntimeException(RESERVED);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean cancelReservation(Long reservationId) {
		Optional<Reservation> existOptional = reservationRepository.findById(reservationId);
		if (existOptional.isPresent()) {
			Reservation existReservation = existOptional.get();
			existReservation.setCanceled(true);
			return true;
		} else {
			return false;
		}
	}

	private Reservation reserve(Reservation reservation) {
		String currentUserEmail = userService.getCurrentUserEmail();
		reservation.setCustomerEmail(currentUserEmail);

		String lockName = getComputedLockName(reservation);
		log.info("Created lockName " + lockName);
		RLock lock = redisDistributedLock.getLock(lockName);
		try {
			log.info("Try to get lock: " + lockName);
			boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
			if (locked) {
				log.info("Locked: " + lockName);
				String key = KeyUtil.generateKey(reservation.getBookingTime().toLocalDate(),
						reservation.getBusinessServiceId());
				cachingAppointmentSchedulerService.removeTimeSlotByKey(key, computeTimeSlot(reservation.getDuration()));
				Reservation savedReservation = reservationRepository.save(reservation);
				addReservationToBusinessSchedule(savedReservation);
				saveToUserHistory(savedReservation, savedReservation.getCustomerEmail());
				saveToUserHistory(savedReservation, savedReservation.getEmployeeEmail());
				return savedReservation;
			} else {
				throw new RuntimeException(RESERVED);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			log.info("Unlocked: " + lockName);
			lock.unlock();
		}
	}

	private String getComputedLockName(Reservation reservation) {
		return KeyUtil.generateKey(reservation.getBookingTime().toLocalDate(),
				reservation.getBusinessServiceId(), computeTimeSlot(reservation.getDuration()));
	}

	private void addReservationToBusinessSchedule(Reservation savedReservation) {
		Long businessId = savedReservation.getBusinessId();
		Optional<ReservationSchedule> reservationScheduleByBusinessId =
				reservationScheduleRepository.findById(businessId);
		if (reservationScheduleByBusinessId.isPresent()) {
			ReservationSchedule reservationSchedule = reservationScheduleByBusinessId.get();
			reservationSchedule.addReservation(savedReservation);
		} else {
			ReservationSchedule reservationSchedule = new ReservationSchedule();
			reservationSchedule.setBusinessId(businessId);
			reservationSchedule.addReservation(savedReservation);
			reservationScheduleRepository.save(reservationSchedule);
		}
	}

	private void updateTimeSlotsInCache(Long businessServiceId,
										Duration existReservationDuration,
										Duration newReservationDuration,
										LocalDate date) {
		TimeSlot existTimeSlot = computeTimeSlot(existReservationDuration);
		TimeSlot newTimeSlot = computeTimeSlot(newReservationDuration);
		cachingAppointmentSchedulerService.removeTimeSlotByKey(KeyUtil.generateKey(date,
				businessServiceId), newTimeSlot);
		cachingAppointmentSchedulerService.addTimeSlotByKey(KeyUtil.generateKey(date,
				businessServiceId), existTimeSlot);
	}

	private void saveToUserHistory(Reservation savedReservation, String userEmail) {
		UserReservationHistory userReservationHistory =
				userReservationHistoryRepository.findByUserEmail(userEmail).orElseGet(UserReservationHistory::new);
		userReservationHistory.addReservation(savedReservation);
		userReservationHistory.setUserEmail(userEmail);
		userReservationHistoryRepository.save(userReservationHistory);
	}

	private TimeSlot computeTimeSlot(Duration duration) {
		LocalDateTime startTime = duration.getStartTime();
		LocalDateTime endTime = duration.getEndTime();
		return new TimeSlot(startTime.toLocalTime(), endTime.toLocalTime());
	}

	private List<TimeSlot> computeTimeSlots(BusinessServiceResponse businessServiceResponse) {
		int duration = businessServiceResponse.getDuration();
		BusinessResponse business = businessServiceResponse.getBusiness();
		BusinessHoursResponse businessHours = business.getBusinessHours();
		List<TimeSlot> availableTimeSlots = new ArrayList<>();

		LocalTime openTime = LocalTime.parse(businessHours.getOpenTime());
		LocalTime closeTime = LocalTime.parse(businessHours.getCloseTime());

		while (openTime.plusMinutes(duration).isBefore(closeTime.plusMinutes(1))) {
			LocalTime slotStart = openTime;
			LocalTime slotEnd = openTime.plusMinutes(duration);


			TimeSlot timeSlot = new TimeSlot(slotStart, slotEnd);
			availableTimeSlots.add(timeSlot);

			openTime = slotEnd;
		}
		return availableTimeSlots;
	}

}
