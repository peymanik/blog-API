package com.peyman.blogapi.integration;

import com.peyman.blogapi.entity.model.Blog;
import com.peyman.blogapi.entity.model.Post;
import com.peyman.blogapi.entity.model.User;
import com.peyman.blogapi.entity.repository.BlogRepository;
import com.peyman.blogapi.entity.repository.PostRepository;
import com.peyman.blogapi.entity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestDataFactory {

    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final PostRepository postRepository;

//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
//    private final PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordEncoder passwordEncoder;


    //CREATE USER
    public User createUserObject(String username, String rawPassword) {
        User user = new User();
        user.setUserName(username);
//        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPassword(rawPassword);
        return user;
    }

    // CREATE BLOG
    public Blog createBlog(String title) {
        Blog blog = new Blog();
        blog.setTitle(title);
//        blog.setUser(user);
        return blog;
    }

    // CREATE POST
    public Post createPost(String title, String content) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setLikes(0L);
        post.setViews(0L);
        return post;
    }

    public User saveUser(User user){
        return userRepository.save(user);
    }

    public Blog saveBlog(Blog blog){
        return blogRepository.save(blog);
    }

    public Post savePost(Post post){
        return postRepository.save(post);
    }

}
