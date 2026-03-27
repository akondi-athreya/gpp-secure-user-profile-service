package com.example.userprofile;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(properties = {
		"spring.cloud.vault.enabled=false",
		"spring.config.import=",
		"db.username=sa",
		"db.password=password123",
		"api.signing-key=abcdefghijklmnopqrstuvwxyz123456"
}, webEnvironment = WebEnvironment.RANDOM_PORT)
class UserProfileControllerIntegrationTests {

	@LocalServerPort
	private int port;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final HttpClient httpClient = HttpClient.newHttpClient();

	private String url(String path) {
		return "http://localhost:" + port + path;
	}

	@Test
	void postProfileReturnsCreatedAndJwtWithSubject() throws Exception {
		String payload = objectMapper.writeValueAsString(Map.of(
				"userId", "u-001",
				"username", "alice",
				"email", "alice@test.com"));

		HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/profile")))
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.POST(HttpRequest.BodyPublishers.ofString(payload))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

		@SuppressWarnings("unchecked")
		Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
		assertThat(responseBody.get("userId")).isEqualTo("u-001");

		String authHeader = response.headers().firstValue(HttpHeaders.AUTHORIZATION).orElse(null);
		assertThat(authHeader).isNotNull();
		assertThat(authHeader).startsWith("Bearer ");

		String token = authHeader.substring("Bearer ".length());
		String[] parts = token.split("\\.");
		assertThat(parts).hasSize(3);

		String jsonPayload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		@SuppressWarnings("unchecked")
		Map<String, Object> claims = objectMapper.readValue(jsonPayload, Map.class);
		assertThat(claims.get("sub")).isEqualTo("u-001");
	}

	@Test
	void getProfileByIdReturns200ForExistingAnd404ForMissing() throws Exception {
		String payload = objectMapper.writeValueAsString(Map.of(
				"userId", "u-010",
				"username", "bob",
				"email", "bob@test.com"));

		HttpRequest createRequest = HttpRequest.newBuilder(URI.create(url("/api/profile")))
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.POST(HttpRequest.BodyPublishers.ofString(payload))
				.build();
		HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
		assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

		HttpRequest existingRequest = HttpRequest.newBuilder(URI.create(url("/api/profile/u-010"))).GET().build();
		HttpResponse<String> existingResponse = httpClient.send(existingRequest, HttpResponse.BodyHandlers.ofString());
		assertThat(existingResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		@SuppressWarnings("unchecked")
		Map<String, Object> existingBody = objectMapper.readValue(existingResponse.body(), Map.class);
		assertThat(existingBody.get("userId")).isEqualTo("u-010");
		assertThat(existingBody.get("username")).isEqualTo("bob");
		assertThat(existingBody.get("email")).isEqualTo("bob@test.com");

		HttpRequest missingRequest = HttpRequest.newBuilder(URI.create(url("/api/profile/u-999"))).GET().build();
		HttpResponse<String> missingResponse = httpClient.send(missingRequest, HttpResponse.BodyHandlers.ofString());
		assertThat(missingResponse.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	@Test
	void vaultStatusReturnsResolvedDbUsernameAndExpectedSchema() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/admin/vault-status"))).GET().build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

		@SuppressWarnings("unchecked")
		Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);
		assertThat(body.get("vaultConnected")).isEqualTo(true);
		assertThat(body.get("secretPath")).isEqualTo("secret/user-profile-service");
		assertThat(body.get("resolvedDbUsername")).isEqualTo("sa");
	}
}