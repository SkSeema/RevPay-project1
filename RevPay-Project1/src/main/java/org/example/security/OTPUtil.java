package org.example.security;

import java.security.SecureRandom;

public class OTPUtil {

    private OTPUtil() {}

    public static String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
