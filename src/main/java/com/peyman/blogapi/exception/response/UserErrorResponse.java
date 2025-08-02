package com.peyman.blogapi.exception.response;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserErrorResponse {
    private  Integer status;
    private  String message;
    private  Long timestamp;
}