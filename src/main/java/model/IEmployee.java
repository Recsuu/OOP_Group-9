package model;

/**
 * =============================================================
 * INTERFACE: IEmployee
 * =============================================================
 *
 * PURPOSE
 * Defines the required methods that every employee
 * representation must implement.
 *
 * This interface helps enforce consistency across
 * employee-related classes.
 *
 * OOP PRINCIPLE
 * -------------------------------------------------------------
 * Abstraction
 * - The interface declares required methods
 * - The implementation is provided by the Employee class
 *
 * IMPLEMENTED BY
 * -------------------------------------------------------------
 * Employee.java
 */

public interface IEmployee {

    String getEmployeeNumber();

    String getFirstName();

    String getLastName();

    String getFullName();

    String getPosition();

    double getBasicSalary();

    double getHourlyRate();
}