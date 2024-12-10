package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.*;

@Getter
@ToString
public class ReviewRequest {

    @NotNull
    @Min(value = 1, message = "rating cannot be below 1")
    @Max(value = 5)
    private Integer rating;

    @Size(max = 300)
    @NotEmpty
    private String content;

    @NotNull
    private Integer courseId;


}
