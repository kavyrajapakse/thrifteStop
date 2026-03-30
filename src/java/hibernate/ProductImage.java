package hibernate;

import javax.persistence.*;

@Entity
@Table(name = "product_img")
public class ProductImage {

    @Id
    @Column(name = "img_path")
    private String imgPath;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Getters and Setters

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
