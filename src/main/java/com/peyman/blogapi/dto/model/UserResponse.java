package com.peyman.blogapi.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String userName;

    private String password;

    private Timestamp createdDate;

    private Timestamp lastModifiedDate;
}
