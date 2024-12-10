package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.StudentSummary;
import com.anastasiaeverstova.myeduserver.dto.UserDTO;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.UserRepository;
import com.anastasiaeverstova.myeduserver.service.ProfileService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final ModelMapper modelMapper;

    @Autowired
    public ProfileController(UserRepository userRepository, ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.modelMapper = new ModelMapper();
    }

    @GetMapping(path = "/mine")
    public ResponseEntity<UserDTO> getUserById(@AuthenticationPrincipal User user) {
        try {
            UserDTO userDTO = userRepository.findUserDTObyId(user.getId()).orElseThrow();
            return ResponseEntity.ok().body(userDTO);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping(path = "/mine")
    @Transactional
    public ResponseEntity<UserDTO> editMyProfile(@RequestBody UserDTO userDTO, @AuthenticationPrincipal User user) {
        try {
            User u = userRepository.findById(user.getId()).orElseThrow();
            u.setFullname(userDTO.getFullname());

            u.setConfirmPass("tysryreyt");
            User freshUser = userRepository.save(u);
            return ResponseEntity.ok().body(modelMapper.map(freshUser, UserDTO.class));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not edit your profile", ex);
        }
    }

    @GetMapping(path = "/summary")
    @ResponseStatus(value = HttpStatus.OK)
    @Cacheable(value = "student-summary", key = "#user.id")
    public List<StudentSummary> getUserSummary(@AuthenticationPrincipal User user) {
        return profileService.getUserSummaryList(user);
    }

}
