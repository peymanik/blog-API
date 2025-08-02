package com.peyman.blogapi.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private int id;

    private String title;

    private String content;

    private Integer likes;

    private Integer views;

    private Date createdDate;

    private Date lastModifiedDate;
}
