package org.example.controller;

import org.example.model.BusinessDetails;
import org.example.model.User;
import org.example.service.UserService;
import org.example.security.PasswordUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService = new UserService();
    private final Scanner sc = new Scanner(System.in);

    public int registerUser() {
        logger.info("===== REGISTER =====");
        logger.info("Choose account type:");
        logger.info("1 → Personal Account");
        logger.info("2 → Business Account");
        logger.info("Enter choice (1 or 2):");
        String accountChoice = sc.nextLine();
        
        String accountType = "PERSONAL";
        BusinessDetails businessDetails = null;
        
        if ("2".equals(accountChoice)) {
            accountType = "BUSINESS";
            businessDetails = collectBusinessDetails();
        }
        

        logger.info("Enter full name:");
        String fullName = sc.nextLine();
        logger.info("Enter email:");
        String email = sc.nextLine();
        logger.info("Enter phone:");
        String phone = sc.nextLine();
        logger.info("Enter password:");
        String password = sc.nextLine();
        logger.info("Enter transaction PIN:");
        String pin = sc.nextLine();
        

        logger.info("\n===== SECURITY QUESTION =====");
        logger.info("Choose a security question:");
        logger.info("1 → What is your mother's maiden name?");
        logger.info("2 → What was the name of your first pet?");
        logger.info("3 → What city were you born in?");
        logger.info("4 → What is your favorite book?");
        logger.info("5 → What was your childhood nickname?");
        logger.info("Enter choice (1-5):");
        String questionChoice = sc.nextLine();
        
        String securityQuestion = switch (questionChoice) {
            case "1" -> "What is your mother's maiden name?";
            case "2" -> "What was the name of your first pet?";
            case "3" -> "What city were you born in?";
            case "4" -> "What is your favorite book?";
            case "5" -> "What was your childhood nickname?";
            default -> "What is your mother's maiden name?";
        };
        
        logger.info("Enter your answer:");
        String securityAnswer = sc.nextLine();

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setTransactionPinHash(PasswordUtil.hash(pin));
        user.setAccountType(accountType);
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswerHash(PasswordUtil.hash(securityAnswer));

        int userId = userService.registerUser(user, businessDetails);
        if (userId > 0) {
            logger.info("User registered successfully, userId={}", userId);
            if ("BUSINESS".equals(accountType)) {
                logger.info("Business account created. Verification pending.");
            }
        } else {
            logger.warn("User registration failed");
        }
        return userId;
    }
    
    private BusinessDetails collectBusinessDetails() {
        logger.info("\n===== BUSINESS DETAILS =====");
        logger.info("Enter business name:");
        String businessName = sc.nextLine();
        
        logger.info("Enter business type (e.g., Retail, Service, Manufacturing, IT, etc.):");
        String businessType = sc.nextLine();
        
        logger.info("Enter tax ID / GST number:");
        String taxId = sc.nextLine();
        
        logger.info("Enter business address:");
        String address = sc.nextLine();
        
        logger.info("Enter verification document details (e.g., Registration number, Certificate ID):");
        String verificationDoc = sc.nextLine();
        
        BusinessDetails bd = new BusinessDetails();
        bd.setBusinessName(businessName);
        bd.setBusinessType(businessType);
        bd.setTaxId(taxId);
        bd.setAddress(address);
        bd.setVerificationDoc(verificationDoc);
        bd.setVerifiedStatus(false); // Default to unverified
        
        return bd;
    }

    public User loginUser() {
        logger.info("===== LOGIN =====");
        logger.info("Enter email or phone:");
        String input = sc.nextLine();
        
        // First check if account exists and is locked
        User existingUser = userService.getUserByEmailOrPhone(input);
        if (existingUser != null && existingUser.isAccountLocked()) {
            logger.error("ACCOUNT LOCKED: Your account has been locked due to multiple failed login attempts.");
            logger.error("Please contact support or use 'Forgot Password' to reset your account.");
            return null;
        }
        
        logger.info("Enter password:");
        String password = sc.nextLine();

        User user = userService.login(input, password);
        if (user != null) {

            int generatedOTP = userService.generateOTP();
            logger.info("===== OTP VERIFICATION =====");
            logger.info("A 4-digit OTP has been generated: {}", generatedOTP);
            logger.info("Enter OTP:");
            String enteredOTP = sc.nextLine();
            
            if (enteredOTP.equals(String.valueOf(generatedOTP))) {
                logger.info("OTP verified successfully!");
                logger.info("LOGIN SUCCESSFUL. Welcome {}", user.getFullName());
                return user;
            } else {
                logger.warn("Invalid OTP. Login failed.");
                return null;
            }
        } else {

            if (existingUser != null) {
                int attempts = existingUser.getFailedAttempts() + 1;
                if (attempts >= 3) {
                    logger.error("ACCOUNT LOCKED: Too many failed login attempts. Your account has been locked.");
                    logger.error("Please use 'Forgot Password' option to reset your account.");
                } else {
                    logger.warn("Invalid credentials. Attempt {}/3. Account will be locked after 3 failed attempts.", attempts);
                }
            } else {
                logger.warn("Invalid credentials");
            }
        }
        return null;
    }

    public int generateOTP() {
        int otp = userService.generateOTP();
        logger.info("Your OTP is: {}", otp);
        return otp;
    }

    public boolean resetPassword(int userId) {
        logger.info("Enter new password:");
        String newPassword = sc.nextLine();
        boolean success = userService.resetPassword(userId, newPassword);
        if (success) {
            logger.info("Password updated successfully");
        } else {
            logger.warn("Password update failed");
        }
        return success;
    }

    public String getLoggedInUserName(int userId) {
        return userService.getUserNameById(userId);
    }

    public void forgotPassword() {
        logger.info("===== FORGOT PASSWORD =====");
        logger.info("Enter email or phone:");
        String input = sc.nextLine();
        
        User user = userService.getUserByIdentifier(input);
        if (user != null) {

            if (user.isAccountLocked()) {
                logger.info("Note: Your account is currently locked. Resetting password will unlock it.");
            }
            

            if (user.getSecurityQuestion() == null || user.getSecurityAnswerHash() == null) {
                logger.warn("Security question not set for this account. Please contact support.");
                return;
            }
            
            logger.info("Security Question: {}", user.getSecurityQuestion());
            logger.info("Enter your answer:");
            String answer = sc.nextLine();
            
            if (userService.verifySecurityAnswer(user.getUserId(), answer)) {
                logger.info("Security answer verified successfully!");
                logger.info("Enter new password:");
                String newPassword = sc.nextLine();
                logger.info("Confirm new password:");
                String confirmPassword = sc.nextLine();
                
                if (newPassword.equals(confirmPassword)) {
                    boolean success = userService.resetPassword(user.getUserId(), newPassword);
                    if (success) {
                        if (user.isAccountLocked()) {
                            logger.info("✓ Password updated successfully and account unlocked!");
                        } else {
                            logger.info("✓ Password updated successfully!");
                        }
                        logger.info("You can now login with your new password.");
                    } else {
                        logger.warn("Password update failed. Please try again.");
                    }
                } else {
                    logger.warn("Passwords do not match. Password reset cancelled.");
                }
            } else {
                logger.warn("Incorrect security answer. Password reset failed.");
            }
        } else {
            logger.warn("User not found with that email or phone.");
        }
    }
}
