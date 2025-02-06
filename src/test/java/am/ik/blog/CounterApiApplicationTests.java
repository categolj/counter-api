package am.ik.blog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CounterApiApplicationTests {

	RestClient restClient;

	@BeforeEach
	void setUp(@LocalServerPort int port, @Autowired RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl("http://localhost:" + port)
			.defaultStatusHandler(status -> true, (request, response) -> {

			})
			.build();
	}

	@Test
	void testIncrement() {
		ResponseEntity<String> response = this.restClient.post()
			.uri("/counter")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"entryId": 100}
					""")
			.retrieve()
			.toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
				{"counter":1}
				""");
	}

	@Test
	void testIncrementAgain() {
		ResponseEntity<String> response = this.restClient.post()
			.uri("/counter")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"entryId": 100}
					""")
			.retrieve()
			.toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
				{"counter":2}
				""");
	}

	@Test
	void testIncrementAnotherEntry() {
		ResponseEntity<String> response = this.restClient.post()
			.uri("/counter")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"entryId": 101}
					""")
			.retrieve()
			.toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
				{"counter":1}
				""");
	}

	@Test
	void testGetAll() {
		ResponseEntity<String> response = this.restClient.get().uri("/counter").retrieve().toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
				[{"entryId":100,"counter":2},{"entryId":101,"counter":1}]
				""");
	}

}
