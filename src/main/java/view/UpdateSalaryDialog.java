/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author recsy
 */
import roles.User;
import service.EmployeeService;

import javax.swing.*;
import java.awt.*;

public class UpdateSalaryDialog extends JDialog {

    private final JTextField txtEmpNo = new JTextField(15);
    private final JTextField txtSalary = new JTextField(15);

    private final JButton btnSave = new JButton("Save");
    private final JButton btnCancel = new JButton("Cancel");

    private final User currentUser;
    private final EmployeeService employeeService;

    public UpdateSalaryDialog(Frame owner,
                              User currentUser,
                              EmployeeService employeeService,
                              String defaultEmpNo) {

        super(owner, "Update Basic Salary", true);
        this.currentUser = currentUser;
        this.employeeService = employeeService;

        setSize(380, 200);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(buildForm(defaultEmpNo), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
    }

    private JPanel buildForm(String defaultEmpNo) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        p.add(new JLabel("Employee #:"), gbc);

        gbc.gridx = 1;
        txtEmpNo.setText(defaultEmpNo == null ? "" : defaultEmpNo);
        p.add(txtEmpNo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        p.add(new JLabel("New Basic Salary:"), gbc);

        gbc.gridx = 1;
        p.add(txtSalary, gbc);

        return p;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private void onSave() {
        try {
            String empNo = txtEmpNo.getText().trim();
            String salaryRaw = txtSalary.getText().trim();

            if (empNo.isEmpty() || salaryRaw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            double newSalary;
            try {
                newSalary = Double.parseDouble(salaryRaw);
            } catch (NumberFormatException e) {
                throw new Exception("Salary must be a number.");
            }

            employeeService.updateBasicSalary(currentUser, empNo, newSalary);

            JOptionPane.showMessageDialog(this, "Salary updated successfully.");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
