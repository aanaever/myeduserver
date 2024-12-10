package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginStatus {
    private boolean loggedIn = false;
    private UserDTO userInfo;


    public LoginStatus(boolean loggedIn, UserDTO userInfo) {
        this.loggedIn = loggedIn;
        this.userInfo = userInfo;
    }
}
