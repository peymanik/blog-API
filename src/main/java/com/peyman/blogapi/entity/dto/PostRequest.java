package com.peyman.blogapi.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequest {

    private Integer id;

    @NotBlank(message = "Blog title cant be empty")
    private String title;

    @NotBlank(message = "Content of post cant be empty")
    private String content;

}