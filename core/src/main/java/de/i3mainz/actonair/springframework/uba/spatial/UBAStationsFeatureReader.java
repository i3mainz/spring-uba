/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.spatial;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import de.i3mainz.actonair.springframework.uba.model.json.deserialize.StationFeatureDeserializer;

/**
 * @author Nikolai Bock
 *
 */
public class UBAStationsFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private ContentState state;

    /** Utility class used to build features */
    protected SimpleFeatureBuilder builder;
    private List<SimpleFeature> features;
    private ListIterator<SimpleFeature> featureIterator;

    public UBAStationsFeatureReader(ContentState state, Query query) throws IOException {
        this.state = state;
        builder = new SimpleFeatureBuilder(state.getFeatureType());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

        ResponseEntity<byte[]> response = restTemplate.exchange(
                ((UBAStationsDataStore) this.state.getEntry().getDataStore()).getUrl(), HttpMethod.GET, null,
                byte[].class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            CsvMapper mapper = new CsvMapper();
            mapper.registerModule(
                    new SimpleModule().addDeserializer(SimpleFeature.class, new StationFeatureDeserializer(builder)));
            CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t');
            MappingIterator<SimpleFeature> it = mapper.reader(SimpleFeature.class).with(schema)
                    .readValues(response.getBody());
            this.features = it.readAll();
            this.featureIterator = this.features.listIterator();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    @Override
    public SimpleFeatureType getFeatureType() {
        return this.state.getFeatureType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.FeatureReader#next()
     */
    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        return featureIterator.next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    @Override
    public boolean hasNext() throws IOException {
        return featureIterator.hasNext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.FeatureReader#close()
     */
    @Override
    public void close() throws IOException {
        features = null;
        featureIterator = null;
        builder = null;
    }

}
