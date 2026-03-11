/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author recsy
 */
public class CredentialBootstrapper {

    public static void main(String[] args) throws Exception {

        PasswordService ps = new PasswordService();
        
        System.out.println("ADMIN hash: " + ps.hash("admin123"));
        System.out.println("IT hash: " + ps.hash("it123"));
        System.out.println("HR hash: " + ps.hash("hr123"));
        System.out.println("ACCT hash: " + ps.hash("acct123"));
        System.out.println("EMP hash: " + ps.hash("emp123"));
    }
}


