/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.model.json.deserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.opengis.feature.simple.SimpleFeature;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author Nikolai Bock
 *
 */
public class StationFeatureDeserializer extends JsonDeserializer<SimpleFeature> {

    private SimpleFeatureBuilder builder;

    public StationFeatureDeserializer(SimpleFeatureBuilder builder) {
        this.builder = builder;
    }

    @Override
    public SimpleFeature deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        GeometryBuilder pointBuilder = new GeometryBuilder(new GeometryFactory(new PrecisionModel(), 4326));
        JsonNode node = p.readValueAsTree();

        String stationsID = node.get("stationCode").textValue();

        builder.add(stationsID);
        List<String> pollution = new ArrayList<String>();
        pollution.add("PM1");
        builder.add(pollution);
        double lat = node.get("lat").asDouble();
        double lon = node.get("lon").asDouble();
        builder.add(pointBuilder.point(lon, lat));
        return builder.buildFeature(stationsID);

    }

}
