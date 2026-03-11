package model;

/**
 * =============================================================
 * CLASS: DeductionBreakdown
 * LAYER: Model
 * =============================================================
 *
 * PURPOSE:
 * This class represents the breakdown of all payroll deductions
 * applied to an employee during payroll computation.
 *
 * These deductions are calculated by the PayrollService
 * and then stored in this object for easier access by the UI
 * and payroll reports.
 *
 * COMMON DEDUCTIONS INCLUDED:
 * - SSS (Social Security System)
 * - PhilHealth
 * - Pag-IBIG
 * - Tax
 * - Late deductions (from attendance)
 * - Other deductions
 *
 * USED BY:
 * - PayrollService (for deduction calculations)
 * - Payslip display in the UI
 * - Payroll breakdown reports
 *
 * IMPORTANT:
 * The "total" field is calculated by the service layer
 * and stored here for display and reporting purposes.
 *
 * OOP PRINCIPLE USED:
 * Encapsulation
 * - Fields are private
 * - Access is controlled using getters and setters
 */

public class DeductionBreakdown {

    /**
     * Social Security System deduction amount.
     */
    private double sss;

    /**
     * PhilHealth contribution deduction.
     */
    private double philHealth;

    /**
     * Pag-IBIG contribution deduction.
     */
    private double pagIbig;

    /**
     * Withholding tax deduction.
     */
    private double tax;

    /**
     * Deduction applied for employee lateness.
     */
    private double lateDeduction;

    /**
     * Other deductions not categorized above.
     */
    private double other;

    /**
     * Total deduction amount.
     * This value is calculated by the PayrollService.
     */
    private double total;

    /**
     * Constructor used to initialize deduction values.
     *
     * @param sss SSS deduction
     * @param philHealth PhilHealth deduction
     * @param pagIbig Pag-IBIG deduction
     * @param tax withholding tax
     * @param lateDeduction late penalty deduction
     * @param other other deductions
     * @param total total deductions
     */
    public DeductionBreakdown(double sss, double philHealth, double pagIbig, double tax,
                              double lateDeduction, double other, double total) {
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.tax = tax;
        this.lateDeduction = lateDeduction;
        this.other = other;
        this.total = total;
    }

    /**
     * Returns SSS deduction amount.
     */
    public double getSss() {
        return sss;
    }

    /**
     * Updates SSS deduction value.
     */
    public void setSss(double sss) {
        this.sss = sss;
    }

    /**
     * Returns PhilHealth deduction.
     */
    public double getPhilHealth() {
        return philHealth;
    }

    /**
     * Updates PhilHealth deduction value.
     */
    public void setPhilHealth(double philHealth) {
        this.philHealth = philHealth;
    }

    /**
     * Returns Pag-IBIG deduction.
     */
    public double getPagIbig() {
        return pagIbig;
    }

    /**
     * Updates Pag-IBIG deduction value.
     */
    public void setPagIbig(double pagIbig) {
        this.pagIbig = pagIbig;
    }

    /**
     * Returns withholding tax deduction.
     */
    public double getTax() {
        return tax;
    }

    /**
     * Updates tax deduction value.
     */
    public void setTax(double tax) {
        this.tax = tax;
    }

    /**
     * Returns deduction applied for lateness.
     */
    public double getLateDeduction() {
        return lateDeduction;
    }

    /**
     * Updates late deduction amount.
     */
    public void setLateDeduction(double lateDeduction) {
        this.lateDeduction = lateDeduction;
    }

    /**
     * Returns other deduction amount.
     */
    public double getOther() {
        return other;
    }

    /**
     * Updates other deductions.
     */
    public void setOther(double other) {
        this.other = other;
    }

    /**
     * Returns total deduction amount.
     */
    public double getTotal() {
        return total;
    }

    /**
     * Updates total deductions.
     */
    public void setTotal(double total) {
        this.total = total;
    }
}