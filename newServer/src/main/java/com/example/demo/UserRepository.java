package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findAllByUseridAndDays(int id, int days);
    List<User> findAllByUserid(int id);

}
