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
public class BlogResponse {
    private int id;

    private String title;

    private Date createdDate;

    private Date lastModifiedDate;
}
