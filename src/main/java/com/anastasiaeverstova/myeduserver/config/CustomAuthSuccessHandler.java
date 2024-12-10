package com.anastasiaeverstova.myeduserver.config;

import com.anastasiaeverstova.myeduserver.dto.UserDTO;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.models.UserRole;
import com.anastasiaeverstova.myeduserver.service.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final MyUserDetailsService myUserDetailsService;
    private final ObjectMapper jsonMapper;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;

    @Value(value = "${frontend.root.url}")
    private String FRONTEND_URL;

    @Autowired
    public CustomAuthSuccessHandler(MyUserDetailsService myUserDetailsService, JwtUtil jwtUtil) {
        this.myUserDetailsService = myUserDetailsService;
        this.jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.modelMapper = new ModelMapper();
        this.jwtUtil = jwtUtil;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        User loggedInUser = (User) authentication.getPrincipal();
        UserDTO userInfo = modelMapper.map(loggedInUser, UserDTO.class);
        UserRole role = UserRole.valueOf(loggedInUser.getUserRole());
        userInfo.setRole(role);

        String jwt = jwtUtil.generateToken(loggedInUser);

        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("message", "Welcome back!");
        authResponse.put("userInfo", userInfo);
        authResponse.put("token", jwt);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().println(jsonMapper.writeValueAsString(authResponse));
        response.getWriter().flush();
    }


}

