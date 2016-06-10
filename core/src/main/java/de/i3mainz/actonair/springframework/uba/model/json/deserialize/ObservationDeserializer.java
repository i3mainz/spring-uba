/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.model.json.deserialize;

import java.io.IOException;
import java.io.StringWriter;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import de.i3mainz.actonair.springframework.uba.model.Observation;

/**
 * @author Nikolai Bock
 *
 */
public class ObservationDeserializer extends JsonDeserializer<Observation> {

    private SimpleFeatureBuilder builder;

    public ObservationDeserializer() throws IOException {
        this.builder = new SimpleFeatureBuilder(buildFeatureType());
    }

    @Override
    public Observation deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = p.readValueAsTree();

        Observation observation = new Observation();

        String stationID = node.get("stationCode").asText();
        String title = HtmlUtils.htmlUnescape(node.get("title").asText().replaceAll(stationID, "").trim());
        String[] valStringArray = node.get("val").asText().split(" ");

        String state = node.get("state").asText();
        double lat = node.get("lat").asDouble();
        double lon = node.get("lon").asDouble();

        if (valStringArray.length == 0 || !isNumeric(valStringArray[0])) {
            throw new JsonMappingException("Cannot load value");
        }

        int value = new Integer(valStringArray[0]);
        observation.setStationId(stationID);

        observation.setPosition(createGeoJSON(stationID, title, state, lat, lon));
        observation.setValue(value);

        return observation;
    }

    /**
     * @param stationID
     * @param title
     * @param state
     * @param lat
     * @param lon
     * @throws IOException
     */
    private String createGeoJSON(String stationID, String title, String state, double lat, double lon)
            throws IOException {

        FeatureJSON fjson = new FeatureJSON();
        StringWriter writer = new StringWriter();
        SimpleFeature feature = this.buildFeature(stationID, title, state, lat, lon);
        fjson.writeFeature(feature, writer);
        return writer.toString();
    }

    private SimpleFeatureType buildFeatureType() throws IOException {
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

    private SimpleFeature buildFeature(String stationID, String title, String state, double lat, double lon) {

        GeometryBuilder pointBuilder = new GeometryBuilder(new GeometryFactory(new PrecisionModel(), 4326));
        builder.add(stationID);
        builder.add(title);
        builder.add(state);
        builder.add(pointBuilder.point(lon, lat));
        return builder.buildFeature(stationID);
    }

    private boolean isNumeric(String s) {
        return java.util.regex.Pattern.matches("\\d+", s);
    }

}
