package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Product;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductDTO {
    private int productId;
    private String productName;
    private Integer productPrice;
    private Boolean isActive;

    public ProductDTO(Product product) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
        this.productPrice = product.getProductPrice();
        this.isActive = product.getIsActive();
    }
}
