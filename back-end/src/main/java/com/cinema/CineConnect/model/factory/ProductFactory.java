package com.cinema.CineConnect.model.factory;

import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.model.FoodProduct;
import com.cinema.CineConnect.model.Product;
import com.cinema.CineConnect.model.Ticket;

public class ProductFactory {

    public static Product createProduct(ProductRecord productRecord) {
        return switch (productRecord.type()) {
            case "Ticket" -> new Ticket(
                    productRecord.productId(),
                    productRecord.sessionId(),
                    productRecord.seatNumber(),
                    productRecord.name(),
                    productRecord.type(),
                    productRecord.price());
            case "Food", "Drink", "Addon" -> new FoodProduct(productRecord);
            default -> throw new RuntimeException("Invalid product type " + productRecord.type());
        };
    }
}
