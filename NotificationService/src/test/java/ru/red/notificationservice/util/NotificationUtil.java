package ru.red.notificationservice.util;

import ru.red.notificationservice.domain.Notification;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class NotificationUtil {
    public static Notification createRandom() {
        var notification = new Notification();
        var random = new Random();
        notification.setTimestamp(Date.from(Instant.ofEpochSecond(random.nextInt(0, (int) Math.pow(2, 32)))));
        var length = random.nextInt(128, 1024);
        var contents = random
                .ints(length, ' ', 'z')
                .mapToObj(i -> (char) i)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        notification.setContents(contents);
        notification.setUserAddress(EmailUtil.generateRandomEmail());
        return notification;
    }

    public static Notification createRandomWithAddress(String address) {
        var notification = createRandom();
        notification.setUserAddress(address);
        return notification;
    }
}
