package roles;

import model.Permission;
import java.util.EnumSet;
import java.util.Set;

/**
 * =============================================================
 * CLASS: AccountantUser
 * =============================================================
 *
 * PURPOSE
 * Represents the accounting department responsible
 * for payroll computation and salary management.
 */

public class AccountantUser extends User {

    public AccountantUser(String username) {
        super(username);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(
                Permission.COMPUTE_PAYROLL,
                Permission.UPDATE_SALARY,
                Permission.VIEW_PAYROLL,
                Permission.FILE_LEAVE,
                Permission.FILE_SUPPORT_TICKET
        );
    }
}