package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
public class VideoRequest {
    @NotNull
    private Integer courseId;
    @NotEmpty
    private String lessonId;

    public VideoRequest() {
    }
}
