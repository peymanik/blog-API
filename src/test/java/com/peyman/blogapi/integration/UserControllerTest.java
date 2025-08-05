package com.peyman.blogapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peyman.blogapi.dto.auth.TokenResponse;
import com.peyman.blogapi.dto.model.BlogRequest;
import com.peyman.blogapi.dto.model.PostRequest;
import com.peyman.blogapi.dto.model.UserRequest;
import com.peyman.blogapi.entity.Blog;
import com.peyman.blogapi.entity.Post;
import com.peyman.blogapi.entity.User;
import com.peyman.blogapi.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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

    private Long insertedUserId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


//    @Disabled
    @Test
    void accessProtectedEndpoint_withValidJwt_shouldSucceed() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        mockMvc.perform(get("/public/users/me")
                        .with(user("fakeUser").roles("Manager"))) // simulates an authenticated user with role USER
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUser"));
    }

//    @Disabled
    @Test
    void accessProtectedEndpoint_withoutJwt_shouldFail() throws Exception {
        mockMvc.perform(get("/public/users/me"))
                .andExpect(status().isUnauthorized());
    }

//    @Disabled
    @Test
    void deleteCurrentUser_shouldReturnId() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        mockMvc.perform(delete("/public/users/me")
                        .with(user("fakeUser").roles("Manager")))
                .andExpect(status().isOk())
                .andExpect(content().string("1")); //deletion successful
    }

//    @Disabled
    @Test
    void GetCurrentUser_withCorrectToken_shouldBeCorrect() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);

        User testPrint = userRepository.findByUserName("fakeUser");

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

        //get cookies
        List<String> setCookieHeaders = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

        // Find the XSRF-TOKEN cookie
        String xsrfCookieValue = setCookieHeaders.stream()
                .filter(header -> header.startsWith("XSRF-TOKEN="))
                .map(header -> header.split("=")[1].split(";")[0]) // get just the value
                .findFirst()
                .orElseThrow();

        // Create a javax.servlet.http.Cookie manually
        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", xsrfCookieValue);

        //Use token in Authorization header and xsrf in cookie
        mockMvc.perform(get("/public/users/me")
                        .cookie(xsrfCookie)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUser"));
    }

//    @Disabled
    @Test
    void GetCurrentUser_withWrongToken_shouldFail() throws Exception {
        mockMvc.perform(get("/public/users/me")
                        .header("Authorization", "Bearer " + "wrongToken"))
                .andExpect(status().isUnauthorized());

    }

//    @Disabled
    @Test
    void updateCurrentUser_shouldReturnUpdatedInfo() throws Exception {
        UserRequest request = new UserRequest(insertedUserId, "fakeUserNew", "fakePassNew");
        String requestJson =  objectMapper.writeValueAsString(request);
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        mockMvc.perform(put("/public/users/me")
                        .with(user("fakeUser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("fakeUserNew"));

    }

//    @Disabled
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

//    @Disabled
    @Test
    void getBlogById_ShouldReturnBlogResponse() throws Exception {
        User user = testDataFactory.createUserObject("blogOwner", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeTitle");
        blog.setUser(savedUser);
//        Blog savedBlog =  blogRepository.save(blog);
        Blog savedBlog = testDataFactory.saveBlog(blog);

        mockMvc.perform(get("/public/users/blog")
                        .with(user("fakeUser").roles("ADMIN"))
                        .param("id", String.valueOf(savedBlog.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("fakeTitle"));
    }

    @Test
    void getListOfBlogsOfCurrentUser_ShouldReturnBlogList() throws Exception {
        User user = testDataFactory.createUserObject("blogOwner", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog firstBlog = testDataFactory.createBlog("fistFakeBlogTitle");
        Blog secondBlog = testDataFactory.createBlog("secondFakeBlogTitle");
        firstBlog.setUser(savedUser);
        secondBlog.setUser(savedUser);
        List<Blog> blogList = Arrays.asList(firstBlog, secondBlog);
        user.setBlogs(blogList);
        Blog savedFirstBlog =  testDataFactory.saveBlog(firstBlog);
        Blog savedSecondBlog =  testDataFactory.saveBlog(secondBlog);

        mockMvc.perform(get("/public/users/me/blogs")
                        .with(user("blogOwner").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("fistFakeBlogTitle"))
                .andExpect(jsonPath("$[1].title").value("secondFakeBlogTitle"));

    }


    @Test
    void getListOfBlogsOfUserById_ShouldReturnBlogList() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog firstBlog = testDataFactory.createBlog("fistFakeBlogTitle");
        Blog secondBlog = testDataFactory.createBlog("secondFakeBlogTitle");
        firstBlog.setUser(user);
        secondBlog.setUser(user);
        List<Blog> blogList = Arrays.asList(firstBlog, secondBlog);
        user.setBlogs(blogList);
        Blog savedFirstBlog =  testDataFactory.saveBlog(firstBlog);
        Blog savedSecondBlog =  testDataFactory.saveBlog(secondBlog);

        mockMvc.perform(get("/public/users/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(savedFirstBlog.getId().toString())
                        .with(user("fakeCurrentUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("fistFakeBlogTitle"))
                .andExpect(jsonPath("$[1].title").value("secondFakeBlogTitle"));
    }


    @Test
    void getListOfAll_ShouldReturnBlogListOfAllBlogs() throws Exception {
        //first user with 2 blogs
        User firstUser = testDataFactory.createUserObject("fistFakeUser", "fakePass");
        User savedFisrtUser = testDataFactory.saveUser(firstUser);
        Blog firstBlog = testDataFactory.createBlog("fistFakeBlogTitle");
        Blog secondBlog = testDataFactory.createBlog("secondFakeBlogTitle");
        firstBlog.setUser(firstUser);
        secondBlog.setUser(firstUser);
        List<Blog> firstBlogList = Arrays.asList(firstBlog, secondBlog);
        firstUser.setBlogs(firstBlogList);
        Blog savedFirstBlog =  testDataFactory.saveBlog(firstBlog);
        Blog savedSecondBlog =  testDataFactory.saveBlog(secondBlog);

        //second user with 2 blogs
        User secondUser = testDataFactory.createUserObject("secondFakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(secondUser);
        Blog thirdBlog = testDataFactory.createBlog("thirdFakeBlogTitle");
        Blog forthBlog = testDataFactory.createBlog("forthFakeBlogTitle");
        thirdBlog.setUser(secondUser);
        forthBlog.setUser(secondUser);
        List<Blog> secondBlogList = Arrays.asList(firstBlog, secondBlog);
        secondUser.setBlogs(secondBlogList);
        Blog savedthirdBlog =  testDataFactory.saveBlog(thirdBlog);
        Blog savedForthBlog =  testDataFactory.saveBlog(forthBlog);

        mockMvc.perform(get("/public/users/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(savedFirstBlog.getId().toString())
                        .with(user("fakeCurrentUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].title").value("fistFakeBlogTitle"))
                .andExpect(jsonPath("$[1].title").value("secondFakeBlogTitle"))
                .andExpect(jsonPath("$[2].title").value("thirdFakeBlogTitle"))
                .andExpect(jsonPath("$[3].title").value("forthFakeBlogTitle"));
    }

    @Test
    void updateBlogIfUserIsOwner_shouldSucceed() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);

        BlogRequest request = new BlogRequest(savedBlog.getId(), "newFakeBlogTitle");
        String requestJson =  objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/public/users/blogs")
                        .with(user("fakeUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("newFakeBlogTitle"));
    }

    @Test
    void updateBlogIfUserIsNotOwner_shouldFail() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);

        BlogRequest request = new BlogRequest(savedBlog.getId(), "newFakeBlogTitle");
        String requestJson =  objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/public/users/blogs")
                        .with(user("wrongUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isEmpty());
    }

    @Test
    void getAllPostsOfBlogByIdOfBlog_shouldReturnPostList() throws Exception {
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

        mockMvc.perform(get("/public/users/blogs/posts")
                        .with(user("fakeUser"))
                        .param("blogId", savedBlog.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("fakeTitleOfFirstPost"))
                .andExpect(jsonPath("$[1].title").value("fakeTitleOfSecondPost"));
    }


    @Test
    void getPostsById_shouldReturnPost() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);
        Post post = testDataFactory.createPost("fakeTitle", "fakeContent");
        post.setBlog(savedBlog);
        List<Post> posts = Arrays.asList(post);
        savedBlog.setPosts(posts);
        Post savedPost = testDataFactory.savePost(post);

        mockMvc.perform(get("/public/users/blogs/post")
                        .with(user("fakeUser"))
                        .param("postId", savedPost.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("fakeTitle"))
                .andExpect(jsonPath("$.content").value("fakeContent"));
    }

    @Test
    void deletePostByIdWhenUserIsOwner_shouldSucceed() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);
        Post post = testDataFactory.createPost("fakeTitle", "fakeContent");
        post.setBlog(savedBlog);
        List<Post> posts = Arrays.asList(post);
        savedBlog.setPosts(posts);
        Post savedPost = testDataFactory.savePost(post);

        mockMvc.perform(delete("/public/users/blogs/posts")
                        .with(user("fakeUser"))
                        .param("postId", savedPost.getId().toString()))
                .andExpect(status().isOk());

    }

//    @Test
//    void deletePostByIdWhenNotOwner_shouldFail() throws Exception {
//        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
//        User savedUser = testDataFactory.saveUser(user);
//        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
//        blog.setUser(savedUser);
//        List<Blog> blogs = Arrays.asList(blog);
//        savedUser.setBlogs(blogs);
//        Blog savedBlog =  testDataFactory.saveBlog(blog);
//        Post post = testDataFactory.createPost("fakeTitle", "fakeContent");
//        post.setBlog(savedBlog);
//        List<Post> posts = Arrays.asList(post);
//        savedBlog.setPosts(posts);
//        Post savedPost = testDataFactory.savePost(post);
//
//        mockMvc.perform(delete("/public/users/blogs/posts")
//                        .with(user("wrongUser"))
//                        .param("postId", savedPost.getId().toString()))
//                .andExpect(status().);
//
//    }

    @Test
    void updatePostById_WhenUserIsOwner_shouldSucceed() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);
        Post post = testDataFactory.createPost("fakeTitle", "fakeContent");
        post.setBlog(savedBlog);
        List<Post> posts = Arrays.asList(post);
        savedBlog.setPosts(posts);
        Post savedPost = testDataFactory.savePost(post);

        PostRequest request = new PostRequest(savedPost.getId(), "newFakeTitle", "newFakeContent");
        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/public/users/blogs/posts")
                        .with(user("fakeUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("newFakeTitle"))
                .andExpect(jsonPath("$.content").value("newFakeContent"));
    }


//    @Test
//    void updatePostById_WhenUserIsNotOwner_shouldFail() throws Exception {
//        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
//        User savedUser = testDataFactory.saveUser(user);
//        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
//        blog.setUser(savedUser);
//        List<Blog> blogs = Arrays.asList(blog);
//        savedUser.setBlogs(blogs);
//        Blog savedBlog =  testDataFactory.saveBlog(blog);
//        Post post = testDataFactory.createPost("fakeTitle", "fakeContent");
//        post.setBlog(savedBlog);
//        List<Post> posts = Arrays.asList(post);
//        savedBlog.setPosts(posts);
//        Post savedPost = testDataFactory.savePost(post);
//
//        mockMvc.perform(delete("/public/users/blogs/posts")
//                        .with(user("fakeUser"))
//                        .param("postId", savedPost.getId().toString()))
//                .andExpect(status().isOk());
//
//    }

    @Test
    void createPOstbyBlogId_WhenUserIsOwnerOfBlog_shouldSucceed() throws Exception {
        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
        User savedUser = testDataFactory.saveUser(user);
        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
        blog.setUser(savedUser);
        List<Blog> blogs = Arrays.asList(blog);
        savedUser.setBlogs(blogs);
        Blog savedBlog =  testDataFactory.saveBlog(blog);

        PostRequest request = new PostRequest(null, "newFakeTitle", "newFakeContent");
        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/public/users/blogs/posts")
                        .with(user("fakeUser"))
                        .param("blogId", savedBlog.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("newFakeTitle"))
                .andExpect(jsonPath("$.content").value("newFakeContent"));
    }

//    @Test
//    void createPOstbyBlogId_WhenUserIsNotOwnerOfBlog_shouldFail() throws Exception {
//        User user = testDataFactory.createUserObject("fakeUser", "fakePass");
//        User savedUser = testDataFactory.saveUser(user);
//        Blog blog = testDataFactory.createBlog("fakeBlogTitle");
//        blog.setUser(savedUser);
//        List<Blog> blogs = Arrays.asList(blog);
//        savedUser.setBlogs(blogs);
//        Blog savedBlog =  testDataFactory.saveBlog(blog);
//        Post post = testDataFactory.createPost("fakeTitle", "fakeContent");
//        post.setBlog(savedBlog);
//        List<Post> posts = Arrays.asList(post);
//        savedBlog.setPosts(posts);
//        Post savedPost = testDataFactory.savePost(post);
//
//        PostRequest request = new PostRequest(savedPost.getId(), "newFakeTitle", "newFakeContent");
//        String requestJson = objectMapper.writeValueAsString(request);
//        mockMvc.perform(put("/public/users/blogs/posts")
//                        .with(user("fakeUser"))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("newFakeTitle"))
//                .andExpect(jsonPath("$.content").value("newFakeContent"));
//    }



}


