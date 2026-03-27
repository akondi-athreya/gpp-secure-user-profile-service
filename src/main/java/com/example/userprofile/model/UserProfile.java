package com.example.userprofile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

	@Id
	@Column(name = "user_id", nullable = false, updatable = false)
	@NotBlank(message = "userId is required")
	private String userId;

	@Column(nullable = false)
	@NotBlank(message = "username is required")
	private String username;

	@Column(nullable = false)
	@NotBlank(message = "email is required")
	@Email(message = "email must be valid")
	private String email;

	public UserProfile() {
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}