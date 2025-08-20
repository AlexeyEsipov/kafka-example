package ru.esipov.ws.productmicroservice.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import ru.esipov.ws.core.CreateProductDto;
import ru.esipov.ws.core.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ProductServiceImpl implements ProductService{

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // асинхронный режим - не ждем подтверждения от брокера, что он получил сообщение
    /*@Override
    public String createProduct(CreateProductDto createProductDto) {
        // TODO save DB
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, createProductDto);
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future = kafkaTemplate
                .send("product-created-events-topic", productId, productCreatedEvent);
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                // если брокер вернул какую-либо ошибку
                LOGGER.error("Failed to send message: {}", exception.getMessage());
            } else {
                // если все прошло как надо
                LOGGER.info("Message send successfully: {}", result.getRecordMetadata());
            }
        });
        // этот лог чтобы проверить, что у нас отправка действительно работает в асинхронном режиме.
        LOGGER.info("Return: {}", productId);
        return productId;
    }*/

    // синхронный режим
    @Override
    public String createProduct(CreateProductDto createProductDto) throws ExecutionException, InterruptedException {
        // TODO save DB
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, createProductDto);

        ProducerRecord<String, ProductCreatedEvent> producerRecord = new ProducerRecord<>(
                "product-created-events-topic",
                productId,
                productCreatedEvent);

        producerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate
                .send(producerRecord).get();

        // этот лог чтобы проверить, что у нас отправка действительно работает в синхронном режиме.
        LOGGER.info("Topic: {}", result.getRecordMetadata().topic());
        LOGGER.info("Partition: {}", result.getRecordMetadata().partition());
        LOGGER.info("Offset: {}", result.getRecordMetadata().offset());
        LOGGER.info("Return: {}", productId);
        return productId;
    }
}
