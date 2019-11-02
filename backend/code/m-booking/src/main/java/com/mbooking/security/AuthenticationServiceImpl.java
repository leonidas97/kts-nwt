package com.mbooking.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mbooking.dto.UserDTO;
import com.mbooking.exception.ApiAuthException;
import com.mbooking.model.User;
import com.mbooking.repository.UserRepository;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDTO login(String email, String password) {
		UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(email, password);
		
		Authentication auth;
		try { 
			auth = authManager.authenticate(loginToken);
		} catch (BadCredentialsException ex) { 
			throw new ApiAuthException();
		}
		
		String token = jwtUtils.generateToken(auth.getName());
		SecurityContextHolder.getContext().setAuthentication(auth);		
		UserDTO user = new UserDTO(userRepo.findByEmail(auth.getName()));
		user.setToken(token);
		return user;
	}
	
	@Override
	public void changePassword(String newPassword, String oldPassword) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepo.findByEmail(email);
		
		if (passwordEncoder.matches(oldPassword, user.getPassword())) {
			newPassword = passwordEncoder.encode(newPassword);
			user.setPassword(newPassword);
			userRepo.save(user);
		} else {
			throw new ApiAuthException();
		}
	}
}
