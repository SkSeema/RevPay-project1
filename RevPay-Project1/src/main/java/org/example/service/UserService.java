package org.example.service;

import org.example.dao.BusinessDetailsDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.BusinessDetails;
import org.example.model.User;
import org.example.security.PasswordUtil;

import java.util.Random;

public class UserService {

    private final UserDAO userDAO;
    private final WalletDAO walletDAO;
    private final BusinessDetailsDAO businessDetailsDAO;
    private final Random random;

    // Default constructor for production use
    public UserService() {
        this.userDAO = new UserDAO();
        this.walletDAO = new WalletDAO();
        this.businessDetailsDAO = new BusinessDetailsDAO();
        this.random = new Random();
    }

    // Constructor for testing with dependency injection
    public UserService(UserDAO userDAO, WalletDAO walletDAO, BusinessDetailsDAO businessDetailsDAO) {
        this.userDAO = userDAO;
        this.walletDAO = walletDAO;
        this.businessDetailsDAO = businessDetailsDAO;
        this.random = new Random();
    }

    // Register user (overloaded to support business details)
    public int registerUser(User user) {
        return registerUser(user, null);
    }
    
    // Register user with optional business details
    public int registerUser(User user, BusinessDetails businessDetails) {
        int userId = userDAO.insertUser(user);
        if (userId > 0) {
            // Automatically create wallet for new user
            walletDAO.createWallet(userId);
            
            // If business account, save business details
            if (businessDetails != null && "BUSINESS".equals(user.getAccountType())) {
                businessDetails.setUserId(userId);
                businessDetailsDAO.insertBusinessDetails(businessDetails);
            }
        }
        return userId;
    }

    // Login with failed attempts tracking
    public User login(String emailOrPhone, String password) {
        User user = userDAO.getUserByEmailOrPhone(emailOrPhone);
        if (user == null) {
            return null;
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            return null; // Return null for locked accounts
        }

        // Verify password
        if (PasswordUtil.verify(password, user.getPasswordHash())) {
            // Successful login - reset failed attempts
            userDAO.resetFailedAttempts(user.getUserId());
            return user;
        } else {
            // Failed login - increment attempts
            userDAO.incrementFailedAttempts(user.getUserId());
            int newFailedAttempts = user.getFailedAttempts() + 1;
            
            // Lock account if 3 or more failed attempts
            if (newFailedAttempts >= 3) {
                userDAO.lockAccount(user.getUserId());
            }
            return null;
        }
    }

    // Generate 4-digit OTP
    public int generateOTP() {
        return 1000 + random.nextInt(9000);
    }

    // Fetch user by email/phone
    public User getUserByEmailOrPhone(String input) {
        return userDAO.getUserByEmailOrPhone(input);
    }

    // Reset password and unlock account
    public boolean resetPassword(int userId, String newPassword) {
        boolean passwordUpdated = userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
        if (passwordUpdated) {
            // Also unlock the account and reset failed attempts
            userDAO.unlockAccount(userId);
        }
        return passwordUpdated;
    }

    // Fetch username by userId
    public String getUserNameById(int userId) {
        User u = userDAO.getUserById(userId);
        return (u != null) ? u.getUsername() : null;
    }

    // Get user by identifier (email or phone)
    public User getUserByIdentifier(String identifier) {
        return userDAO.getUserByEmailOrPhone(identifier);
    }

    // Get userId by identifier (email or phone)
    public Integer getUserIdByIdentifier(String identifier) {
        User u = userDAO.getUserByEmailOrPhone(identifier);
        return (u != null) ? u.getUserId() : null;
    }

    // Verify security answer
    public boolean verifySecurityAnswer(int userId, String answer) {
        User user = userDAO.getUserById(userId);
        return user != null && user.getSecurityAnswerHash() != null && PasswordUtil.verify(answer, user.getSecurityAnswerHash());
    }
}
