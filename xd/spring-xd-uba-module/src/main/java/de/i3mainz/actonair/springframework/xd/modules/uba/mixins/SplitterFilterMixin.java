package de.i3mainz.actonair.springframework.xd.modules.uba.mixins;

import javax.validation.constraints.AssertTrue;

import org.springframework.xd.module.options.spi.ModuleOption;
import org.springframework.xd.module.options.spi.ProfileNamesProvider;

public class SplitterFilterMixin implements ProfileNamesProvider {

    private static final String[] USE_SPLITTER_FILTER = new String[] { "use-splitter", "use-filter" };
    private static final String[] DONT_USE_SPLITTER = new String[] { "dont-use-splitter" };
    private static final String[] USE_SPLITTER_NOFILTER = new String[] { "use-splitter", "dont-use-filter" };

    private boolean split = true;
    private String filter;

    @AssertTrue(message = "filter only when split is active")
    private boolean isValid() {
        if (!split) {
            return isNullOrEmpty(filter);
        }
        return true;
    }

    @Override
    public String[] profilesToActivate() {
        return split ? !isNullOrEmpty(filter) ? USE_SPLITTER_FILTER : USE_SPLITTER_NOFILTER : DONT_USE_SPLITTER;
    }

    public boolean isSplit() {
        return split;
    }

    @ModuleOption("whether to split the WFS result as individual messages")
    public void setSplit(boolean split) {
        this.split = split;
    }

    @ModuleOption(value = "filter all messages without condition")
    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
