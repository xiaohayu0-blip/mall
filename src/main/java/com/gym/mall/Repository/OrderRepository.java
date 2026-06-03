package com.gym.mall.Repository;

import com.gym.mall.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, String status, Pageable pageable);

    List<Order> findByUserIdAndStatus(Long userId, String status);

    // 管理员查询
    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreateTimeDesc(String status, Pageable pageable);
}
