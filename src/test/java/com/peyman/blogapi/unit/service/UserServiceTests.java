package com.peyman.blogapi.unit.service;

import com.peyman.blogapi.entity.dto.UserResponse;
import com.peyman.blogapi.entity.model.User;
import com.peyman.blogapi.entity.repository.UserRepository;
import com.peyman.blogapi.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import wiremock.com.google.common.base.Optional;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @Disabled
    void testFindById_whenUserExists_returnsUser() {
        User user = new User("peyman", "123", 5L);

        //When the findById(5) method is called, return an Optional containing the user you just created.This replaces real DB access with your fake user
        when(userRepository.findById(5)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(5);

        assertThat(result.getUserName()).isEqualTo("peyman");
    }
}

