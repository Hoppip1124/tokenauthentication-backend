package com.phegondev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phegondev.entity.OurUsers;

public interface UsersRepo extends JpaRepository<OurUsers, Integer> {

	Optional<OurUsers> findByEmail(String email);
}
