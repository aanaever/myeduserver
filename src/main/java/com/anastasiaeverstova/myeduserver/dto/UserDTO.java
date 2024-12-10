package com.anastasiaeverstova.myeduserver.dto;

import com.anastasiaeverstova.myeduserver.models.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String fullname;
    private String email;
    private Instant createdAt;
    private UserRole role;


    public UserDTO(Integer id, String fullname, String email, Instant createdAt, UserRole role) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.createdAt = createdAt;
        this.role = role;

    }





}
