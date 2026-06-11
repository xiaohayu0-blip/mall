package com.gym.mall.Repository;

import com.gym.mall.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 原子更新订单为已支付状态，只在当前状态为 PENDING 时更新
     * 返回受影响行数（0 表示并发下状态已被其他请求修改）
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'PAID', o.paidTime = :paidTime WHERE o.orderNo = :orderNo AND o.status = 'PENDING_PAYMENT'")
    int updateStatusToPaid(@Param("orderNo") String orderNo, @Param("paidTime") Long paidTime);
}
