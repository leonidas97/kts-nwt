package com.mbooking.controller;

import com.mbooking.dto.LoginRequestDTO;
import com.mbooking.dto.UserDTO;
import com.mbooking.exception.ApiAuthException;
import com.mbooking.model.Customer;
import com.mbooking.model.User;
import com.mbooking.repository.CustomerRepository;
import com.mbooking.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_h2")
public class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Test
    public void when_RegisterWithUnusedEmail_Success() {
        String email = "ivkovicdjordje1997@gmail.com";
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setPassword("123");
        userDTO.setFirstname("djole");
        userDTO.setLastname("ivkovic");

        ResponseEntity<UserDTO> response = testRestTemplate.postForEntity("/api/users/register",userDTO, UserDTO.class);
        UserDTO registeredDTO = response.getBody();
        Assert.assertEquals(userDTO.getEmail(), registeredDTO.getEmail());

        LoginRequestDTO loginDTO = new LoginRequestDTO(email, "123");
        ResponseEntity<ApiAuthException> loginResponse = testRestTemplate.postForEntity("/api/auth/login", loginDTO, ApiAuthException.class);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());

        Customer user = customerRepo.findByEmail(email);
        ResponseEntity<String> confirmResponse = testRestTemplate.exchange(
                "/api/users/confirm_registration/" + email + "/" + user.getEmailConfirmationId(),
                HttpMethod.PUT,
                null,
                String.class);
        user = customerRepo.findByEmail(email);
        Assert.assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());
        Assert.assertEquals(true, user.isEmailConfirmed());
        System.out.println(confirmResponse.getBody());

        loginDTO = new LoginRequestDTO(email, "123");
        ResponseEntity<UserDTO> succloginResponse = testRestTemplate.postForEntity("/api/auth/login", loginDTO, UserDTO.class);
        Assert.assertEquals(HttpStatus.OK, succloginResponse.getStatusCode());
    }
}
