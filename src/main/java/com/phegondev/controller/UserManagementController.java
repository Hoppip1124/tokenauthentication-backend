package com.phegondev.controller;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phegondev.dto.LoginResponse;
import com.phegondev.dto.ReqRes;
import com.phegondev.entity.OurUsers;
import com.phegondev.service.JWTUtils;
import com.phegondev.service.UserManagementService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserManagementController {

	@Autowired
	private UserManagementService userManagementService;

	@Autowired
	private JWTUtils jwtUtils;

	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@PostMapping("/auth/register")
	public ResponseEntity<ReqRes> register(@RequestBody ReqRes reg) {
		return ResponseEntity.ok(userManagementService.register(reg));
	}
	
	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponse> login(@RequestBody ReqRes req) {
		ReqRes response = userManagementService.login(req);

		if (response.getStatusCode() == 200) {
			UserDetails userDetails = userManagementService.createUserDetailsFromReqRes(response);
			if (userDetails == null) {
				return ResponseEntity.status(500).body(new LoginResponse(null, null, null, "UserDetailsの取得に失敗"));
			}
			String name = response.getName();
			String token = response.getToken();
			String redirectUrl = userManagementService.determineRedirectUrl(userDetails);
			return ResponseEntity.ok(new LoginResponse(name, token, redirectUrl, null));
		} else {
			return ResponseEntity.status(response.getStatusCode()).body(new LoginResponse(null, null, null, response.getMessage()));
		}
	}
	
	@PostMapping("/auth/logout")
	public ResponseEntity<?> logout(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			Date expiration = jwtUtils.getExpirationDateFromToken(token);
			
			if (expiration != null) {
				long ttl = expiration.getTime() - System.currentTimeMillis();
				redisTemplate.opsForValue().set(token, "revoked", ttl, TimeUnit.MILLISECONDS);
			}
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().build();
	}

	@GetMapping("/api/user")
	public ResponseEntity<ReqRes> getCurrentUser() {
		Authentication authetication = SecurityContextHolder.getContext().getAuthentication();
		if (authetication == null || !authetication.isAuthenticated()) {
			ReqRes errorResponse = new ReqRes();
			errorResponse.setMessage("Unauthorized");
			return ResponseEntity.status(401).body(errorResponse);
		}
		Object principal = authetication.getPrincipal();
		if (principal instanceof OurUsers) {
			OurUsers ourusers = (OurUsers)principal;
			ReqRes res = new ReqRes();
			res.setName(ourusers.getName());
			res.setEmail(ourusers.getEmail());
			res.setCity(ourusers.getCity());
			res.setRole(ourusers.getRole());
			return ResponseEntity.ok(res);
		} else {
			ReqRes errorResponse = new ReqRes();
			errorResponse.setMessage("Internal Server Error");
			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	@GetMapping("/admin/get-all-users")
	public ResponseEntity<ReqRes> getAllUsers() {
		return ResponseEntity.ok(userManagementService.getAllUsers());
	}
}
