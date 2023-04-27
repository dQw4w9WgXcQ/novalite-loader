package io.novalite.auth;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class Crypt {
    @SneakyThrows
    public static byte[] encrypt(String text) {
        String secret = "y/B?E(H+MbQeThWm";
        Key key = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(text.getBytes());
    }

    @SneakyThrows
    public static String decrypt(byte[] encrypted) {
        String secret = "y/B?E(H+MbQeThWm";
        Key key = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}
