/**
 * 
 */
package de.i3mainz.actonair.springframework.xd.modules.uba.mixins;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

import org.springframework.xd.module.options.spi.ModuleOption;

import de.i3mainz.actonair.springframework.uba.enums.DataType;
import de.i3mainz.actonair.springframework.uba.enums.Pollutant;

/**
 * @author Nikolai Bock
 *
 */
public final class UBAMixins {

    /**
     * 
     */
    private UBAMixins() {
    }

    public static class UBADataMixin {

        private Pollutant pollutant = Pollutant.PM1;
        private DataType valueType = DataType.Tagesmittel;

        /**
         * @return the pollutant
         */
        public Pollutant getPollutant() {
            return pollutant;
        }

        /**
         * @param pollutant
         *            the pollutant to set
         */
        @ModuleOption("Schadstoff welcher abgefragt wird")
        public void setPollutant(Pollutant pollutant) {
            this.pollutant = pollutant;
        }

        /**
         * @return the valueType
         */
        public DataType getValueType() {
            return valueType;
        }

        /**
         * @param valueType
         *            the valueType to set
         */
        @ModuleOption("Aggregationsvariante der Daten")
        public void setValueType(DataType valueType) {
            this.valueType = valueType;
        }

        @AssertTrue(message = "Der Abfragetyp muss zum Schadstoff passen.")
        public boolean isValueTypeComparesPollutant() {
            switch (pollutant) {
            case PM1:
                return valueType.equals(DataType.Tagesmittel);
            case CO:
                return valueType.equals(DataType.AchtStundenMittel) || valueType.equals(DataType.AchtStundenTagesMax);
            case O3:
                return valueType.equals(DataType.AchtStundenMittel) || valueType.equals(DataType.AchtStundenTagesMax)
                        || valueType.equals(DataType.EinStundenMittel) || valueType.equals(DataType.EinStundenTagesMax);
            case SO2:
                return valueType.equals(DataType.Tagesmittel) || valueType.equals(DataType.EinStundenMittel)
                        || valueType.equals(DataType.EinStundenTagesMax);
            case NO2:
                return valueType.equals(DataType.EinStundenMittel) || valueType.equals(DataType.EinStundenTagesMax);
            default:
                return false;
            }
        }
    }

    public static class UBATemporalMixin {
        private String measurementStamp;

        @ModuleOption("Zeitpunkt der Messdaten (SpEL")
        public void setMeasurementStamp(String measurementStamp) {
            this.measurementStamp = measurementStamp;
        }

        public String getMeasurementStamp() {
            return this.measurementStamp;
        }
    }

    public static class UBASpatialMixin {
        private String stateCode;

        @ModuleOption("Zwei-Zeichen-Code für Bundesländer")
        public void setStateCode(String stateCode) {
            this.stateCode = stateCode;
        }

        @Pattern(regexp = "(BW|BY|BE|BB|HH|HE|MV|NI|NW|RP|SH|SL|SN|ST|TH|UB)")
        public String getStateCode() {
            return this.stateCode;
        }

    }

}
