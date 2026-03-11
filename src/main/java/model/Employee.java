package model;

import java.time.LocalDate;

/**
 * =============================================================
 * CLASS: Employee
 * LAYER: Model
 * =============================================================
 *
 * PURPOSE:
 * Represents an employee record in the MotorPH Payroll System.
 *
 * This class stores all employee information used by the system,
 * including personal details, government IDs, employment details,
 * and salary-related attributes.
 *
 * DATA SOURCE:
 * Data is loaded from the EmployeeDetails.csv file through
 * the EmployeeDAO class.
 *
 * ARCHITECTURE ROLE:
 * DAO Layer      → Reads/writes employee data from CSV
 * Service Layer  → Uses employee data for payroll computation
 * View Layer     → Displays employee records in the UI
 *
 * INTERFACE IMPLEMENTATION:
 * This class implements the IEmployee interface to enforce
 * required employee-related methods across the system.
 *
 * OOP PRINCIPLES USED
 * -------------------------------------------------------------
 * Encapsulation
 * - Fields are private
 * - Access controlled through getters and setters
 *
 * Abstraction
 * - Important methods declared in IEmployee interface
 *
 * Polymorphism
 * - Methods defined in the interface are implemented here
 *
 * Example:
 * getFullName()
 * getBasicSalary()
 * getHourlyRate()
 */

public class Employee implements IEmployee {

    /**
     * Unique identifier for the employee.
     * Example: 10001
     */
    private String employeeNumber;

    /**
     * Employee last name.
     */
    private String lastName;

    /**
     * Employee first name.
     */
    private String firstName;

    /**
     * Employee date of birth.
     */
    private LocalDate birthday;

    /**
     * Residential address of the employee.
     */
    private String address;

    /**
     * Contact number of the employee.
     */
    private String phoneNumber;

    /**
     * Government identification numbers used for payroll deductions.
     */
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagIbigNumber;

    /**
     * Employment status.
     * Example: Regular, Probationary, Contractual
     */
    private String status;

    /**
     * Job position of the employee.
     * Example: Payroll Manager, HR Manager, etc.
     */
    private String position;

    /**
     * Immediate supervisor of the employee.
     */
    private String immediateSupervisor;

    /**
     * Basic monthly salary of the employee.
     */
    private double basicSalary;

    /**
     * Allowances provided by the company.
     */
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;

    /**
     * Payroll calculation values used by the payroll service.
     */
    private double grossSemiMonthlyRate;
    private double hourlyRate;

    /**
     * Returns the unique employee number.
     */
    @Override
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    /**
     * Returns employee first name.
     */
    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns employee last name.
     */
    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the full name of the employee.
     */
    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns employee job position.
     */
    @Override
    public String getPosition() {
        return position;
    }

    /**
     * Returns employee basic salary.
     */
    @Override
    public double getBasicSalary() {
        return basicSalary;
    }

    /**
     * Returns employee hourly rate.
     */
    @Override
    public double getHourlyRate() {
        return hourlyRate;
    }

    /**
     * Constructor used to create an employee object
     * when loading records from the CSV file.
     */
    public Employee(String employeeNumber, String lastName, String firstName, LocalDate birthday,
                    String address, String phoneNumber, String sssNumber, String philhealthNumber,
                    String tinNumber, String pagIbigNumber, String status, String position,
                    String immediateSupervisor, double basicSalary, double riceSubsidy,
                    double phoneAllowance, double clothingAllowance, double grossSemiMonthlyRate,
                    double hourlyRate) {

        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNumber = sssNumber;
        this.philhealthNumber = philhealthNumber;
        this.tinNumber = tinNumber;
        this.pagIbigNumber = pagIbigNumber;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        this.hourlyRate = hourlyRate;
    }

    /* =========================================================
       SETTERS AND GETTERS
       Used for updating employee information within the system
       ========================================================= */

    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }

    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }

    public String getPagIbigNumber() { return pagIbigNumber; }
    public void setPagIbigNumber(String pagIbigNumber) { this.pagIbigNumber = pagIbigNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setPosition(String position) { this.position = position; }

    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }

    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }

    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }

    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }

    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) { this.grossSemiMonthlyRate = grossSemiMonthlyRate; }

    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

}