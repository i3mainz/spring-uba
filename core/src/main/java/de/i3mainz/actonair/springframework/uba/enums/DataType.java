/**
 * 
 */
package de.i3mainz.actonair.springframework.uba.enums;

/**
 * @author Nikolai Bock
 *
 */
public enum DataType {
    Tagesmittel("1TMW"), AchtStundenMittel("8SMW"), AchtStundenTagesMax(
            "8TMAX"), EinStundenMittel("1SMW"), EinStundenTagesMax("1TMAX");

    private String value;

    private DataType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
