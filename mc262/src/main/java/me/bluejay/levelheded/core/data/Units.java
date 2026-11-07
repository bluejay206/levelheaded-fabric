package me.bluejay.levelheaded.core.data;

public enum Units {
    METERS(1.0, "m"),
    FEET(3.280839895, "ft"),
    CHAINS(0.049709695, "ch"),
    MILES(0.00062137119, "mi");

    private final double fromMeters;
    private final String symbol;

    Units(double fromMeters, String symbol) {
        this.fromMeters = fromMeters;
        this.symbol = symbol;
    }

    public double toUnit(double meters) {
        return meters * fromMeters;
    }

    public double toMeters(double value) {
        return value / fromMeters;
    }

    public String symbol() {
        return symbol;
    }

    /** Optional ground-from-grid factor. 1.0 = raw blocks (grid). */
    public final class ProjectionCorrection {
        private boolean mercator;
        private double latitudeDeg = 0.0; // used when mercator

        public void setMercator(boolean on) { this.mercator = on; }
        public boolean isMercator() { return mercator; }

        public void setLatitudeDeg(double lat) {
            this.latitudeDeg = Math.max(-85.0, Math.min(85.0, lat));
        }

        public double latitudeDeg() { return latitudeDeg; }

        /** Multiply horizontal block metres by this to get ground metres. */
        public double horizontalFactor() {
            if (!mercator) return 1.0;
            double cos = Math.cos(Math.toRadians(latitudeDeg));
            return Math.max(1e-6, cos); // avoid pole blow-up
        }
    }
}