package org.springframework.uba.integration.splitter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.integration.annotation.Splitter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Nikolai Bock
 *
 */
public class StationsHeaderSplitter {

    @SuppressWarnings("unchecked")
    @Splitter
    public List<Message<? extends Object>> split(Message<?> message) {
        return ((Collection<String>) message.getHeaders().get("station"))
                .stream().map(s -> createMessage(message, s))
                .collect(Collectors.toList());
    }

    private static Message<?> createMessage(Message<?> message,
            String station) {
        return MessageBuilder.fromMessage(message)
                .setHeader("filteredStation", station).build();
    }
}
