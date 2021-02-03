package com.supmark.model;

public class Product {

    private String notes;
    private int quantity;
    private String productImage;
    private String product;
    private String user;

    public Product(String user, int quantity, String notes) {
    }


    public Product(String productImage, String product, String user, int quantity, String notes) {
        this.product = product;
        this.productImage = productImage;
        this.user = user;
        this.quantity = quantity;
        this.notes = notes;
    }

    public Product(String productImage, String product, String user, int quantity) {
        this.product = product;
        this.productImage = productImage;
        this.user = user;
        this.quantity = quantity;
        this.notes = "";
    }

    public Product(String productImage, String product) {
        this.product = product;
        this.productImage = productImage;
    }

    public Product(String product) {
        this.product = product;
    }

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String productImage) {
        this.notes = notes;
    }

}


