package com.peyman.blogapi.entity.repository;

import com.peyman.blogapi.entity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUserName(String userName);


    Integer deleteByUserName(String userName);
}