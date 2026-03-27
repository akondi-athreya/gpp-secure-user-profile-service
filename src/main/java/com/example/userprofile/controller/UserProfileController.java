package com.example.userprofile.controller;

import com.example.userprofile.model.UserProfile;
import com.example.userprofile.service.JwtService;
import com.example.userprofile.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class UserProfileController {

	private static final String SECRET_PATH = "secret/user-profile-service";

	private final UserProfileService userProfileService;
	private final JwtService jwtService;
	private final String resolvedDbUsername;

	public UserProfileController(
			UserProfileService userProfileService,
			JwtService jwtService,
			@Value("${db.username}") String resolvedDbUsername) {
		this.userProfileService = userProfileService;
		this.jwtService = jwtService;
		this.resolvedDbUsername = resolvedDbUsername;
	}

	@PostMapping("/profile")
	public ResponseEntity<UserProfile> createProfile(@Valid @RequestBody UserProfile userProfile) {
		UserProfile savedProfile = userProfileService.createProfile(userProfile);
		String token = jwtService.generateToken(savedProfile.getUserId());

		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.body(savedProfile);
	}

	@GetMapping("/profile/{userId}")
	public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
		return userProfileService.getProfileById(userId)
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
	}

	@GetMapping("/admin/vault-status")
	public Map<String, Object> getVaultStatus() {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("vaultConnected", true);
		response.put("secretPath", SECRET_PATH);
		response.put("resolvedDbUsername", resolvedDbUsername);
		return response;
	}
}