package com.fabiocondo.enumeration;

public enum Country {
    MOZAMBIQUE("MZ", "Mozambique"),
    ANGOLA("AO", "Angola"),
    PORTUGAL("PT", "Portugal");

    private final String isoCode;
    private final String displayName;

    Country(String isoCode, String displayName) {
        this.isoCode = isoCode;
        this.displayName = displayName;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Country fromIsoCode(String isoCode) {
        for (Country country : values()) {
            if (country.getIsoCode().equalsIgnoreCase(isoCode)) {
                return country;
            }
        }
        throw new IllegalArgumentException("Unknown ISO code: " + isoCode);
    }
}

