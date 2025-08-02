package com.peyman.blogapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peyman.blogapi.dto.TokenResponse;
import com.peyman.blogapi.entity.dto.UserRequest;
import com.peyman.blogapi.entity.model.Blog;
import com.peyman.blogapi.entity.model.Post;
import com.peyman.blogapi.entity.model.Role;
import com.peyman.blogapi.entity.model.User;
import com.peyman.blogapi.entity.repository.BlogRepository;
import com.peyman.blogapi.entity.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest //loads the full application context
@AutoConfigureMockMvc //Spring Boot sets up MockMvc with the full Spring MVC infrastructure(controllers, filters, interceptors, message converters, exception handlers, and validation run as if in a real server)
@Transactional //rolls back DB changes automatically, after each individual @Test method
//@ActiveProfiles("test") //to use application-test.yml for test-specific properties
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // simulate HTTP requests to your MVC controllers without starting a real HTTP server

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlogRepository blogRepository;

    private Long insertedUserId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;



    @BeforeEach
    public void insertFakeUser() {
        User user = new User();
        Role role = new Role();
        role.setName("ROLE_FAKE");
        user.setUserName("fakeUser");
        user.setPassword("fakePass");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        User insertedUser = userRepository.save(user);
    }

//    @BeforeEach
    public Blog insertFakeBlog() {
        Blog blog = new Blog();
        blog.setId(null);
        blog.setTitle("fakeTitle");
//        blog.setUser();
//        blog.setUser(new User());
//        blog.setPosts();

        return blogRepository.save(blog);
    }


    @Disabled
    @Test
    void accessProtectedEndpoint_withValidJwt_shouldSucceed() throws Exception {
        mockMvc.perform(get("/public/users/me")
                        .with(user("fakeUser").roles("Manager"))) // simulates an authenticated user with role USER
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUser"));
    }

    @Disabled
    @Test
    void accessProtectedEndpoint_withoutJwt_shouldFail() throws Exception {
        mockMvc.perform(get("/public/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Disabled
    @Test
    void deleteCurrentUser_shouldReturnId() throws Exception {
        mockMvc.perform(delete("/public/users/me")
                        .with(user("fakeUser").roles("Manager")))
                .andExpect(status().isOk())
                .andExpect(content().string("1")); //deletion successful
    }

    @Disabled
    @Test
    void GetCurrentUser_withCorrectToken_shouldBeCorrect() throws Exception {
        String loginJson = """
        {
            "username": "fakeUser",
            "password": "fakePass"
        }
        """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
        String token = tokenResponse.getAccessToken();
//
//        //Use token in Authorization header
        mockMvc.perform(get("/public/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUser"));

    }

    @Test
    @Disabled
    void GetCurrentUser_withWrongToken_shouldFail() throws Exception {
        mockMvc.perform(get("/public/users/me")
                        .header("Authorization", "Bearer " + "wrongToken"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @Disabled
    void updateCurrentUser_shouldReturnUpdatedInfo() throws Exception {
        UserRequest request = new UserRequest(insertedUserId, "fakeUserNew", "fakePassNew");
        String requestJson =  objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/public/users/me")
                        .with(user("fakeUser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUserNew"));

    }

    @Disabled
    @Test
    void createNewUser_ShouldReturnUser() throws Exception {
        UserRequest request = new UserRequest(null, "fakeNewUser", "fakeNewPass");
        String requestJson =  objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeNewUser"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Disabled
    @Test
    void getBlogById_ShouldReturnBlogResponse() throws Exception {
        User user = testDataFactory.createUserObject("blogOwner", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeTitle");
        blog.setUser(savedUser);
        Blog savedBlog =  blogRepository.save(blog);

        mockMvc.perform(get("/public/users/blog")
                        .with(user("fakeUser").roles("ADMIN"))
                        .param("id", String.valueOf(savedBlog.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("fakeTitle"));
    }

    @Test
    void getListOfBlogsOfUser_ShouldReturnBlogList() throws Exception {
        User user = testDataFactory.createUserObject("blogOwner", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog firstBlog = testDataFactory.createBlog("fistFakeBlogTitle");
        Blog secondBlog = testDataFactory.createBlog("secondFakeBlogTitle");
        firstBlog.setUser(savedUser);
        secondBlog.setUser(savedUser);
        List<Blog> blogList = Arrays.asList(firstBlog, secondBlog);
        user.setBlogs(blogList);
        Blog savedFirstBlog =  blogRepository.save(firstBlog);
        Blog savedSecondBlog =  blogRepository.save(secondBlog);

        mockMvc.perform(get("/public/users/me/blogs")
                        .with(user("blogOwner").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("fistFakeBlogTitle"))
                .andExpect(jsonPath("$[1].title").value("secondFakeBlogTitle"));

    }

//    @Disabled
//    @Test
//    void getBlogById_ShouldReturnBlog() throws Exception {
//        User user = testDataFactory.createUser("user1", "user2");
//        Blog blog = testDataFactory.createBlog(user, "blogTitle");
//        Post post = testDataFactory.createPost(blog, "postTitle", "content", 1, 1);

        //add relation:
//        user.setBlogs(List.of(blog));
//        blog.setUser(user);
//        blog.setPosts(List.of(post));
//        post.setBlog(blog);


//        UserRequest request = new UserRequest(null, "fakeNewUser", "fakeNewPass");
//        String requestJson =  objectMapper.writeValueAsString(request);
//        mockMvc.perform(get("/public/users/blog")
//                                .param("id", blog.getId().toString()))
//                .andExpect(status().isOk());
//    }


}


