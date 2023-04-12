package com.yemi.secureme2.controllers;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yemi.secureme2.Entities.UserEntity;
import com.yemi.secureme2.repositories.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/home")
public class HomeController {
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;

    @Autowired
    public HomeController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/hello")
	public String hello(){
		return "hello";
	}

    @PostMapping("/login")
	public String login(@RequestBody UserEntity user){
		String username = user.getUsername();
		String token;
		try {
			user.setPassword(passwordEncoder.encode(user.getPassword()));

		userRepository.save(user);

		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		token = Jwts.builder()
						.claim("username", username)
						.setIssuedAt(new Date(System.currentTimeMillis()))
						.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
						.signWith(key)
						.compact();
		} catch (Exception e) {
			// sends developer a message for prod send 500 response.
			return e.getMessage();
		}
		

		return token;
	}
}
