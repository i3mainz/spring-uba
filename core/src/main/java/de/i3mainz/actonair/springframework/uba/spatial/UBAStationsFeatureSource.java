/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.spatial;

import java.io.IOException;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Point;

/**
 * @author Nikolai Bock
 *
 */
public class UBAStationsFeatureSource extends ContentFeatureSource {

    public UBAStationsFeatureSource(ContentEntry entry, Query query) {
        super(entry, query);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.store.ContentFeatureSource#getBoundsInternal(org.
     * geotools.data.Query)
     */
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
       ReferencedEnvelope bounds = new ReferencedEnvelope(getSchema().getCoordinateReferenceSystem());
       SimpleFeatureIterator itr = this.getFeatures(query).features();
       while(itr.hasNext()){
           bounds.include(itr.next().getBounds());
       }
       itr.close();
       return bounds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.store.ContentFeatureSource#getCountInternal(org.
     * geotools.data.Query)
     */
    @Override
    protected int getCountInternal(Query query) throws IOException {
        SimpleFeatureIterator itr = this.getFeatures(query).features();
        int count =0;
        while(itr.hasNext()){
            itr.next();
            count++;
        }
        itr.close();
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.store.ContentFeatureSource#getReaderInternal(org.
     * geotools.data.Query)
     */
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return new UBAStationsFeatureReader(getState(), query);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.store.ContentFeatureSource#buildFeatureType()
     */
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        // set the name
        b.setName(entry.getName());

        // add some properties
        b.add("stationID", String.class);
        b.add("Pollution", List.class);

        // add a geometry property
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", Point.class);

        // build the type
        return b.buildFeatureType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.store.ContentFeatureSource#getDataStore()
     */
    @Override
    public UBAStationsDataStore getDataStore() {
        return (UBAStationsDataStore) super.getDataStore();
    }

}
