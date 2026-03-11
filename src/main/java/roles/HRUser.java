package roles;

import model.Permission;
import java.util.EnumSet;
import java.util.Set;

/**
 * =============================================================
 * CLASS: HRUser
 * =============================================================
 *
 * PURPOSE
 * Represents Human Resources personnel.
 *
 * HR users manage employee records and
 * handle leave request approvals.
 */

public class HRUser extends User {

    public HRUser(String username) {
        super(username);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(
                Permission.CRUD_EMPLOYEE,
                Permission.VIEW_EMPLOYEE,
                Permission.MANAGE_LEAVE,
                Permission.FILE_LEAVE,
                Permission.FILE_SUPPORT_TICKET
        );
    }
}