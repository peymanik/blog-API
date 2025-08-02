package com.peyman.blogapi.entity.repository;

import com.peyman.blogapi.entity.model.Blog;
import com.peyman.blogapi.entity.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BlogRepository extends JpaRepository<Blog, Integer> {


    Blog getBlogByTitle(String title);
}