/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.spatial;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import de.i3mainz.actonair.springframework.uba.creators.URLCreator;

/**
 * @author Nikolai Bock
 *
 */
public class UBAStationsDataStore extends ContentDataStore {

    private String url;

    public UBAStationsDataStore(String url) {
        this.url = url;
    }
    
    public UBAStationsDataStore(URLCreator creator){
        this.url = creator.getDSURL();
    }

    /**
     * @return the url
     */
    public final String getUrl() {
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.data.store.ContentDataStore#createTypeNames()
     */
    @Override
    protected List<Name> createTypeNames() throws IOException {
        String name = "ubaStation";
        Name typeName = new NameImpl(name);
        return Collections.singletonList(typeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.geotools.data.store.ContentDataStore#createFeatureSource(org.geotools
     * .data.store.ContentEntry)
     */
    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new UBAStationsFeatureSource(entry, Query.ALL);
    }

    public SimpleFeature getStation(String fid) throws IOException {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        Filter filter = ff.id(ff.featureId(fid));
        return getFeatureSource(getTypeNames()[0]).getFeatures(filter).features().next();
    }

}
