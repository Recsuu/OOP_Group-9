package model;

import java.time.LocalDate;

/**
 * =============================================================
 * CLASS: PayrollPeriod
 * =============================================================
 *
 * PURPOSE
 * Represents the payroll coverage period.
 *
 * Example:
 * March 1 – March 15
 */

public class PayrollPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public PayrollPeriod(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

}