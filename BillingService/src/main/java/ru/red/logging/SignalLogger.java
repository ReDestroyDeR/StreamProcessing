package ru.red.logging;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Signal;

import java.util.function.Consumer;

@Log4j2
@Component
public class SignalLogger implements Consumer<Signal<?>> {

    @Override
    public void accept(Signal<?> signal) {
        if (signal.hasError()) {
            if (signal.getThrowable() == null) {
                log.error("[Signal<{}>] unknown", signal.getType().toString());
                return;
            }
            log.warn("[Signal<{}>] {}:{}",
                    signal.getType().toString(),
                    signal.getThrowable().getMessage(),
                    signal.getThrowable().getClass().toString());

            return;
        }

        log.info("[Signal<{}>] {}",
                signal.getType().toString(),
                signal.hasValue() ? signal.get() : "Void");
    }
}
