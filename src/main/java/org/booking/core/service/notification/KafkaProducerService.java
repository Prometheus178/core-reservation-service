package org.booking.core.service.notification;

public interface KafkaProducerService {

	void sent(String topicName, String json);

}
