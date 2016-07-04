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

    @Splitter
    public List<Message<?>> split(Message<?> message) {
//        List<Message<?>> result = new ArrayList<>();
//        MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
//        Iterator<String> itr = ((List<String>) message.getHeaders().get("station")).iterator();
//        while (itr.hasNext()) {
//            builder.setHeader("station", itr.next());
//            result.add(builder.build());
//        }
//        return result;
        return ((Collection<Message<?>>) message.getHeaders().get("station")).stream()
                .map(s -> MessageBuilder.fromMessage(message).setHeader("station", s).build())
                .collect(Collectors.toList());
    }

}
