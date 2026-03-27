package com.example.userprofile.service;

import com.example.userprofile.model.UserProfile;
import com.example.userprofile.repository.UserProfileRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

	private final UserProfileRepository userProfileRepository;

	public UserProfileService(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

	public UserProfile createProfile(UserProfile userProfile) {
		return userProfileRepository.save(userProfile);
	}

	public Optional<UserProfile> getProfileById(String userId) {
		return userProfileRepository.findById(userId);
	}
}