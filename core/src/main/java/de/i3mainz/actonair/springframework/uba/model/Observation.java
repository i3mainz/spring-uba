/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Nikolai Bock
 *
 */
@JsonPropertyOrder({"stationId","position","value"})
public class Observation implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String stationId;
    private Date datum;
    private String position;
    private double value;
    
    /**
     * @return the stationId
     */
    public final String getStationId() {
        return stationId;
    }
    /**
     * @param stationId the stationId to set
     */
    public final void setStationId(String stationId) {
        this.stationId = stationId;
    }
    /**
     * @return the datum
     */
    public final Date getDatum() {
        return datum;
    }
    /**
     * @param datum the datum to set
     */
    public final void setDatum(Date datum) {
        this.datum = datum;
    }
    /**
     * @return the position
     */
    public final String getPosition() {
        return position;
    }
    /**
     * @param ort the position to set
     */
    public final void setPosition(String position) {
        this.position = position;
    }
    /**
     * @return the value
     */
    public final double getValue() {
        return value;
    }
    /**
     * @param value the value to set
     */
    public final void setValue(double value) {
        this.value = value;
    }
}
