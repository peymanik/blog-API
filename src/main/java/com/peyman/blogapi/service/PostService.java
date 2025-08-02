package com.peyman.blogapi.service;


import com.peyman.blogapi.entity.dto.PostRequest;
import com.peyman.blogapi.entity.dto.PostResponse;
import com.peyman.blogapi.entity.model.Blog;
import com.peyman.blogapi.entity.model.Post;
import com.peyman.blogapi.entity.repository.PostRepository;
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
    private ModelMapper modelMapper;

    public  PostResponse getPostById(int id) {
        Post post = postRepository.findById(id).orElse(null);
        return modelMapper.map(post, PostResponse.class);
    }

    public  Post getPostEntityById(int id) {
        return postRepository.findById(id).orElse(null);
    }

    public  void deletePostById(Integer Id) {
        postRepository.deleteById(Id);
    }

    public PostResponse updatePost(PostRequest request) {
        Post post = modelMapper.map(request,Post.class);
        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost,PostResponse.class);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<PostResponse> getAllPostsOfBlog(Integer id) {
        Blog blog = blogService.getBlogEntityById(id);
        List<Post> posts = blog.getPosts();
        return posts.stream()
                .map(post -> modelMapper.map(post, PostResponse.class))
                .collect(Collectors.toList());
    }

}
