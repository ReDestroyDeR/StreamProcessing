package ru.red.notificationservice.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Document(collection = "notification-service")
public class Notification {
    @MongoId
    private ObjectId id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date timestamp;

    private String userAddress;
    private String contents;
}
