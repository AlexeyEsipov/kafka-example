package ru.esipov.ws.emailnotification.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import ru.esipov.ws.core.ProductCreatedEvent;
import ru.esipov.ws.emailnotification.exception.NonRetryableException;
import ru.esipov.ws.emailnotification.exception.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.esipov.ws.emailnotification.persistence.entity.ProcessedEventEntity;
import ru.esipov.ws.emailnotification.persistence.repository.ProcessEventRepository;

@Component
//@KafkaListener(topics = "product-created-events-topic", groupId = "product-created-events") // можно указать groupID тут
@KafkaListener(topics = "product-created-events-topic")
public class ProductCreatedEventHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RestTemplate restTemplate;
    private final ProcessEventRepository processEventRepository;

    public ProductCreatedEventHandler(RestTemplate restTemplate, ProcessEventRepository processEventRepository) {
        this.restTemplate = restTemplate;
        this.processEventRepository = processEventRepository;
    }


    @Transactional
    @KafkaHandler
    public void handle(@Payload ProductCreatedEvent productCreatedEvent,
                       @Header("messageId") String messageId,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        // пример ошибки, которую не надо повторять запрос, а сразу пишется в dead letter topic
       /* if (true) {
            throw new NonRetryableException("Non retryable exception");
        }*/
        logger.info("Received event: {}, productId: {}", productCreatedEvent.getTitle(), productCreatedEvent.getProductId());

        ProcessedEventEntity processedEventEntity = processEventRepository.findByMessageId(messageId);

        if (processedEventEntity != null) {
            logger.info("Duplicate message id: {}", messageId);
            return;
        }

        String url = "http://localhost:8090/response/200";
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().value() == HttpStatus.OK.value()) {
                logger.info("Received event: {}", response.getBody());
            }
        } catch (ResourceAccessException e) {  // если ресурс недоступен, то повторяем вызов
            logger.error(e.getMessage());
            throw new RetryableException(e);
        } catch (HttpServerErrorException e) {   // тут уже понятно, что восстановить ресурс невозможно, повторять не смысла
            logger.error(e.getMessage());
            throw new NonRetryableException(e);
        } catch (Exception e) {   // тут уже понятно, что восстановить ресурс невозможно, повторять не смысла
            logger.error(e.getMessage());
            throw new NonRetryableException(e);
        }

        try {
            processEventRepository.save(new ProcessedEventEntity(messageId, productCreatedEvent.getProductId()));
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage());
            throw new NonRetryableException(e);
        }


    }
}
