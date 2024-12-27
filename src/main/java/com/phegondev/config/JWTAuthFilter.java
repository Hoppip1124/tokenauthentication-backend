package com.phegondev.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phegondev.service.JWTUtils;
import com.phegondev.service.OurUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

	@Autowired
	private JWTUtils jwtUtils;
	
	@Autowired
	private OurUserDetailsService ourUserDetailsService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO 自動生成されたメソッド・スタブ
		final String authHeader = request.getHeader("Authorization");
		final String jwtToken;
		final String userEmail;

		final String requestURI = request.getRequestURI();

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			if (redisTemplate.hasKey(token)) {
				// トークンがブラックリストに存在する場合
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

		logger.debug("Processing request for URI: " + requestURI);
		if (requestURI.startsWith("/auth/") || requestURI.startsWith("/public/")) {
			logger.debug("Skipping JWT filter for: " + requestURI);
			filterChain.doFilter(request, response);
			return;
		}
		
		if (authHeader == null || authHeader.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}
		jwtToken = authHeader.substring(7);
		userEmail = jwtUtils.extractUsername(jwtToken);
		
		if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);
			
			if (jwtUtils.isTokenValid(jwtToken, userDetails)) {
				SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				securityContext.setAuthentication(token);
				SecurityContextHolder.setContext(securityContext);
			}
		}
		filterChain.doFilter(request, response);
	}
}
