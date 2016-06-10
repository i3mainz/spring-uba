/**
 * 
 */
package de.i3mainz.actonair.springframework.xd.modules.uba;

import org.springframework.xd.module.options.spi.Mixin;
import org.springframework.xd.module.options.spi.ModuleOption;
import org.springframework.xd.module.options.spi.ProfileNamesProvider;

import de.i3mainz.actonair.springframework.xd.modules.uba.mixins.SplitterFilterMixin;
import de.i3mainz.actonair.springframework.xd.modules.uba.mixins.UBAMixins.UBADataMixin;
import de.i3mainz.actonair.springframework.xd.modules.uba.mixins.UBAMixins.UBASpatialMixin;
import de.i3mainz.actonair.springframework.xd.modules.uba.mixins.UBAMixins.UBATemporalMixin;

/**
 * @author Nikolai Bock
 *
 */
@Mixin({ UBADataMixin.class, UBATemporalMixin.class, UBASpatialMixin.class, SplitterFilterMixin.class })
public class UBASourceOptionsMetadata implements ProfileNamesProvider {

    private boolean readAll = true;
    private String stationFilter = "new double[]{8.23124,49.9999345,0.05}";

    @Override
    public String[] profilesToActivate() {
        if (readAll) {
            return new String[] { "use-all-station" };
        } else {
            return new String[] { "use-filtered-station" };
        }
    }

    public boolean isReadAll() {
        return readAll;
    }

    @ModuleOption("whether all stations or the station in area will be loaded")
    public void setReadAll(boolean readAll) {
        this.readAll = readAll;
    }

    /**
     * @return the stationFilter
     */
    public final String getStationFilter() {
        return stationFilter;
    }

    /**
     * @param stationFilter the stationFilter to set
     */
    @ModuleOption("expression on station filter")
    public final void setStationFilter(String stationFilter) {
        this.stationFilter = stationFilter;
    }
    
    
}
