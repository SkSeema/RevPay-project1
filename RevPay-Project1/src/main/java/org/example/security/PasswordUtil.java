package org.example.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(10));
    }

    public static boolean verify(String raw, String hashed) {

        // 🔥 IMPORTANT SAFETY CHECK
        if (hashed == null || !hashed.startsWith("$2")) {
            return false;
        }

        return BCrypt.checkpw(raw, hashed);
    }
}
