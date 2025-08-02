package com.peyman.blogapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peyman.blogapi.entity.dto.UserRequest;
import com.peyman.blogapi.entity.dto.UserResponse;
import com.peyman.blogapi.entity.model.User;
import com.peyman.blogapi.entity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


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

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        return modelMapper.map(user, UserResponse.class);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Integer deleteUserByUsername(String username) {
        return userRepository.deleteByUserName((username));
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }

    public UserResponse updateUser(UserRequest userRequest) {
//        User user = objectMapper.convertValue(userRequest, User.class);
//        User savedUser = userRepository.save(user);
//        return objectMapper.convertValue(savedUser, UserResponse.class);

//        User user = objectMapper.convertValue(userRequest, User.class);
        User user = modelMapper.map(userRequest, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

}