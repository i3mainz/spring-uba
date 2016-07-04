/**
 * 
 */
package org.springframework.cloud.stream.app.uba.processor;

import java.util.concurrent.BlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.app.uba.processor.UbaProcessorConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Some nice integration tests UBA processor
 * 
 * @author Nikolai Bock
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UbaProcessorIntegrationTests.UBAProcessorApplication.class)
@IntegrationTest("{server.port=-1}")
@DirtiesContext
public abstract class UbaProcessorIntegrationTests {

    @Autowired
    @Bindings(UbaProcessorConfiguration.class)
    protected Processor processor;

    @Autowired
    protected MessageCollector messageCollector;

    @WebIntegrationTest({ "ubasensors.measurementStamp='-1D'", "ubasensors.filterStations=false" })
    public static class TestUBAStandardRequest extends UbaProcessorIntegrationTests {

        @Test
        public void testInsert() throws JsonProcessingException {
            processor.input().send(new GenericMessage<String>("Hallo Welt"));
            BlockingQueue<Message<?>> messages = messageCollector.forChannel(processor.output());
            messages.stream().map(Message::getPayload).forEach(System.out::println);
        }
    }

    @SpringBootApplication
    public static class UBAProcessorApplication {

    }
}
