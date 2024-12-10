package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.config.JwtUtil;
import com.anastasiaeverstova.myeduserver.dto.LoginStatus;
import com.anastasiaeverstova.myeduserver.dto.UserDTO;
import com.anastasiaeverstova.myeduserver.models.CustomOAuthUser;
import com.anastasiaeverstova.myeduserver.models.MyCustomResponse;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.models.UserRole;
import com.anastasiaeverstova.myeduserver.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, ModelMapper modelMapper, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = new ModelMapper();
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<MyCustomResponse> addNewUser(@RequestBody @Valid User user) {
        if (!user.getPassword().equals(user.getConfirmPass()))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Passwords don't match");

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MyCustomResponse("Registered! Welcome"));
        } catch (Exception ex) {
            if (ex instanceof DataIntegrityViolationException) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping(path = "/register/teacher")
    public ResponseEntity<MyCustomResponse> registerTeacher(@RequestBody @Valid User user) {
        if (!user.getPassword().equals(user.getConfirmPass())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Passwords don't match");
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setUserRole(UserRole.ROLE_TEACHER);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MyCustomResponse("Registered as Teacher! Welcome"));
        } catch (Exception ex) {
            if (ex instanceof DataIntegrityViolationException) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<LoginStatus> getStatusLogin(@AuthenticationPrincipal CustomOAuthUser customOAuthUser,
                                                      @AuthenticationPrincipal User user) {
        if (customOAuthUser != null) {
            return convertToDto(customOAuthUser);
        } else if (user != null) {
            return convertToDto(user);
        } else {
            return ResponseEntity.ok().body(new LoginStatus());
        }
    }

    private ResponseEntity<LoginStatus> convertToDto(@NotNull User user) {
        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        return ResponseEntity.ok().body(new LoginStatus(true, userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> loginRequest) throws Exception {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = (User) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            UserDTO userInfo = modelMapper.map(user, UserDTO.class);
            UserRole role = UserRole.valueOf(user.getUserRole());
            userInfo.setRole(role);

            Map<String, Object> authResponse = new HashMap<>();
            authResponse.put("success", true);
            authResponse.put("message", "Welcome back!");
            authResponse.put("userInfo", userInfo);
            authResponse.put("token", jwt);
            authResponse.put("refreshToken", refreshToken);

            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials", e);
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                String newAccessToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

                Map<String, Object> tokens = new HashMap<>();
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", newRefreshToken);

                return ResponseEntity.ok(tokens);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
            }
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token has expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }
    }

}
