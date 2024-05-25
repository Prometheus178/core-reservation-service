package org.booking.core.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	public static final String NOTIFICATION_IN_0 = "notification-in-0";

	@Bean
	public NewTopic bookingCoreTopic(){
		return TopicBuilder.name(NOTIFICATION_IN_0).build();
	}
}
