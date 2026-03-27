package com.example.userprofile.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final Key signingKey;

	public JwtService(@Value("${api.signing-key:${API_SIGNING_KEY:abcdefghijklmnopqrstuvwxyz123456}}") String signingKeyValue) {
		this.signingKey = Keys.hmacShaKeyFor(signingKeyValue.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String userId) {
		Date now = new Date();
		return Jwts.builder()
				.subject(userId)
				.issuedAt(now)
				.signWith(signingKey)
				.compact();
	}
}