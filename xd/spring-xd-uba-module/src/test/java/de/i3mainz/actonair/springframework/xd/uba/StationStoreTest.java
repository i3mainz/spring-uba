package de.i3mainz.actonair.springframework.xd.uba;

import java.io.IOException;

import javax.measure.unit.Unit;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.GeometryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.i3mainz.actonair.springframework.uba.spatial.UBAStationsDataStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class StationStoreTest {

    static final Logger logger = LoggerFactory.getLogger(StationStoreTest.class);

    @Autowired
    private UBAStationsDataStore ds;

    @Test
    public void test() throws IOException {
        SimpleFeatureSource fs = ds.getFeatureSource(ds.getTypeNames()[0]);
        SimpleFeatureType schema = fs.getSchema();
        Query query = new Query(schema.getTypeName(), Filter.INCLUDE);
        BoundingBox bounds = fs.getBounds(query);
        if (bounds == null) {
            // information was not available in the header
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = fs.getFeatures(query);
            bounds = collection.getBounds();
        }
        logger.info("The features are contained within " + bounds);
        logger.info("Anzahl Features: " + fs.getCount(query));

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        String geometryPropertyName = schema.getGeometryDescriptor().getLocalName();
        CoordinateReferenceSystem crs = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
        Unit<?> uom = crs.getCoordinateSystem().getAxis(0).getUnit();
        GeometryBuilder builder = new GeometryBuilder();
        Point point = builder.point(8.2331231, 49.9992323);
        Polygon polygon = (Polygon) point.buffer(.05);
        Filter filter = ff.intersects(ff.property(geometryPropertyName), ff.literal(polygon));
        SimpleFeatureCollection filteredFeatures = fs.getFeatures(filter);
        logger.info("Anzahl gefilteter Features: " + filteredFeatures.size());
        SimpleFeatureIterator itr = filteredFeatures.features();
        while (itr.hasNext()) {
            logger.info(((Geometry) itr.next().getDefaultGeometry()).toText());
        }
    }

}
