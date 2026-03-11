package model;

/**
 * =============================================================
 * CLASS: DashboardMetric
 * LAYER: Model
 * =============================================================
 *
 * PURPOSE:
 * This class represents a simple metric displayed on the system
 * dashboard of the MotorPH Payroll System.
 *
 * Dashboard metrics are commonly used to show summary
 * information such as:
 * - Total Employees
 * - Total Payroll Generated
 * - Pending Leave Requests
 * - Open Support Tickets
 *
 * Each metric contains:
 * - A title (name of the metric)
 * - A value (numeric or text value displayed on the dashboard)
 *
 * USED BY:
 * - HomePanel (dashboard UI)
 * - Dashboard analytics features
 *
 * DESIGN NOTE:
 * The fields are marked as "final" which means they cannot
 * be modified after the object is created. This ensures that
 * dashboard metrics remain constant once generated.
 *
 * OOP PRINCIPLE USED:
 * Encapsulation
 * - Fields are private
 * - Values are accessed through getter methods
 */

public class DashboardMetric {

    /**
     * Title of the dashboard metric.
     * Example: "Total Employees"
     */
    private final String title;

    /**
     * Value displayed for the metric.
     * Example: "42"
     */
    private final String value;

    /**
     * Constructor used to create a dashboard metric.
     *
     * @param title metric title
     * @param value metric value
     */
    public DashboardMetric(String title, String value) {
        this.title = title;
        this.value = value;
    }

    /**
     * Returns the title of the metric.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the value of the metric.
     */
    public String getValue() {
        return value;
    }
}