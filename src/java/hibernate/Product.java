package hibernate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "price")
    private Double price;


    @Column(name = "description")
    private String description;

    @Column(name = "title")
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "datetime_added")
    private Date datetimeAdded;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private Condition condition;


    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;


    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDatetimeAdded() {
        return datetimeAdded;
    }

    public void setDatetimeAdded(Date datetimeAdded) {
        this.datetimeAdded = datetimeAdded;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    
}
