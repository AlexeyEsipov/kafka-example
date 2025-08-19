package ru.esipov.ws.core;

import java.math.BigDecimal;

public class CreateProductDto {
    private String title;
    private BigDecimal price;
    private BigDecimal quantity;

    public CreateProductDto() {
    }

    public CreateProductDto(String title, BigDecimal price, BigDecimal quantity) {
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
