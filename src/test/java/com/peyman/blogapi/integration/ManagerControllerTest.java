package com.peyman.blogapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peyman.blogapi.dto.model.PostRequest;
import com.peyman.blogapi.entity.Blog;
import com.peyman.blogapi.entity.Post;
import com.peyman.blogapi.entity.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

//import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static sun.nio.ch.DefaultSelectorProvider.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;




@Slf4j
@SpringBootTest //loads the full application context
@AutoConfigureMockMvc
@Transactional
public class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_WhenUserHasManagerRole_shouldReturnList() throws Exception {
        User user1 = testDataFactory.createUserObject("fakeUser1", "fakePass");
        User savedUser1 = testDataFactory.saveUser(user1);
        User user2 = testDataFactory.createUserObject("fakeUser2", "fakePass");
        User savedUser2 = testDataFactory.saveUser(user2);

        mockMvc.perform(get("/manager/users/all")
                        .with(user("fakeUser").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNumber());
    }

    @Test
    void getAllUsers_WhenUserDoesntHaveManagerRole_shouldFail() throws Exception {
        User user1 = testDataFactory.createUserObject("fakeUser1", "fakePass");
        User savedUser1 = testDataFactory.saveUser(user1);
        User user2 = testDataFactory.createUserObject("fakeUser2", "fakePass");
        User savedUser2 = testDataFactory.saveUser(user2);

        mockMvc.perform(get("/manager/users/all")
                        .with(user("fakeUser")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);

        mockMvc.perform(get("/manager/users")
                        .with(user("fakeUser").roles("MANAGER"))
                        .param("userId", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUser"));
    }

    @Test
    void deleteUserById_shouldSuccess() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);

        mockMvc.perform(delete("/manager/users")
                        .with(user("fakeUser").roles("MANAGER"))
                        .param("userId", savedUser.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBlogById_shouldSuccess() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);

        mockMvc.perform(delete("/manager/users")
                        .with(user("fakeUser").roles("MANAGER"))
                        .param("userId", savedUser.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getAllPost_shouldReturnListOfPosts() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);

        Post firstPost = testDataFactory.createPost("fakeTitleOfFirstPost", "fakeContent");
        Post secondPost = testDataFactory.createPost("fakeTitleOfSecondPost", "fakeContent");
        firstPost.setBlog(savedBlog);
        secondPost.setBlog(savedBlog);
        List<Post> posts = Arrays.asList(firstPost, secondPost);
        savedBlog.setPosts(posts);
        Post savedFistPost = testDataFactory.savePost(firstPost);
        Post savedSecondPost = testDataFactory.savePost(secondPost);

        mockMvc.perform(get("/manager/users/blogs/posts")
                        .with(user("fakeUser").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNumber());
    }

    @Test
    void deletePostById_shouldSuccess() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);

        Post post = testDataFactory.createPost("fakeTitleOfFirstPost", "fakeContent");
        post.setBlog(savedBlog);
        List<Post> posts = Arrays.asList(post);
        savedBlog.setPosts(posts);
        Post savedPost = testDataFactory.savePost(post);

        mockMvc.perform(delete("/manager/users/blogs/posts")
                        .with(user("fakeUser").roles("MANAGER"))
                        .param("postId", savedUser.getId().toString()))
                .andExpect(status().isOk());
    }

}
