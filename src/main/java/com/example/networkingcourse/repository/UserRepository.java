package com.example.networkingcourse.repository;

import com.example.networkingcourse.model.User;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface UserRepository extends Repository<User, Integer>
{
    List<User> findAll();

    User save(User user);

    Optional<User> findById(Integer id);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
