package com.shop.app.repository;

import com.shop.app.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(String id);

    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
