package model;

import java.time.LocalDate;

/**
 * =============================================================
 * CLASS: Payslip
 * =============================================================
 *
 * PURPOSE
 * Represents the payroll output for an employee.
 *
 * This object contains all payroll calculation results
 * including:
 * - hours worked
 * - gross pay
 * - deductions
 * - net pay
 */

public class Payslip {

    private String employeeNumber;
    private String employeeName;
    private String position;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private double hoursWorked;
    private double overtimeHours;

    private double grossPay;
    private DeductionBreakdown deductions;
    private double netPay;

    public Payslip(String employeeNumber,
                   String employeeName,
                   String position,
                   LocalDate periodStart,
                   LocalDate periodEnd,
                   double hoursWorked,
                   double overtimeHours,
                   double grossPay,
                   DeductionBreakdown deductions,
                   double netPay) {

        this.employeeNumber = employeeNumber;
        this.employeeName = employeeName;
        this.position = position;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.hoursWorked = hoursWorked;
        this.overtimeHours = overtimeHours;
        this.grossPay = grossPay;
        this.deductions = deductions;
        this.netPay = netPay;
    }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    public double getGrossPay() { return grossPay; }
    public void setGrossPay(double grossPay) { this.grossPay = grossPay; }

    public DeductionBreakdown getDeductions() { return deductions; }
    public void setDeductions(DeductionBreakdown deductions) { this.deductions = deductions; }

    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }

}