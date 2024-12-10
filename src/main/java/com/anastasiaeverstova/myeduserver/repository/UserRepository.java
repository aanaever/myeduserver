package com.anastasiaeverstova.myeduserver.repository;

import com.anastasiaeverstova.myeduserver.dto.UserDTO;
import com.anastasiaeverstova.myeduserver.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("SELECT new com.anastasiaeverstova.myeduserver.dto.UserDTO(id, fullname, email, createdAt, userRole) FROM User WHERE id = ?1")
    Optional<UserDTO> findUserDTObyId(Integer id);

    List<User> findAll();

}
