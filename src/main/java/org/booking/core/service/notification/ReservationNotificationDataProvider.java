package org.booking.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.booking.core.domain.entity.reservation.Reservation;
import org.booking.core.notification.dto.Contact;
import org.booking.core.notification.dto.Notification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Log
@RequiredArgsConstructor
@Service
public class ReservationNotificationDataProvider implements NotificationDataProvider<Reservation, Notification> {

	@Override
	public Notification generateMessage(String action, @NonNull Reservation obj) {
		Contact employeeContact = new Contact();
		employeeContact.setEmail(obj.getEmployeeEmail());
//		employeeContact.setRole(RoleClassification.MANAGER.name());
		Contact customerContact = new Contact();
		customerContact.setEmail(obj.getCustomerEmail());
//		customerContact.setRole(RoleClassification.CUSTOMER.name());
		return Notification.builder()
				.action(action)
				.uuid(UUID.randomUUID().toString())
				.contacts(List.of(employeeContact, customerContact))
				.build();
	}


}
