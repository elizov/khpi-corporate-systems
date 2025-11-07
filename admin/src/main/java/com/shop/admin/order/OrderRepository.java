package com.shop.admin.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findWithItemsById(String id);

    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findAllByStatusOrderByCreatedAtAsc(OrderStatus status);
}
