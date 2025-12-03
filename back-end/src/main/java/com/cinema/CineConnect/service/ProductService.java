package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.model.Product;
import com.cinema.CineConnect.model.factory.ProductFactory;
import com.cinema.CineConnect.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final Path rootLocation;

    public ProductService(ProductRepository productRepository,
            @Value("${file.upload-dir}") String uploadDir) {
        this.productRepository = productRepository;
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public ProductRecord saveProductWithImage(ProductRecord productRecord, MultipartFile file) throws IOException {
        // Validate product type
        if (!isValidProductType(productRecord.type())) {
            throw new IllegalArgumentException(
                    "Invalid product type: " + productRecord.type() + ". Must be Food or Drink.");
        }

        // Save image file
        String uniqueFilename = saveImageFile(file);

        // Create product with image URL
        UUID productId = UUID.randomUUID();
        String imageUrl = "/uploads/" + uniqueFilename;

        ProductRecord productWithImage = new ProductRecord(
                productId,
                productRecord.name(),
                productRecord.type(),
                productRecord.price(),
                productRecord.quantity(),
                productRecord.available(),
                null, // sessionId (not used for Food/Drink)
                imageUrl,
                productRecord.addOns(),
                null // seatNumber (not used for Food/Drink)
        );

        // Use ProductFactory to create the appropriate product instance
        Product product = ProductFactory.createProduct(productWithImage);

        // Persist the product
        productRepository.saveProduct(product);

        return productWithImage;
    }

    private String saveImageFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                .normalize().toAbsolutePath();

        // Security check: ensure file is within upload directory
        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside upload directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return uniqueFilename;
    }

    private boolean isValidProductType(String type) {
        return "Food".equals(type) || "Drink".equals(type);
    }
}
