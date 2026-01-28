package org.example.model;

import java.time.LocalDateTime;

public class User {

    private int userId;                     // user_id
    private String accountType;             // account_type (PERSONAL / BUSINESS)
    private String fullName;                // full_name
    private String email;                   // email
    private String phone;                   // phone
    private String passwordHash;            // password_hash
    private String transactionPinHash;      // transaction_pin_hash
    private String securityQuestion;         // security_question
    private String securityAnswerHash;       // security_answer_hash
    private int failedAttempts;              // failed_attempts
    private boolean accountLocked;           // account_locked
    private LocalDateTime createdAt;          // created_at

    // ---------- Getters & Setters ----------

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getTransactionPinHash() {
        return transactionPinHash;
    }

    public void setTransactionPinHash(String transactionPinHash) {
        this.transactionPinHash = transactionPinHash;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ---------- Convenience Methods for Controllers ----------
    
    public String getUsername() {
        return email;  // Using email as username
    }

    public void setUsername(String username) {
        this.email = username;  // Setting email as username
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public String getTransactionPin() {
        return transactionPinHash;
    }

    public void setTransactionPin(String pin) {
        this.transactionPinHash = pin;
    }
}
