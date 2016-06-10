/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.creators;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.i3mainz.actonair.springframework.uba.enums.DataType;
import de.i3mainz.actonair.springframework.uba.enums.Pollutant;

/**
 * @author Nikolai Bock
 *
 */
public class URLCreator implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLCreator.class);

    private final String BASE_URL = "http://www.umweltbundesamt.de/luftdaten";
    private Pollutant pollutant;
    private DataType valueType;
    private String stateCode;
    private StringBuilder builder = new StringBuilder();
    private boolean readAll = true;

    /**
     * @param pollutant
     *            the pollutant to set
     */
    public final void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    /**
     * @param valueType
     *            the valueType to set
     */
    public final void setValueType(DataType valueType) {
        this.valueType = valueType;
    }

    public final void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    /**
     * @return the readAll
     */
    public final boolean isReadAll() {
        return readAll;
    }

    /**
     * @param readAll
     *            the readAll to set
     */
    public final void setReadAll(boolean readAll) {
        this.readAll = readAll;
    }

    /**
     * @return the uRL
     */
    public final String getURL() {
        StringBuilder builder2 = new StringBuilder(builder.toString());
        builder2.append("&");
        builder2.append("date=");
        builder2.append("{datum}");
        if (this.valueType.equals(DataType.AchtStundenMittel) || this.valueType.equals(DataType.EinStundenMittel)) {
            builder2.append("&");
            builder2.append("hour=");
            builder2.append("{hour}");
        }
        if (!readAll) {
            builder2.append("&");
            builder2.append("station=");
            builder2.append("{station}");
        }
        LOGGER.debug(builder2.toString());
        return builder2.toString();
    }

    public final String getDSURL() {
        StringBuilder builder2 = new StringBuilder(builder.toString());
        builder2.append("&");
        builder2.append("date=");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        builder2.append(new SimpleDateFormat("yyyyMMdd").format(cal.getTime()));
        LOGGER.debug("DS-URL: "+builder2.toString());
        return builder2.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        builder.append(BASE_URL);
        builder.append("/stations/");
        builder.append("locations");
        builder.append("?");
        builder.append("pollutant=");
        builder.append(this.pollutant.name());
        builder.append("&");
        builder.append("data_type=");
        builder.append(this.valueType.value());
        if (this.stateCode != null && !this.stateCode.isEmpty()) {
            builder.append("&");
            builder.append("state=");
            builder.append(this.stateCode);
        }
    }

}
