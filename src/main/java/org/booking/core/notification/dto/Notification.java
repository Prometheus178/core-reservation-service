package org.booking.core.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
public class Notification implements Serializable {

	String uuid;
	NotificationChannel notificationChannel;
	List<Contact> contacts;
	String action;

}
