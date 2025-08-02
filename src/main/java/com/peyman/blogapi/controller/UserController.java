package com.peyman.blogapi.controller;

import com.peyman.blogapi.entity.dto.*;
import com.peyman.blogapi.entity.model.Blog;
import com.peyman.blogapi.entity.model.Post;
import com.peyman.blogapi.entity.model.User;
import com.peyman.blogapi.service.BlogService;
import com.peyman.blogapi.service.PostService;
import com.peyman.blogapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;



@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/public")  //add base path
public class UserController {
    private final UserService userService;
    private final BlogService blogService;
    private final PostService postService;


    @GetMapping("/users/me")
    public UserResponse getCurrentUser(Principal principal) {
        String username = principal.getName();
        try {
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    @DeleteMapping("/users/me")
    public Integer deleteCurrentUser(Principal principal) {
        String username = principal.getName();
        return userService.deleteUserByUsername(username);
    }

    @PutMapping("/users/me")
    public UserResponse updateCurrentUser(@Valid @RequestBody UserRequest request, Principal principal, Authentication authentication) {
        UserResponse userresponse = userService.getUserByUsername(authentication.getName());
        request.setId(userresponse.getId());
        return userService.updateUser(request);
    }

    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
//        request.setId(0L);
        return userService.updateUser(request);
    }

    @GetMapping("/users/blog")
    public BlogResponse getBlogById(@RequestParam Integer id) {
        return blogService.getBlogById(id);
    }

    @GetMapping("/users/me/blogs")
    public List<BlogResponse> getAllUserBlogs(Principal principal) {
        String username = principal.getName();
        return blogService.getAllUserBlogs(username);
    }

    @GetMapping("/user/blogs")
    public List<BlogResponse> getAllBlogsOfAUser(@RequestParam Integer userId) {
        UserResponse user = userService.getUserById(userId);
        return blogService.getAllUserBlogs(user.getUserName());
    }

    @GetMapping("/users/blogs")
    public List<BlogResponse> getAllBlogs() {
        return blogService.getAllBlogs();
    }


    @PutMapping("/users/blogs")
    public BlogResponse updateBlog(@RequestBody BlogRequest request, Principal principal) {
        String username = principal.getName();
        Blog blog = blogService.getBlogByTitle(request.getTitle());
        if (blog.getUser().getUserName() == username) {
            return blogService.updateBlog(request);
        }
        return null;
    }

    @GetMapping("/users/blogs/posts")
    public List<PostResponse> getAllPostsOfBlog(@RequestParam Integer blogId) {
        return postService.getAllPostsOfBlog(blogId);
    }

    @GetMapping("/users/blogs/post")
    public PostResponse getPostById(@RequestParam Integer postId) {
        return postService.getPostById(postId);
        //increase view of post??
    }

    @DeleteMapping("/users/blogs/posts")
    public void deletePostById(@RequestParam Integer postId, Principal principal) {
        String username = principal.getName();
        Post post = postService.getPostEntityById(postId);
        if (post.getBlog().getUser().getUserName() == username) {
            postService.deletePostById(postId);
        }
    }

    @PutMapping("/users/blogs/posts")
    public PostResponse updatePost(@Valid @RequestBody PostRequest request, Principal principal) {
        String username = principal.getName();
        Post post = postService.getPostEntityById(request.getId());
        if (post.getBlog().getUser().getUserName() == username) {
            return postService.updatePost(request);
        }
        else return null;
    }

    @PostMapping("/users/blogs/posts")
    public PostResponse createPost(@Valid @RequestBody PostRequest request, @RequestParam Integer blogId, Principal principal) {
        String username = principal.getName();
        Blog blog = blogService.getBlogEntityById(blogId);
        request.setId(0);
        if (blog.getUser().getUserName() == username) {
            return postService.updatePost(request);
        }
        else return null;
    }

}