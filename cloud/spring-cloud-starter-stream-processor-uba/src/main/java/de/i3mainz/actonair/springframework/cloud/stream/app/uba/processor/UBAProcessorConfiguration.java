/**
 * 
 */
package de.i3mainz.actonair.springframework.cloud.stream.app.uba.processor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.http.Http;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.messaging.MessageChannel;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import de.i3mainz.actonair.springframework.cloud.stream.app.uba.processor.integration.splitter.StationsHeaderSplitter;
import de.i3mainz.actonair.springframework.cloud.stream.app.uba.processor.integration.transformer.CSVTransformer;
import de.i3mainz.actonair.springframework.cloud.stream.app.uba.processor.integration.transformer.StationHeaderEnricher;
import de.i3mainz.actonair.springframework.uba.creators.DateCreator;
import de.i3mainz.actonair.springframework.uba.creators.URLCreator;
import de.i3mainz.actonair.springframework.uba.model.json.deserialize.ObservationDeserializer;
import de.i3mainz.actonair.springframework.uba.spatial.UBAStationsDataStore;

/**
 * @author Nikolai Bock
 *
 */
@Configuration
@EnableConfigurationProperties(value = { UBAProcessorProperties.class })
@EnableBinding(Processor.class)
public class UBAProcessorConfiguration {

    @Autowired
    private UBAProcessorProperties properties;

    @Bean
    public MessageChannel dateenrich() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel stationsChannel() {
        return MessageChannels.direct().get();
    }
    
    @Bean
    public IntegrationFlow startFlow() {
        return IntegrationFlows.from(Processor.INPUT).enrichHeaders(h -> h.headerExpression("observationtime",
                "@dateCreator.create(" + properties.getMeasurementStamp() + ")")).channel(dateenrich()).get();
    }

    @Bean
    public IntegrationFlow endFlow(CSVTransformer transformer) {
        return IntegrationFlows.from(stationsChannel())
                .handle(Http.outboundGateway(new SpelExpressionParser().parseExpression("@urlCreator.getURL()"))
                        .httpMethod(HttpMethod.GET).expectedResponseType(byte[].class)
                        .uriVariable("datum",
                                "headers['observationtime'].format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMdd'))")
                .uriVariable("hour",
                        "headers['observationtime'].format(T(java.time.format.DateTimeFormatter).ofPattern('HH'))")
                .uriVariable("station", "headers['station']"))
                .enrichHeaders(h -> h.header("pollutant", properties.getPollutant()).header("valueType",
                        properties.getValueType()))
                .transform(transformer).transform(Transformers.toJson())
                .enrichHeaders(h -> h.header("SensorService", "UBA-Luftdaten")).channel(Processor.OUTPUT).get();
    }
    
    @Bean
    public CsvMapper mapper() {
        return new CsvMapper();
    }

    @Bean
    public CSVTransformer transformer(CsvMapper mapper) throws IOException {
        return new CSVTransformer(mapper, new ObservationDeserializer());
    }

    @Bean
    public URLCreator urlCreator() {
        URLCreator creator = new URLCreator();
        creator.setPollutant(properties.getPollutant());
        creator.setReadAll(properties.isReadAll());
        creator.setValueType(properties.getValueType());
        creator.setStateCode(properties.getStateCode());
        return creator;

    }

    @Bean
    @ConditionalOnProperty(name = "ubasensors.filterStations", havingValue = "true")
    public UBAStationsDataStore ds(URLCreator creator) {
        return new UBAStationsDataStore(creator);
    }

    @Bean
    public DateCreator dateCreator() {
        return new DateCreator();
    }

    @Bean
    @ConditionalOnProperty(name = "ubasensors.filterStations", havingValue = "true")
    public StationHeaderEnricher stationEnricher(UBAStationsDataStore ds) throws IOException {
        return new StationHeaderEnricher(ds);
    }

    @Bean
    @ConditionalOnProperty(name = "ubasensors.filterStations", havingValue = "true")
    public IntegrationFlow filteredProcess() {
        return IntegrationFlows.from(dateenrich())
                .enrichHeaders(h -> h.headerExpression("station",
                        "@stationEnricher.addStations(" + properties.getStationFilter() + ")"))
                .split(new StationsHeaderSplitter()).channel(stationsChannel()).get();
    }

    @Bean
    @ConditionalOnProperty(name = "ubasensors.filterStations", havingValue = "false")
    public IntegrationFlow allStationsProcess() {
        return IntegrationFlows.from(dateenrich()).channel(stationsChannel()).get();
    }

}
