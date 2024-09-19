package au.com.test.cba.kafka.stream.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import au.com.test.cba.kafka.model.Customer;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
class KafkaStreamProcessorTest {

	private KafkaStreamProcessor kafkaStreamProcessor;

	@BeforeEach
	public void setUp() {
		kafkaStreamProcessor = new KafkaStreamProcessor();
	}

	@Test
	public void shouldSendToEvenTopicBinder() {
		Customer customer = new Customer("John", "Doe", "1990-01-01"); // Even age
		Message<Customer> message = MessageBuilder.withPayload(customer).build();
		Message<Customer> resultMessage = kafkaStreamProcessor.processor().apply(message);

		assertNotNull(resultMessage);
		assertEquals(customer, resultMessage.getPayload());
		assertEquals(KafkaStreamProcessor.EVEN_TOPIC_BINDER, resultMessage.getHeaders().get(KafkaStreamProcessor.STREAM_SENDTO_DESTINATION_HEADER));
	}

	@Test
	public void shouldSendToOddTopicBinder() {
		Customer customer = new Customer("Jane", "Doe", "1991-01-01"); // Odd age
		Message<Customer> message = MessageBuilder.withPayload(customer).build();
		Message<Customer> resultMessage = kafkaStreamProcessor.processor().apply(message);

		assertNotNull(resultMessage);
		assertEquals(customer, resultMessage.getPayload());
		assertEquals(KafkaStreamProcessor.ODD_TOPIC_BINDER, resultMessage.getHeaders().get(KafkaStreamProcessor.STREAM_SENDTO_DESTINATION_HEADER));
	}

	@Test
	public void shouldThrowRunTimeExceptionForFutureDOB() {
		Customer customer = new Customer("Jon", "FutureDoe", "3000-01-01"); // Future date
		Message<Customer> message = MessageBuilder.withPayload(customer).build();
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			kafkaStreamProcessor.processor().apply(message);
		});

		assertEquals("Future date of birth is invalid", exception.getMessage());
	}

	@Test
	public void shouldSendToEvenTopiBinderForAgeLessThanAYear() {
		String dobStr = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now().minusMonths(6L));
		Customer customer = new Customer("New", "born", dobStr); // Less than one year
		Message<Customer> message = MessageBuilder.withPayload(customer).build();

		Message<Customer> resultMessage = kafkaStreamProcessor.processor().apply(message);
		assertNotNull(resultMessage);
		assertEquals(customer, resultMessage.getPayload());
		assertEquals(KafkaStreamProcessor.EVEN_TOPIC_BINDER, resultMessage.getHeaders().get(KafkaStreamProcessor.STREAM_SENDTO_DESTINATION_HEADER));

	}

	@Test
	public void shouldCacluateAgeCorrectly() {
		String dateOfBirth = "1990-01-01";
		int age = KafkaStreamProcessor.calculateAge(dateOfBirth);

		assertEquals(LocalDate.now().getYear() - 1990, age);
	}

	@Test
	public void shouldThrowRunTimeExceptionForFutureDate() {
		String futureDateOfBirth = "3000-01-01";
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			KafkaStreamProcessor.calculateAge(futureDateOfBirth);
		});
		assertEquals("Future date of birth is invalid", exception.getMessage());
	}
}
