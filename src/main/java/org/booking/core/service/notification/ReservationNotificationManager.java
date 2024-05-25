package org.booking.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.booking.core.domain.entity.reservation.Reservation;
import org.booking.core.notification.dto.Notification;
import org.booking.core.notification.dto.NotificationChannel;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.booking.core.actions.NotificationType.EMAIL;

@Log
@RequiredArgsConstructor
@Service
public class ReservationNotificationManager implements NotificationManager<Reservation> {

	private final KafkaProducerService kafkaProducerService;
	private final NotificationDataProvider<Reservation, Notification> dataProvider;

	@Override
	public void sendNotification(String receiver, String action, Reservation obj) {
		Notification message = dataProvider.generateMessage(action, obj);
		message.setNotificationChannel(getMetaInfoDto());
		kafkaProducerService.sent(receiver, toJson(message));
	}

	private NotificationChannel getMetaInfoDto() {
		NotificationChannel notificationChannel = new NotificationChannel();
		notificationChannel.setSender("booking-core");
		notificationChannel.setReceiver("booking-core-notification-service");
		notificationChannel.setNotifyBy(List.of(EMAIL));
		return notificationChannel;
	}
}
