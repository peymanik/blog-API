package com.peyman.blogapi.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

//    private Integer id;
    private Long id;

    @NotBlank(message = "Blog title cant be empty")
    private String title;

    @NotBlank(message = "Content of post cant be empty")
    private String content;



}