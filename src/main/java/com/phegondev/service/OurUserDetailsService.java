package com.phegondev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.phegondev.repository.UsersRepo;

@Service
public class OurUserDetailsService implements UserDetailsService {

	@Autowired
	private UsersRepo usersRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO 自動生成されたメソッド・スタブ
		return usersRepo.findByEmail(username).orElseThrow();
	}

}
