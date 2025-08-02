package com.peyman.blogapi.entity.repository;

import com.peyman.blogapi.entity.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PostRepository extends JpaRepository<Post, Integer> {


}