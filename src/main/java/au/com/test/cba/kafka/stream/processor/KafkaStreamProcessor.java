package au.com.test.cba.kafka.stream.processor;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import au.com.test.cba.kafka.model.Customer;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaStreamProcessor {

	public static final String ODD_TOPIC_BINDER = "processor-out-1";
	public static final String EVEN_TOPIC_BINDER = "processor-out-0";
	public static final String STREAM_SENDTO_DESTINATION_HEADER = "spring.cloud.stream.sendto.destination";

	@Bean
	public Function<Message<Customer>, Message<Customer>> processor() {
		return inputMessage -> {
			try {
				Customer payload = inputMessage.getPayload();
				// Check if the age attribute is even or odd
				if (calculateAge(payload.getDateOfBirth()) % 2 == 0) {
					// If even, publish to the first output
					return MessageBuilder.withPayload(payload)
							.setHeader(STREAM_SENDTO_DESTINATION_HEADER, EVEN_TOPIC_BINDER).build();
				} else {
					// If odd, publish to the second output (process-out-1)
					return MessageBuilder.withPayload(payload)
							.setHeader(STREAM_SENDTO_DESTINATION_HEADER, ODD_TOPIC_BINDER).build();
				}
			} catch (Exception e) {
				log.error("Error occured in processor while processing message. ", e);
				throw e;
			}

		};
	}

	static int calculateAge(String dateOfBirth) {
		LocalDate birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		int years = Period.between(birthDate, LocalDate.now()).getYears();
		if( years < 0) {
			throw new RuntimeException("Future date of birth is invalid");
		}
		else if( years == 0) {
			log.warn("Age is less than 1 year. Considering it as even");
		}
		return years;
	}

}
