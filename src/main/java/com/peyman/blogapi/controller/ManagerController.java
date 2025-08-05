package com.peyman.blogapi.controller;


import com.peyman.blogapi.dto.model.PostResponse;
import com.peyman.blogapi.dto.model.UserResponse;
import com.peyman.blogapi.service.BlogService;
import com.peyman.blogapi.service.PostService;
import com.peyman.blogapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@Secured("ROLE_MANAGER")
@RequestMapping("/manager")  //add base path
public class ManagerController {
    private final UserService userService;
    private final BlogService blogService;
    private final PostService postService;

//    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/users/all")
    public List<UserResponse> getListOfAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users")
    public UserResponse getUserById(@RequestParam Long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("/users")
    public void deleteUserById(@RequestParam Long userId) {
        userService.deleteUserById(userId);
    }

    @DeleteMapping("/users/blogs")
    public void deleteBlogById(@RequestParam Long blogId) {
        blogService.deleteBlogById(blogId);
    }

    @GetMapping("/users/blogs/posts")
    public List<PostResponse> getAllPosts() {
        return postService.getAllPosts();
    }

    @DeleteMapping("/users/blogs/posts")
    public void deletePostById(@RequestParam Long postId) {
        postService.deletePostById(postId);
    }

}