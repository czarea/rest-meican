package com.czarea.rest.meican.repository;

import com.czarea.rest.meican.entity.Order;
import com.czarea.rest.meican.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author zhouzx
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findAllByUserId(Integer userId, Pageable pageable);
}
