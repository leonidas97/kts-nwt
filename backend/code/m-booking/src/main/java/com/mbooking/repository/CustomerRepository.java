package com.mbooking.repository;

import com.mbooking.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long>{
	Customer findByEmail(String email);
	Page<Customer> findByFirstnameContainingAndLastnameContainingAndEmailContaining(String firstname, String lastname, String email, Pageable pageable);
}
