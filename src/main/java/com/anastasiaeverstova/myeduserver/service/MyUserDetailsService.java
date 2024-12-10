package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.config.JwtUtil;
import com.anastasiaeverstova.myeduserver.models.AuthProvider;
import com.anastasiaeverstova.myeduserver.models.CustomOAuthUser;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.models.UserRole;
import com.anastasiaeverstova.myeduserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }

    public String processOAuthPostLogin(OidcUser oidcUser) {
        CustomOAuthUser m = new CustomOAuthUser(oidcUser);
        Optional<User> existUser = userRepository.findByEmail(m.getEmail());

        User user;
        if (existUser.isEmpty()) {
            user = new User();
            user.setFullname(m.getName());
            user.setEmail(m.getEmail());
            user.setConfirmPass("fjrff");
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setUserRole(UserRole.ROLE_STUDENT);

            userRepository.save(user);
        } else {
            user = existUser.get();
        }
        return jwtUtil.generateToken(user);
    }
}