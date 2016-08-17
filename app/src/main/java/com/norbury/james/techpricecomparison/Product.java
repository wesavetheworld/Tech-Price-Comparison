package com.norbury.james.techpricecomparison;

/**
 * Created by james on 17/08/2016.
 * This holds the product information to be passed to an ArrayAdapter
 */
public class Product {
    private String name;
    private float price;
    private String image;

    public Product(String name, float price, String image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }
}
