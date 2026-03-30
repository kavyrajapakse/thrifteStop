package hibernate;

import javax.persistence.*;

@Entity
@Table(name = "city")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "city_name")
    private String cityName;
    
    @Column(name = "shipping_cost")
    private Double shippingCost;

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * @return the shippingCost
     */
    public Double getShippingCost() {
        return shippingCost;
    }

    /**
     * @param shippingCost the shippingCost to set
     */
    public void setShippingCost(Double shippingCost) {
        this.shippingCost = shippingCost;
    }
}
