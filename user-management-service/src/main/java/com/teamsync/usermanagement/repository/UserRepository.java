package com.teamsync.usermanagement.repository;

import com.teamsync.usermanagement.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    public Users findByEmail(String email);

}