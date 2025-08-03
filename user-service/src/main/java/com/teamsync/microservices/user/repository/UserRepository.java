package com.teamsync.microservices.user.repository;

import com.teamsync.microservices.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    public Users findByEmail(String email);
}



