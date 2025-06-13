package org.datn.bookstation.constants;

public enum RankName {
    GOLD("Gold"),
    SILVER("Silver"),
    DIAMOND("Diamond");

    private final String displayName;

    RankName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RankName fromString(String value) {
        for (RankName rank : RankName.values()) {
            if (rank.name().equalsIgnoreCase(value) || rank.displayName.equalsIgnoreCase(value)) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid rank name: " + value);
    }
}
