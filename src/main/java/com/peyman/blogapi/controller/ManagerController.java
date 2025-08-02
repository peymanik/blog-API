package com.peyman.blogapi.controller;


import com.peyman.blogapi.service.BlogService;
import com.peyman.blogapi.service.PostService;
import com.peyman.blogapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")  //add base path
public class ManagerController {
    private final UserService userService;
    private final BlogService blogService;
    private final PostService postService;


//    @PreAuthorize("hasRole('MANAGER')")
//    @PostMapping("/users")
//    public User createUser(@Valid @RequestBody UserRequest request) {
//        request.setId(0);
//        User user = new User();
//        user.setUserName(request.getUsername());
//        user.setPassword(request.getPassword());
//        user.setId(request.getId());
//        return userService.saveUser(user);
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/users")
//    public List<User> getAllUsers() {
//        return userService.getAllUsers();
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/user")
//    public User getUserById(@RequestParam Integer userId) {
//        return userService.getUserById(userId);
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @DeleteMapping("/users")
//    public void deleteUserById(@RequestParam Integer userId) {
//        userService.deleteUserById(userId);
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/users/blog")
//    public Blog getBlogById(@RequestParam Integer blogId) {
//        return blogService.getBlogById(blogId);
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/user/blogs")
//    public List<Blog> getAllBlogsOfAUser(@RequestParam Integer userId) {
//        User user = userService.getUserById(userId);
//        return user.getBlogs();
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @DeleteMapping("/users/blogs")
//    public void deleteBlogById(@RequestParam Integer blogId) {
//        blogService.deleteBlogById(blogId);
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/users/blogs")
//    public List<Blog> getAllBlogs() {
//        return blogService.getAllBlogs();
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/users/blogs/posts")
//    public List<Post> getAllPosts() {
//        return postService.getAllPosts();
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @DeleteMapping("/users/blogs/posts")
//    public void deletePostById(@RequestParam Integer postId) {
//        postService.deletePostById(postId);
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/users/blogs/post")
//    public Post getPostById(@RequestParam Integer postId) {
//        Post post = postService.getPostById(postId);
//        post.setViews(post.getViews() + 1);
//        return post;
//    }

}