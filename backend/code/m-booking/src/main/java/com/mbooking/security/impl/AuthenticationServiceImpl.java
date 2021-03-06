package com.mbooking.security.impl;

import com.mbooking.dto.UserDTO;
import com.mbooking.exception.ApiAuthException;
import com.mbooking.model.Customer;
import com.mbooking.model.User;
import com.mbooking.repository.AuthorityRepository;
import com.mbooking.repository.CustomerRepository;
import com.mbooking.repository.UserRepository;
import com.mbooking.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private CustomerRepository customerRepo;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthorityRepository authorityRepo;

	@Override
	public UserDTO login(String email, String password) {
		UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(email, password);

		Authentication auth;
		try {
			auth = authManager.authenticate(loginToken);
		} catch (BadCredentialsException ex) {
			throw new ApiAuthException("Invalid credentials.");
		}

		checkUserRestrictions(auth.getName());

		String token = jwtUtils.generateToken(auth.getName());
		SecurityContextHolder.getContext().setAuthentication(auth);
		UserDTO user = new UserDTO(userRepo.findByEmail(auth.getName()));
		user.setToken(token);
		return user;
	}

	private void checkUserRestrictions(String email) {
		User user = userRepo.findByEmail(email);
		if (user != null) {
			if (!user.isEmailConfirmed()) {
				throw new ApiAuthException("Please confirm registration!");
			}

			if (user.isBanned()) {
				throw new ApiAuthException("Your account is banned!");
			}
		}
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
			throw new ApiAuthException("Please enter current password to verify ownership of account.");
		}
	}

}