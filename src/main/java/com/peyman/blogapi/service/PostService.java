package com.peyman.blogapi.service;


import com.peyman.blogapi.dto.model.PostRequest;
import com.peyman.blogapi.dto.model.PostResponse;
import com.peyman.blogapi.entity.Blog;
import com.peyman.blogapi.entity.Post;
import com.peyman.blogapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private  final PostRepository postRepository;
    private final BlogService blogService;
    private final ModelMapper modelMapper;

    public  PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        return modelMapper.map(post, PostResponse.class);
    }

    public  PostResponse createPost(PostRequest request, Long blogId) {
        Blog blog = blogService.getBlogEntityById(blogId);

        Post post = modelMapper.map(request, Post.class);
        post.setId(null);
        post.setBlog(blog);
        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostResponse.class);
    }

    public  Post getPostEntityById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public void deletePostById(Long Id) {
        postRepository.deleteById(Id);
    }

    public PostResponse updatePost(PostRequest request) {
        Post post = modelMapper.map(request,Post.class);
        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost,PostResponse.class);
    }

    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(post -> modelMapper.map(post, PostResponse.class))
                .collect(Collectors.toList());
    }

    public List<PostResponse> getAllPostsOfBlog(Long id) {
        Blog blog = blogService.getBlogEntityById(id);
        List<Post> posts = blog.getPosts();
        return posts.stream()
                .map(post -> modelMapper.map(post, PostResponse.class))
                .collect(Collectors.toList());
    }

}
