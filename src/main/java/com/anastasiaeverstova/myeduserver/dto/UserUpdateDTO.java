package com.anastasiaeverstova.myeduserver.dto;

import com.anastasiaeverstova.myeduserver.models.AuthProvider;
import com.anastasiaeverstova.myeduserver.models.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDTO {
    private Integer id;
    private String fullname;
    private String email;
    private String password;
    private AuthProvider authProvider;
    private UserRole userRole;
    private boolean enabled;


    public UserUpdateDTO(Integer id, String fullname, String email, String password, AuthProvider authProvider, UserRole userRole, boolean enabled) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
        this.userRole = userRole;
        this.enabled = enabled;
    }
}
