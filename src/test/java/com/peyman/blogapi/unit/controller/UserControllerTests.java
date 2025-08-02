package com.peyman.blogapi.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peyman.blogapi.controller.UserController;
import com.peyman.blogapi.dto.TokenResponse;
import com.peyman.blogapi.entity.dto.UserRequest;
import com.peyman.blogapi.entity.dto.UserResponse;
import com.peyman.blogapi.entity.repository.UserRepository;
import com.peyman.blogapi.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.*;
//import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

//order of layers?

//@AutoConfigureMockMvc
//@WebMvcTest(UserController.class)
//@Transactional //set    to rollback after DB changes
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelMapper modelMapper;

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser() throws Exception {
        UserResponse user = new UserResponse();
        user.setUserName("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);

        Principal principal = () -> "testUser";

        mockMvc.perform(get("/public/users/me")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) //Asserts that the HTTP status of the response is 200 OK
                .andExpect(jsonPath("$.username").value("user1")); //Asserts that the JSON body contains a field name with the value "Alice"
    }


////  controller post
//    @Test
////    @Disabled
//    void testCreatUser() throws Exception {
//        UserRequest request = new UserRequest(7, "alipoya", "5646");
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        mockMvc.perform(post("/public/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userName").value("alipoya"));
//    }
//
    //service method update:
//    @Test
//    @Disabled
//    void testGetCurrentUser() throws Exception {
//        // Step 1: Login and get token
//        String loginJson = """
//        {
//            "username": "peyman",
//            "password": "123"
//        }
//        """;
//
//        MvcResult result = mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(loginJson))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String response = result.getResponse().getContentAsString();
//        TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
//        String token = tokenResponse.getAccessToken();
//
//        // Step 2: Use token in Authorization header
//        mockMvc.perform(get("/public/users/me")
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userName").value("peyman"));
//    }




}
