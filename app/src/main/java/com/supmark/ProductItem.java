package com.supmark;

public class ProductItem {

    private String productImage;
    private String product;
    private String user;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ProductItem(String productImage, String product, String user) {
        this.product = product;
        this.productImage = productImage;
        this.user = user;
    }

    public ProductItem(String productImage, String product) {
        this.product = product;
        this.productImage = productImage;
    }
}


