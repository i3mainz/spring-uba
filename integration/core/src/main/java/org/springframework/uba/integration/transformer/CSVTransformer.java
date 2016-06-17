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
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

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
    
    private static final Logger LOG = LoggerFactory.getLogger(CSVTransformer.class);

    private CsvMapper mapper;
    private CsvSchema schema;
    private Class<Observation> clazz = Observation.class;

    public CSVTransformer(CsvMapper mapper, JsonDeserializer<Observation> deserializer) {
        this.mapper = mapper;
        this.mapper.registerModule(new SimpleModule().addDeserializer(clazz, deserializer));
        schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t');
    }

    @Transformer
    public List<Observation> transform(@Payload byte[] payload,
            @Header("observationtime") ZonedDateTime observationTime) throws Exception {
        MappingIterator<Observation> it = mapper.reader(clazz).with(schema).readValues(payload);
        List<Observation> result = new ArrayList<>();

        while (it.hasNextValue()) {
            try {
                Observation observ = it.nextValue();
                observ.setDatum(GregorianCalendar.from(observationTime).getTime());
                result.add(observ);
            } catch (IOException e) {
                LOG.warn("IOException beim Datum setzen! Bearbeite nächstes Element");
                continue;
            }
        }
        return result;
    }
}