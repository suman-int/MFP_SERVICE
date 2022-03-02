package com.mnao.mfp.cr.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mnao.mfp.cr.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String username);
}
