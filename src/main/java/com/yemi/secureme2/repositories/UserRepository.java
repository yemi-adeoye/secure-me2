package com.yemi.secureme2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.yemi.secureme2.Entities.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, Integer>{
    public UserEntity findByEmail(String email);
}
