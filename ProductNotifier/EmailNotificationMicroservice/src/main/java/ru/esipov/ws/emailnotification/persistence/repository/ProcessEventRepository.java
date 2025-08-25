package ru.esipov.ws.emailnotification.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.esipov.ws.emailnotification.persistence.entity.ProcessedEventEntity;

import java.util.Optional;

public interface ProcessEventRepository extends JpaRepository<ProcessedEventEntity, Long> {

//    Optional<ProcessedEventEntity> findByMessageId(String messageId);
    ProcessedEventEntity findByMessageId(String messageId);
}
