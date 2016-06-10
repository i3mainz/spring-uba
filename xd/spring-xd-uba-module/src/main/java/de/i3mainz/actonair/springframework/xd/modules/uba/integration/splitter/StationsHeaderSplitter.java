/**
 * 
 */
package de.i3mainz.actonair.springframework.xd.modules.uba.integration.splitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        List<Message<?>> result = new ArrayList<Message<?>>();
        MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
        Iterator<String> itr = ((List<String>) message.getHeaders().get("station")).iterator();
        while (itr.hasNext()) {
            builder.setHeader("station", itr.next());
            result.add(builder.build());
        }
        return result;
    }

}
