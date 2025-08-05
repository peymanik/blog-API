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
public class BlogRequest {

    private Long id;

    @NotBlank(message = "Blog title is required")
    private String title;

}