package com.supmark;

public class ProductItem {

    private String productImage;
    private String product;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public ProductItem(String productImage, String product) {
        this.product = product;
        this.productImage = productImage;
    }
}


