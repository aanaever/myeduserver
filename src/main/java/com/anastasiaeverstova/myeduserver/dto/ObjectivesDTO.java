package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ObjectivesDTO {
    @NotNull
    private Integer courseId;
    @NotEmpty
    private List<String> objectives;
}
