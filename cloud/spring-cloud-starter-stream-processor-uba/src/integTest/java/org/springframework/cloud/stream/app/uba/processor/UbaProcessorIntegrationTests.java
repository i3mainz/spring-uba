/**
 * 
 */
package org.springframework.cloud.stream.app.uba.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.i3mainz.actonair.springframework.uba.model.Observation;

/**
 * Some nice integration tests UBA processor
 * 
 * @author Nikolai Bock
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UbaProcessorIntegrationTests.UBAProcessorApplication.class)
@IntegrationTest({ "server.port=-1" })
@DirtiesContext
public abstract class UbaProcessorIntegrationTests {

    @Autowired
    @Bindings(UbaProcessorConfiguration.class)
    protected Processor processor;

    @Autowired
    protected MessageCollector messageCollector;

    @Autowired
    protected ObjectMapper objectMapper;

    protected FeatureJSON fjson = new FeatureJSON(new GeometryJSON());

    private static SimpleFeatureType buildFeatureType() {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        // set the name
        b.setName("UBAStation");

        // add some properties
        b.add("stationID", String.class);
        b.add("title", String.class);
        b.add("state", String.class);

        // add a geometry property
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", Point.class);

        // build the type
        return b.buildFeatureType();
    }

    protected SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS");

    @WebIntegrationTest({ "ubasensors.measurementStamp='-1D'",
            "ubasensors.filterStations=false" })
    public static class TestUBAStandardRequest
            extends UbaProcessorIntegrationTests {

        @Test
        public void testInsert() throws JsonProcessingException {
            processor.input().send(new GenericMessage<String>("Hallo Welt"));
            BlockingQueue<Message<?>> messages = messageCollector
                    .forChannel(processor.output());
            System.out.println("Standard Request: ");
            messages.stream().map(Message::getPayload)
                    .forEach(System.out::println);
        }
    }

    @WebIntegrationTest({
            "ubasensors.measurementStamp=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.S\").parse('2016-08-23 12:00:00.0')",
            "ubasensors.filterStations=true",
            "ubasensors.stationFilter=new double[]{8.2352354,49.99923434,0.05}",
            "ubasensors.readAll=false"/*
                                       * ,
                                       * "logging.level.org.springframework.web: DEBUG"
                                       */ })
    public static class TestUBAFilteredRequest
            extends UbaProcessorIntegrationTests {

        @Test
        public void testInsert() throws JsonProcessingException {
            processor.input().send(new GenericMessage<String>("Hallo Welt"));
            BlockingQueue<Message<?>> messages = messageCollector
                    .forChannel(processor.output());
            System.out.println("Filtered Measurements: ");
            messages.stream().map(Message::getPayload)
                    .forEach(System.out::println);
        }
    }

    @WebIntegrationTest({
            "ubasensors.measurementStamp=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.S\").parse(payload.time)",
            "ubasensors.filterStations=true",
            "ubasensors.stationFilter=new double[]{new Double(payload.longitude),new Double(payload.latitude),0.05}",
            "ubasensors.readAll=false", "ubasensors.pollutant=O3",
            "ubasensors.valueType=AchtStundenTagesMax"
            /*
             * , "logging.level.org.springframework.web: DEBUG"
             */ })

    public static class TestUBAO3Request extends UbaProcessorIntegrationTests {

        @Test
        public void testO3() throws JsonProcessingException {
            Map<String, Object> map = new HashMap<>();
            map.put("time", "2016-08-18 12:00:00.0");
            map.put("latitude", 49.99923434);
            map.put("longitude", 8.2452354);
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            map.put("time", "2016-08-27 12:00:00.0");
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            BlockingQueue<Message<?>> messages = messageCollector
                    .forChannel(processor.output());
            System.out.println("Filtered Payload Measurements: ");
            messages.stream().map(Message::getPayload)
                    /* .map(o -> createString(o, "µg/m³", "Ozone")) */
                    .forEach(System.out::println);
        }
    }

    @WebIntegrationTest({
            "ubasensors.measurementStamp=new java.text.SimpleDateFormat(\"yyyy-MM-ddHH:mm:ss.S\").parse(payload.time)",
            "ubasensors.filterStations=true",
            "ubasensors.stationFilter=new double[]{new Double(payload.longitude),new Double(payload.latitude),0.05}",
            "ubasensors.readAll=false", "ubasensors.pollutant=NO2",
            "ubasensors.valueType=EinStundenTagesMax"/*
                                                      * ,
                                                      * "logging.level.org.springframework.web: DEBUG"
                                                      */ })
    public static class TestUBANO2Request extends UbaProcessorIntegrationTests {

        @Test
        public void testNO2() throws JsonProcessingException {
            Map<String, Object> map = new HashMap<>();
            map.put("time", "2016-08-18 12:00:00.0");
            map.put("latitude", 49.99923434);
            map.put("longitude", 8.2452354);
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            map.put("time", "2016-08-27 12:00:00.0");
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            BlockingQueue<Message<?>> messages = messageCollector
                    .forChannel(processor.output());
            messages.stream().map(Message::getPayload)
                    /* .map(o -> createString(o, "µg/m³", "NO2")) */
                    .forEach(System.out::println);
        }
    }

    @WebIntegrationTest({
            "ubasensors.measurementStamp=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.S\").parse(payload.time)",
            "ubasensors.filterStations=true",
            "ubasensors.stationFilter=new double[]{new Double(payload.longitude),new Double(payload.latitude),0.05}",
            "ubasensors.readAll=false", "ubasensors.pollutant=SO2",
            "ubasensors.valueType=Tagesmittel" })

    public static class TestUBASO2Request extends UbaProcessorIntegrationTests {

        @Test
        public void testSO2() throws JsonProcessingException {
            Map<String, Object> map = new HashMap<>();
            map.put("time", "2016-08-18 12:00:00.0");
            map.put("latitude", 49.99923434);
            map.put("longitude", 8.2452354);
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            map.put("time", "2016-08-27 12:00:00.0");
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            BlockingQueue<Message<?>> messages = messageCollector
                    .forChannel(processor.output());
            messages.stream()
                    .map(Message::getPayload)/*
                                              * .map(o -> createString(o,
                                              * "µg/m³", "SO2"))
                                              */
                    .forEach(System.out::println);
        }

    }

    @WebIntegrationTest({
            "ubasensors.measurementStamp=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.S\").parse(payload.time)",
            "ubasensors.filterStations=true",
            "ubasensors.stationFilter=new double[]{new Double(payload.longitude),new Double(payload.latitude),0.05}",
            "ubasensors.readAll=false", "ubasensors.pollutant=CO",
            "ubasensors.valueType=AchtStundenTagesMax"
            /*
             * , "logging.level.org.springframework.web: DEBUG"
             */ })

    public static class TestUBACORequest extends UbaProcessorIntegrationTests {

        @Test
        public void testCO() throws JsonProcessingException {
            Map<String, Object> map = new HashMap<>();
            map.put("time", "2016-08-18 12:00:00.0");
            map.put("latitude", 49.99923434);
            map.put("longitude", 8.2452354);
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            map.put("time", "2016-08-27 12:00:00.0");
            processor.input()
                    .send(new GenericMessage<Map<String, Object>>(map));
            BlockingQueue<Message<?>> messages = messageCollector
                    .forChannel(processor.output());
            messages.stream().map(Message::getPayload)
                    /* .map(o -> createString(o, "mg/m³", "CO")) */
                    .forEach(System.out::println);
        }

    }

    @SuppressWarnings("unchecked")
    protected String createString(Object observation, String uom,
            String property) {
        System.out.println(observation.getClass().getName());
        Observation tmp = null;
        if (observation instanceof Observation) {
            tmp = (Observation) observation;

        }
        if (observation instanceof List) {
            tmp = (Observation) ((List<Observation>) observation).get(0);
        }
        if (tmp != null) {
            SimpleFeatureType featureType = buildFeatureType();

            if (featureType != null) {
                fjson.setFeatureType(featureType);
            }

            SimpleFeature position = null;
            try {
                position = fjson.readFeature(tmp.getPosition());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (position != null) {
                return "INSERT INTO " + "TEST_DATA" + "." + "TEST_DATA"
                        + "_INPUT_2 (PERSON_ID,SENSOR_ID,POSITION,TIME,PROPERTY,VALUE,UOM) values('"
                        + "UBA" + "', '" + tmp.getStationId() + "', '"
                        + ((Geometry) position.getDefaultGeometry()).toText()
                        + "', '" + df.format(tmp.getDatum()) + "', '" + property
                        + "', " + tmp.getValue() + ", '" + uom + "');\n";
            }
        }
        return null;
    }

    protected void writeToFile(String row) {
        try {
            File file = new File("HANAIMPORT.sql");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            bw.write(row);
            bw.close();

            System.out.println("Done for " + row);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SpringBootApplication
    public static class UBAProcessorApplication {

    }
}
