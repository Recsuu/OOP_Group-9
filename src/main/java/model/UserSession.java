package model;

import roles.User;

/**
 * =============================================================
 * CLASS: UserSession
 * =============================================================
 *
 * PURPOSE
 * Represents the currently logged-in user session.
 *
 * It stores the active user information so that the
 * system can apply role-based permissions.
 */

public class UserSession {

    private final User user;
    private final UserAccount account;
    private final Employee employee;

    public UserSession(User user, UserAccount account, Employee employee) {
        this.user = user;
        this.account = account;
        this.employee = employee;
    }

    public User getUser() { return user; }
    public UserAccount getAccount() { return account; }
    public Employee getEmployee() { return employee; }

}