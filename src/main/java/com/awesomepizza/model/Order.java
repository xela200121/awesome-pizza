package com.awesomepizza.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String customerName;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatusEnum status;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Order(String code, String customerName) {
        this.code = code;
        this.customerName = customerName;
        this.status = OrderStatusEnum.RECEIVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInProgress() {
        this.status = OrderStatusEnum.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = OrderStatusEnum.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
}
