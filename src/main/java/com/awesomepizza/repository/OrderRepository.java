package com.awesomepizza.repository;

import com.awesomepizza.model.Order;
import com.awesomepizza.model.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCode(String code);

    boolean existsByStatus(OrderStatusEnum orderStatusEnum);

    Optional<Order> findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum orderStatusEnum);
}
