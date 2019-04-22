package de.test.toolboxtest4;

import java.security.KeyStore;

public class KeyListItem {

    private String alias;
    private String algorithm;
    private String format;

    public KeyListItem(String alias, String algorithm, String format) {
        this.alias = alias;
        this.algorithm = algorithm;
        this.format = format;
    }

    public String getAlias() {
        return alias;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getFormat() {
        return format;
    }
}
