package com.peyman.blogapi.service;

import com.peyman.blogapi.entity.dto.BlogRequest;
import com.peyman.blogapi.entity.dto.BlogResponse;
import com.peyman.blogapi.entity.model.Blog;
import com.peyman.blogapi.entity.model.User;
import com.peyman.blogapi.entity.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BlogService {
    private  final BlogRepository blogRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public Blog getBlogById(int id) {
        return blogRepository.findById(id).orElse(null);
    }

    public BlogResponse updateBlog(BlogRequest request) {
        Blog blog = blogRepository.findById(request.getId()).orElse(null);
        Blog savedBlog = blogRepository.save(blog);
        return modelMapper.map(savedBlog, BlogResponse.class);
    }

    public List<BlogResponse> getAllBlogs() {
        List<Blog> blogs = blogRepository.findAll();
        return  blogs.stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }

    public BlogResponse getBlogById(Integer id) {
        Blog blog = blogRepository.findById(id).orElse(null);
        return modelMapper.map(blog, BlogResponse.class);
    }

    public Blog getBlogEntityById(Integer id) {
        return blogRepository.findById(id).orElse(null);
    }

    public void deleteBlogById(Integer id){
        blogRepository.deleteById(id);
    }

    public Blog getBlogByTitle(String title) {
        return blogRepository.getBlogByTitle(title);
    }

    public List<BlogResponse> getAllUserBlogs(String username) {
        User user = userService.getUserEntityByUsername(username);
        List<Blog> blogs = user.getBlogs();
        return blogs.stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }



}