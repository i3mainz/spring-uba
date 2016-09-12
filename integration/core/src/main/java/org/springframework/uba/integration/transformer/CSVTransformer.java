/**
 * 
 */
package org.springframework.uba.integration.transformer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import de.i3mainz.actonair.springframework.uba.model.Observation;

/**
 * @author Nikolai Bock
 *
 */
public class CSVTransformer {

    private static final Logger LOG = LoggerFactory
            .getLogger(CSVTransformer.class);

    private CsvMapper mapper;
    private CsvSchema schema;
    private Class<Observation> clazz = Observation.class;

    public CSVTransformer(CsvMapper mapper,
            JsonDeserializer<Observation> deserializer) {
        this.mapper = mapper;
        this.mapper.registerModule(
                new SimpleModule().addDeserializer(clazz, deserializer));
        schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t');
    }

    @SuppressWarnings("unchecked")
    @Transformer
    public List<Observation> transform(@Payload Object payload,
            @Header("observationtime") ZonedDateTime observationTime) {
        MappingIterator<Observation> it;
        byte[] data = null;
        if (payload instanceof byte[]) {
            data = (byte[]) payload;
        } else if (payload instanceof ResponseEntity) {
            data = (byte[]) ((ResponseEntity<byte[]>) payload).getBody();
        }
        try {
            it = mapper.reader(clazz).with(schema).readValues(data);
        } catch (JsonProcessingException e1) {
            LOG.error("Can't process payload!" + payload);
            throw new MessagingException("Can't process payload", e1);
        } catch (IOException e1) {
            LOG.error("Can't access payload!" + payload);
            throw new MessagingException("Can't access payload", e1);
        }
        List<Observation> result = new ArrayList<>();
        if (it != null) {
            it.forEachRemaining(observ -> {
                observ.setDatum(
                        GregorianCalendar.from(observationTime).getTime());
                result.add(observ);
            });
        }
        return result;
    }
}
