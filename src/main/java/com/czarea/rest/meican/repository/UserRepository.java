package com.czarea.rest.meican.repository;

import com.czarea.rest.meican.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zhouzx
 */
public interface UserRepository extends JpaRepository<User, Integer> {

}
