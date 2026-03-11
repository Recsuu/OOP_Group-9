package roles;

import model.Permission;
import java.util.EnumSet;
import java.util.Set;

/**
 * =============================================================
 * CLASS: AdminUser
 * =============================================================
 *
 * PURPOSE
 * Represents a system administrator.
 *
 * Admin users have full control over the system
 * including employee management, payroll,
 * leave approvals, and support tickets.
 */

public class AdminUser extends User {

    public AdminUser(String username) {
        super(username);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(
                Permission.ADMIN_ALL,
                Permission.VIEW_EMPLOYEE,
                Permission.CRUD_EMPLOYEE,
                Permission.COMPUTE_PAYROLL,
                Permission.VIEW_PAYROLL,
                Permission.UPDATE_SALARY,
                Permission.VIEW_OWN_PAYSLIP,
                Permission.FILE_LEAVE,
                Permission.MANAGE_LEAVE,
                Permission.VIEW_AUDIT_LOGS,
                Permission.FILE_SUPPORT_TICKET,
                Permission.MANAGE_SUPPORT_TICKET
        );
    }
}