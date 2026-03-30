package hibernate;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "users_email")
    private String usersEmail;

    @Column(name = "address")
    private Integer addressId;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private OrderStatus status;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "order_date")
    private Date orderDate;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUsersEmail() {
        return usersEmail;
    }

    public void setUsersEmail(String usersEmail) {
        this.usersEmail = usersEmail;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
