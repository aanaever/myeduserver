package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LessonDTO {
    private UUID id;
    private String lessonName;
    private String videokey;
    private Integer lengthSeconds;
    private Integer position;
    private Boolean isWatched;
    private Integer videoTime;
}
