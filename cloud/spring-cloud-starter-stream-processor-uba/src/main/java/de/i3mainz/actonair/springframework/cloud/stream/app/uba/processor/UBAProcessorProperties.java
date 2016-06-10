package de.i3mainz.actonair.springframework.cloud.stream.app.uba.processor;

import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.i3mainz.actonair.springframework.uba.enums.DataType;
import de.i3mainz.actonair.springframework.uba.enums.Pollutant;
import lombok.Data;

@ConfigurationProperties(prefix = "ubasensors")
@Data
public class UBAProcessorProperties {

    private Pollutant pollutant = Pollutant.PM1;
    private DataType valueType = DataType.Tagesmittel;
    @Pattern(regexp = "(BW|BY|BE|BB|HH|HE|MV|NI|NW|RP|SH|SL|SN|ST|TH|UB)")
    private String stateCode;
    private boolean readAll = true;
    private String stationFilter;
    private String measurementStamp;
    private boolean filterStations = false;
}
