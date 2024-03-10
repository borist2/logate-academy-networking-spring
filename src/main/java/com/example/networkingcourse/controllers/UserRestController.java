package com.example.networkingcourse.controllers;

import com.example.networkingcourse.model.User;
import com.example.networkingcourse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users")
@RequiredArgsConstructor
@RestController
public class UserRestController
{
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> userList()
    {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user)
    {
        var savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> editUser(@PathVariable Integer id, @RequestBody User user)
    {
        if (id == null || !userRepository.existsById(id))
        {
            return ResponseEntity.notFound().build();
        }

        user.setId(id);
        var savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id)
    {
        if (id == null || !userRepository.existsById(id))
        {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
