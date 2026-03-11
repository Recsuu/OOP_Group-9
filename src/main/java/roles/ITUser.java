package roles;

import model.Permission;
import java.util.EnumSet;
import java.util.Set;

/**
 * =============================================================
 * CLASS: ITUser
 * =============================================================
 *
 * PURPOSE
 * Represents IT support personnel responsible for
 * system monitoring and support ticket management.
 */

public class ITUser extends User {

    public ITUser(String username) {
        super(username);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(
                Permission.FILE_LEAVE,
                Permission.VIEW_AUDIT_LOGS,
                Permission.FILE_SUPPORT_TICKET,
                Permission.MANAGE_SUPPORT_TICKET
        );
    }
}