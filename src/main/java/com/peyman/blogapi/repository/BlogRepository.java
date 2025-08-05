package com.peyman.blogapi.repository;

import com.peyman.blogapi.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BlogRepository extends JpaRepository<Blog, Long> {


    Blog getBlogByTitle(String title);
}