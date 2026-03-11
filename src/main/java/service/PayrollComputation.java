package service;

/**
 * =============================================================
 * SERVICE: PayrollComputation
 * =============================================================
 * PURPOSE
 * Performs all payroll-related calculations.
 *
 * RESPONSIBILITIES
 * - Attendance calculation
 * - Overtime calculation
 * - Allowance calculation
 * - Gross pay computation
 * - Deduction calculation
 *
 * DESIGN
 * This class only performs computations and does not access
 * any DAO or storage layer directly.
 */

import model.AttendanceRecord;
import model.DeductionBreakdown;
import model.Employee;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class PayrollComputation {

    /**
     * Container class holding summarized attendance results.
     */
    public static class AttendanceSummary {

        private final double hoursWorked;
        private final double overtimeHours;
        private final double lateDeduction;

        public AttendanceSummary(double hoursWorked, double overtimeHours, double lateDeduction) {
            this.hoursWorked = hoursWorked;
            this.overtimeHours = overtimeHours;
            this.lateDeduction = lateDeduction;
        }

        public double getHoursWorked() { return hoursWorked; }
        public double getOvertimeHours() { return overtimeHours; }
        public double getLateDeduction() { return lateDeduction; }
    }

    /**
     * Computes total attendance summary for a payroll period.
     */
    public static AttendanceSummary computeAttendance(List<AttendanceRecord> records, double hourlyRate) {

        // Sort records by date for consistent weekly grouping
        records.sort(Comparator.comparing(AttendanceRecord::getDate));

        double[] weeklyHours = new double[5];
        double[] weeklyOT = new double[5];

        double totalLateDeduction = 0.0;
        int workdayCount = 0;
        int currentWeek = 0;

        for (AttendanceRecord r : records) {

            // Move to next week after every 5 workdays
            if (workdayCount % 5 == 0 && workdayCount != 0) currentWeek++;
            workdayCount++;

            // Limit to maximum week index
            if (currentWeek >= 5) currentWeek = 4;

            double inTime = toDecimalHours(r.getLogIn());
            double outTime = toDecimalHours(r.getLogOut());

            // Compute regular daily hours
            double dailyHours = Math.max(0, Math.min(outTime, 17.0) - Math.max(inTime, 8.0) - 1.0);
            weeklyHours[currentWeek] += dailyHours;

            // Overtime calculation
            if (outTime > 17.0167) {
                weeklyOT[currentWeek] += (outTime - 17.0167);
            }

            // Late penalty calculation
            if (inTime > 8.1833 && currentWeek < 4) {
                totalLateDeduction += ((inTime - 8.1833) * hourlyRate);
            }
        }

        double totalHours = 0, totalOT = 0;

        for (int i = 0; i < 5; i++) {
            totalHours += weeklyHours[i];
            totalOT += weeklyOT[i];
        }

        return new AttendanceSummary(totalHours, totalOT, totalLateDeduction);
    }

    private static double toDecimalHours(LocalTime t) {
        return t.getHour() + (t.getMinute() / 60.0);
    }

    public static double computeWeeklyAllowance(Employee emp) {
        return (emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance()) / 4.0;
    }

    public static double computeOvertimePay(double otHours, double hourlyRate) {
        return otHours * hourlyRate * 1.25;
    }

    public static double computeGrossPay(double hoursWorked, double otHours, double hourlyRate, double weeklyAllowance) {
        double regularPay = hoursWorked * hourlyRate;
        double otPay = computeOvertimePay(otHours, hourlyRate);
        return regularPay + otPay + weeklyAllowance;
    }

    public static DeductionBreakdown computeDeductions(double gross, double lateDeduction) {

        double sss = calculateSSS(gross);
        double phil = calculatePhilHealth(gross);
        double pagibig = calculatePagibig(gross);
        double tax = calculateWithholdingTax(gross);
        double other = 0.0;

        double total = sss + phil + pagibig + tax + lateDeduction + other;

        return new DeductionBreakdown(sss, phil, pagibig, tax, lateDeduction, other, total);
    }

    private static double calculateSSS(double compensation) {
        double base = 3250, minSSS = 135.00, maxSSS = 1125.00;
        int steps = (int) ((compensation - base) / 500);
        return Math.min(maxSSS, Math.max(minSSS, minSSS + steps * 22.5));
    }

    private static double calculatePhilHealth(double compensation) {
        return compensation * 0.03;
    }

    private static double calculatePagibig(double compensation) {
        return Math.min(100.00, compensation * 0.02);
    }

    private static double calculateWithholdingTax(double compensation) {
        if (compensation <= 20832) return 0;
        else if (compensation <= 33333) return (compensation - 20832) * 0.2;
        else if (compensation <= 66667) return (compensation - 33333) * 0.25 + 2500;
        else if (compensation <= 166667) return (compensation - 66667) * 0.3 + 10833;
        else if (compensation <= 666667) return (compensation - 166667) * 0.32 + 40833.33;
        else return (compensation - 666667) * 0.35 + 200833.33;
    }
}