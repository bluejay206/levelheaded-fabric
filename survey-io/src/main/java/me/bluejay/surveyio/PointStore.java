package me.bluejay.surveyio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable list of survey points. Thread-safe for simple add/read use.
 */
public final class PointStore {
    private final List<SurveyPoint> points = new ArrayList<>();

    public void add(SurveyPoint point) {
        if (point != null) {
            points.add(point);
        }
    }

    public List<SurveyPoint> points() {
        return Collections.unmodifiableList(new ArrayList<>(points));
    }

    public void clear() {
        points.clear();
    }

    public int size() {
        return points.size();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }
}
