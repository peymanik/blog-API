package com.peyman.blogapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peyman.blogapi.dto.model.UserRequest;
import com.peyman.blogapi.dto.model.UserResponse;
import com.peyman.blogapi.entity.User;
import com.peyman.blogapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {


    private  final UserRepository userRepository;
    private final  ModelMapper modelMapper;
    private  final ObjectMapper objectMapper;


    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUserName(username);
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        return userResponse;
    }

    public User getUserEntityByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return modelMapper.map(user, UserResponse.class);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users =  userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Integer deleteUserByUsername(String username) {
        return userRepository.deleteByUserName((username));
    }

    public UserResponse updateUser(UserRequest userRequest) {
        User user = modelMapper.map(userRequest, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

}