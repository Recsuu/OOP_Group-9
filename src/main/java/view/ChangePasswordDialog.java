/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author recsy
 *
 * =============================================================
 * ChangePasswordDialog
 * =============================================================
 * Dialog used to update the password of a user account.
 *
 * This dialog:
 * - Displays the current username (read-only)
 * - Accepts a new password
 * - Calls AuthService to update the password
 *
 * Architecture Layer: VIEW
 * It interacts with AuthService which handles the actual
 * password update logic.
 */

import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    /** Field where user enters the new password */
    private final JPasswordField txtNewPassword = new JPasswordField(20);

    /** Button to submit the new password */
    private final JButton btnSave = new JButton("Update Password");

    /** Button to cancel and close the dialog */
    private final JButton btnCancel = new JButton("Cancel");

    /** Service responsible for authentication operations */
    private final AuthService authService;

    /** Username of the logged-in user (passed from login) */
    private final String username;

    /**
     * Constructor for ChangePasswordDialog.
     *
     * @param owner parent frame
     * @param authService authentication service used to update password
     * @param username username whose password will be changed
     */
    public ChangePasswordDialog(Frame owner,
                                AuthService authService,
                                String username) {

        // Create modal dialog window
        super(owner, "Change Password", true);

        this.authService = authService;
        this.username = username;

        // Dialog window size
        setSize(400, 180);

        // Center dialog relative to parent window
        setLocationRelativeTo(owner);

        // Layout for dialog
        setLayout(new BorderLayout());

        // Add UI components
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        // Button event handlers
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
    }

    /**
     * Builds the form panel containing
     * username and new password fields.
     */
    private JPanel buildFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Spacing between components
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.anchor = GridBagConstraints.WEST;

        // Username label
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        // Username field (read-only)
        gbc.gridx = 1;

        JTextField txtUser = new JTextField(username, 20);

        txtUser.setEditable(false); // prevent editing username

        panel.add(txtUser, gbc);

        // Password label
        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(new JLabel("New Password:"), gbc);

        // Password input field
        gbc.gridx = 1;

        panel.add(txtNewPassword, gbc);

        return panel;
    }

    /**
     * Builds the bottom button panel.
     */
    private JPanel buildButtons() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        panel.add(btnCancel);
        panel.add(btnSave);

        return panel;
    }

    /**
     * Handles the save button action.
     *
     * Steps:
     * 1. Retrieve new password from field
     * 2. Validate that it is not empty
     * 3. Call AuthService to update password
     * 4. Show success message
     */
    private void onSave() {

        try {

            // Retrieve password from field
            String newPass = new String(txtNewPassword.getPassword());

            // Validate input
            if (newPass.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a new password."
                );

                return;
            }

            // Update password through AuthService
            authService.updatePassword(username, newPass);

            // Success message
            JOptionPane.showMessageDialog(
                    this,
                    "Password updated successfully."
            );

            // Close dialog
            dispose();

        } catch (Exception ex) {

            // Error handling
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}