/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.CredentialDAO;
import model.Credential;
import model.Permission;
import model.Role;
import roles.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * =============================================================
 * SERVICE CLASS: UserDirectoryService
 * =============================================================
 *
 * PURPOSE
 * Handles operations related to system user accounts
 * stored in the credential CSV file.
 *
 * RESPONSIBILITIES
 * - Retrieve all user accounts
 * - Count active system users
 * - Filter employee numbers by role
 * - Build mappings between employee numbers and roles
 *
 * ARCHITECTURE ROLE
 * DAO Layer
 * -> CredentialDAO reads credential records from CSV storage
 *
 * Service Layer (THIS CLASS)
 * -> Applies filtering logic
 * -> Enforces permission checks
 *
 * Model Layer
 * -> Credential represents user account data
 * -> Role represents the user's system role
 *
 * SECURITY
 * Some operations require ADMIN permissions.
 */

public class UserDirectoryService {

    /**
     * DAO responsible for retrieving credential records.
     */
    private final CredentialDAO credentialDAO;

    /**
     * Constructor injecting CredentialDAO dependency.
     */
    public UserDirectoryService(CredentialDAO credentialDAO) {
        this.credentialDAO = credentialDAO;
    }

    /**
     * Retrieves all system users.
     *
     * Intended for HR, IT, or Admin use.
     * Permission checks are typically handled
     * in both UI and service layers.
     */
    public List<Credential> listAllUsers(User currentUser) throws Exception {

        if (currentUser == null)
            throw new Exception("User required.");

        return credentialDAO.findAll();
    }

    /**
     * Returns the number of active user accounts.
     *
     * For this CSV-based system, "active users"
     * simply means total credential records.
     *
     * Used by the IT Dashboard tile:
     * "Total Active Users".
     */
    public int getActiveUserCount(User currentUser) throws Exception {

        // Only admin users may access this metric
        if (currentUser == null || !currentUser.can(Permission.ADMIN_ALL)) {
            throw new Exception("Access denied.");
        }

        return credentialDAO.findAll().size();
    }

    /**
     * Returns employee numbers filtered by role.
     *
     * If role is null, all employee numbers will be returned.
     */
    public List<String> employeeNumbersByRole(User currentUser, Role role) throws Exception {

        // Retrieve all credential records
        List<Credential> creds = listAllUsers(currentUser);

        /**
         * Filter credentials:
         * - Must have a valid employee number
         * - Must match the specified role (if provided)
         */
        return creds.stream()
                .filter(c -> c.getEmployeeNumber() != null && !c.getEmployeeNumber().isBlank())
                .filter(c -> role == null || c.getRole() == role)
                .map(Credential::getEmployeeNumber)
                .distinct() // remove duplicates
                .collect(Collectors.toList());
    }

    /**
     * Creates a mapping between employee numbers and their roles.
     *
     * Example result:
     * EMP001 -> ADMIN
     * EMP002 -> HR
     * EMP003 -> ACCOUNTANT
     */
    public Map<String, Role> roleByEmployeeNumber(User currentUser) throws Exception {

        List<Credential> creds = listAllUsers(currentUser);

        Map<String, Role> map = new HashMap<>();

        for (Credential c : creds) {

            // Skip invalid or empty employee numbers
            if (c.getEmployeeNumber() == null || c.getEmployeeNumber().isBlank())
                continue;

            // Map employee number to role
            map.put(c.getEmployeeNumber(), c.getRole());
        }

        return map;
    }
}