package roles;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * =============================================================
 * CLASS: RoleFactory
 * =============================================================
 *
 * PURPOSE
 * Creates role-specific user objects dynamically
 * based on the role stored in the database or CSV.
 *
 * DESIGN PATTERN
 * -------------------------------------------------------------
 * Factory Pattern
 *
 * Instead of manually creating objects like:
 * new AdminUser(...)
 * new HRUser(...)
 *
 * The system uses RoleFactory to create the correct
 * role class automatically.
 *
 * Example:
 * User user = RoleFactory.create("HR", "john.doe");
 *
 * This keeps the authentication system flexible
 * and easy to extend.
 */

public class RoleFactory {

    private static final Map<String, Function<String, User>> registry = new HashMap<>();

    static {
        registry.put("ADMIN", AdminUser::new);
        registry.put("IT", ITUser::new);
        registry.put("HR", HRUser::new);
        registry.put("ACCOUNTANT", AccountantUser::new);
        registry.put("EMPLOYEE", EmployeeUser::new);
    }

    public static User create(String role, String username) {

        Function<String, User> creator = registry.get(role.toUpperCase());

        if (creator == null) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }

        return creator.apply(username);
    }
}