package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BusinessDetails {

    private static final Logger logger = LogManager.getLogger(BusinessDetails.class);

    private int businessId;
    private int userId;
    private String businessName;
    private String businessType;
    private String taxId;
    private String address;
    private String verificationDoc;
    private boolean verifiedStatus;

    public BusinessDetails() {
        logger.debug("BusinessDetails object created");
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVerificationDoc() {
        return verificationDoc;
    }

    public void setVerificationDoc(String verificationDoc) {
        this.verificationDoc = verificationDoc;
    }

    public boolean isVerifiedStatus() {
        return verifiedStatus;
    }

    public void setVerifiedStatus(boolean verifiedStatus) {
        this.verifiedStatus = verifiedStatus;
    }
}
