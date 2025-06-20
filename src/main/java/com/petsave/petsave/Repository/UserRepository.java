package com.petsave.petsave.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.petsave.petsave.Entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
