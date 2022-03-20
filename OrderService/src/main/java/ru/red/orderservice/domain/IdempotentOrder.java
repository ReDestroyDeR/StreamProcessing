package ru.red.orderservice.domain;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "idempotency")
public class IdempotentOrder {
    @MongoId(FieldType.STRING)
    private String idempotencyKey;
    @NonNull
    private Order response;
    @Indexed(expireAfter = "${service.order.idempotency.ttl}")
    private Integer ttl;
}
