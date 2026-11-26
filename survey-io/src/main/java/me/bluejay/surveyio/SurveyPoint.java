package me.bluejay.surveyio;

/**
 * A survey point in PNEZD form.
 * Coordinates are in survey space (Northing = -Z, Easting = X, Elevation = Y in block metres unless scaled).
 */
public record SurveyPoint(
        int number,
        double northing,
        double easting,
        double elevation,
        String description
) {
    public SurveyPoint {
        if (description == null) description = "";
    }

    public SurveyPoint(int number, double northing, double easting, double elevation) {
        this(number, northing, easting, elevation, "");
    }
}
