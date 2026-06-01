package swp391.group6.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class ProductDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @OneToOne(mappedBy = "productDetail")
    private Product product;

    @Column
    private String description;

    @Column
    private String variants;

    @Column
    private String images;
}