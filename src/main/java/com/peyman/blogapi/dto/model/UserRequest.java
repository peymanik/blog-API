package com.peyman.blogapi.dto.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class UserRequest {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
//    @Pattern(regexp = "[a-zA-z0-9]")
    private String password;

}

//public record UserRequest (Integer id, String username ,String password){}

