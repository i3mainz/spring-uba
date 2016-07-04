package org.springframework.cloud.stream.app.uba.processor;

import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.i3mainz.actonair.springframework.uba.enums.DataType;
import de.i3mainz.actonair.springframework.uba.enums.Pollutant;
import lombok.Data;

@ConfigurationProperties(prefix = "ubasensors")
@Data
public class UbaProcessorProperties {

    /**
     * Requested pollutant 
     */
    private Pollutant pollutant = Pollutant.PM1;
    /**
     * Requested type of value (aggregation)
     */
    private DataType valueType = DataType.Tagesmittel;
    /**
     * 2-digit code of state
     */
    @Pattern(regexp = "(BW|BY|BE|BB|HH|HE|MV|NI|NW|RP|SH|SL|SN|ST|TH|UB)")
    private String stateCode;
    /**
     * Whether all stations should be request or only by filter
     */
    private boolean readAll = true;
    /**
     * Filter for stations (Geometry) SpEL
     */
    private String stationFilter;
    /**
     * Temporal information for request (DateTime or String (-1D e.g.)) -- SpEL 
     */
    private String measurementStamp;
    /**
     * Whether exclude stations from request 
     */
    private boolean filterStations = false;
}
