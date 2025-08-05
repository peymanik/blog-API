package com.peyman.blogapi.repository;

import com.peyman.blogapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserName(String userName);


    Integer deleteByUserName(String userName);
}