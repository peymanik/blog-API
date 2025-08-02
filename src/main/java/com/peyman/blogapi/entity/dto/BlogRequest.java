package com.peyman.blogapi.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BlogRequest {

    private Integer id;

    @NotBlank(message = "Blog title is required")
    private String title;

}