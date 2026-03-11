package model;

/**
 * =============================================================
 * CLASS: UserAccount
 * =============================================================
 *
 * PURPOSE
 * Represents the login identity of a system user.
 *
 * This class links authentication data with an
 * employee record.
 *
 * CONNECTION
 * -------------------------------------------------------------
 * UserAccount
 *      ↓
 * Employee.employeeNumber
 */

public class UserAccount {

    private final String username;
    private final String role;
    private final String employeeNumber;

    public UserAccount(String username, String role, String employeeNumber) {
        this.username = username;
        this.role = role;
        this.employeeNumber = employeeNumber;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getEmployeeNumber() { return employeeNumber; }

}