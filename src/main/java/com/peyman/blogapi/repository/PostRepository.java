package com.peyman.blogapi.repository;

import com.peyman.blogapi.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PostRepository extends JpaRepository<Post, Long> {


}