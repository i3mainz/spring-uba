/**
 * 
 */
package org.springframework.uba.integration.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import de.i3mainz.actonair.springframework.uba.spatial.UBAStationsDataStore;

/**
 * @author Nikolai Bock
 *
 */
public class StationHeaderEnricher {

    private static final Logger LOG = LoggerFactory.getLogger(StationHeaderEnricher.class);

    private SimpleFeatureSource fs;
    private FilterFactory2 ff;
    private String geometryPropertyName;
    private GeometryBuilder builder;

    /**
     * @param ds
     * @throws IOException
     */
    public StationHeaderEnricher(UBAStationsDataStore ds) throws IOException {
        this.fs = ds.getFeatureSource(ds.getTypeNames()[0]);
        this.ff = CommonFactoryFinder.getFilterFactory2();
        this.geometryPropertyName = fs.getSchema().getGeometryDescriptor().getLocalName();
        this.builder = new GeometryBuilder();
    }

    public List<String> addStations(Object polygon) {
        List<String> result = new ArrayList<>();
        Geometry filterpolygon = null;
        if (polygon instanceof double[]) {
            filterpolygon = builder.point(((double[]) polygon)[0], ((double[]) polygon)[1])
                    .buffer(((double[]) polygon)[2]);
        } else if (polygon instanceof Polygon) {
            filterpolygon = (Geometry) polygon;
        } else if (polygon instanceof String) {
            try {
                filterpolygon = new WKTReader().read(polygon.toString());
            } catch (ParseException e) {
                LOG.error("Cannot parse the string as WKT", e);
            }
        }
        if (filterpolygon != null) {
            Filter filter = ff.intersects(ff.property(geometryPropertyName), ff.literal(filterpolygon));
            Filter filter2 = Query.FIDS.getFilter();
            Filter andFilter = ff.and(filter, filter2);
            SimpleFeatureCollection filteredFeatures = null;
            try {
                filteredFeatures = fs.getFeatures(andFilter);

            } catch (IOException e) {
                LOG.error("Can't access features.", e);
            }
            if (filteredFeatures != null) {
                LOG.debug("Anzahl Features: " + filteredFeatures.size());
                SimpleFeatureIterator itr = filteredFeatures.features();
                while (itr.hasNext()) {
                    result.add(itr.next().getID());
                }
                itr.close();
            }
        }
        return result;

    }
}
