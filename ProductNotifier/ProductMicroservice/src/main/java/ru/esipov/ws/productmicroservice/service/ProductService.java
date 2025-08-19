package ru.esipov.ws.productmicroservice.service;



import ru.esipov.ws.core.CreateProductDto;

import java.util.concurrent.ExecutionException;

public interface ProductService {

    String createProduct(CreateProductDto createProductDto) throws ExecutionException, InterruptedException;
}
