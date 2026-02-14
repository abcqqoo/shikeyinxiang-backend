package com.example.diet.nutrition.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Week helper utilities.
 */
public final class WeekStartUtils {

    private WeekStartUtils() {
    }

    /**
     * Resolve any date (or an optional weekStart input) into the Monday of that week.
     * When input is null, uses today's date.
     */
    public static LocalDate resolveWeekStart(LocalDate input) {
        LocalDate base = input != null ? input : LocalDate.now();
        return base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}

