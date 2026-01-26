package com.sentinel.secure_vault.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";

    /**
     * Decrypts a byte array using a Base64 encoded string key.
     * * @param encryptedData The raw bytes read from the file.
     * @param base64Key     The encryption key stored in the database.
     * @return              The decrypted file bytes.
     */
    public static byte[] decrypt(byte[] encryptedData, String base64Key) throws Exception {
        // 1. Decode the Base64 key back into raw bytes
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        // 2. Reconstruct the SecretKey
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

        // 3. Initialize Cipher for Decryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // 4. Decrypt
        return cipher.doFinal(encryptedData);
    }

    /**
     * (Optional) Encrypt helper if needed elsewhere
     */
    public static byte[] encrypt(byte[] data, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }
}