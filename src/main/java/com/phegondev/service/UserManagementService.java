package com.phegondev.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.phegondev.dto.ReqRes;
import com.phegondev.entity.OurUsers;
import com.phegondev.repository.UsersRepo;

@Service
public class UserManagementService {

	@Autowired
	private UsersRepo usersRepo;

	@Autowired
	private JWTUtils jwtUtils;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public ReqRes register(ReqRes registrationRequest) {
		ReqRes resp = new ReqRes();
		try {
			OurUsers ourUser = new OurUsers();
			ourUser.setEmail(registrationRequest.getEmail());
			ourUser.setCity(registrationRequest.getCity());
			ourUser.setRole(registrationRequest.getRole());
			ourUser.setName(registrationRequest.getName());
			ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
			OurUsers ourUsersResult = usersRepo.save(ourUser);
			if (ourUsersResult.getId() > 0) {
				resp.setOurUsers(ourUsersResult);
				resp.setMessage("User saved successfully");
				resp.setStatusCode(200);
			}
		} catch (Exception e) {
			resp.setStatusCode(500);
			resp.setError(e.getMessage());
		}
		return resp;
	}
	
	public ReqRes login(ReqRes loginRequest) {
		ReqRes response = new ReqRes();
		try {
			authenticationManager
				.authenticate(
						new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
						loginRequest.getPassword()));
			var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
			var jwt = jwtUtils.generateToken(user);
			var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
			response.setStatusCode(200);
			response.setToken(jwt);
			response.setRefreshToken(refreshToken);
			response.setExpirationTime("24Hours");
			response.setMessage("Successfully Logged In");
			response.setName(user.getName());
			response.setRole(user.getRole());
		} catch (Exception e) {
			response.setStatusCode(500);
			response.setError(e.getMessage());
		}
		return response;
	}
	
	public ReqRes getAllUsers() {
		ReqRes reqRes = new ReqRes();
		try {
			List<OurUsers> result = usersRepo.findAll();
			if (!result.isEmpty()) {
				reqRes.setOurUsersList(result);
				reqRes.setStatusCode(200);
				reqRes.setMessage("Successful");
			} else {
				reqRes.setStatusCode(404);
				reqRes.setMessage("User not found");
			}
			return reqRes;
		} catch (Exception e) {
			reqRes.setStatusCode(500);
			reqRes.setMessage("Error occurred" + e.getMessage());
			return reqRes;
		}
	}

	public String determineRedirectUrl(UserDetails userDetails) {
		if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
			return "/admin/dashboard";
		} else {
			return "/user/dashboard";
		}	
	}

	public UserDetails createUserDetailsFromReqRes(ReqRes req) {
		try {
			String username = req.getName();
			String roles = req.getRole();
			
			if (username ==null || roles == null) {
				return null;
			}

			List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
			return new User(username, "password", authorities);
		} catch (Exception e) {
			System.err.println("UserDetails作成失敗：" + e.getMessage());
			return null;
		}
	}
}
