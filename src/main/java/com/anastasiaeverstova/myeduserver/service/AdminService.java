package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

}
