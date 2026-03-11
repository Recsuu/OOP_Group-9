package roles;

import model.Permission;
import java.util.EnumSet;
import java.util.Set;

/**
 * =============================================================
 * CLASS: EmployeeUser
 * =============================================================
 *
 * PURPOSE
 * Represents a regular employee using the system.
 *
 * Employees have limited access and can:
 * - View their own payslips
 * - File leave requests
 * - Submit support tickets
 */

public class EmployeeUser extends User {

    public EmployeeUser(String username) {
        super(username);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(
                Permission.VIEW_OWN_PAYSLIP,
                Permission.FILE_LEAVE,
                Permission.FILE_SUPPORT_TICKET
        );
    }
}