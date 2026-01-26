package com.sentinel.secure_vault.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";

    // 1. Generate a new Secret Key
    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // AES-256
        return keyGen.generateKey();
    }

    // 2. Encrypt Bytes (Used in Upload)
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    // 3. Decrypt Bytes using String Key (Used in Download)
    public byte[] decrypt(byte[] encryptedData, String base64Key) throws Exception {
        // Convert String Key back to SecretKey object
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

        // Decrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, originalKey);
        return cipher.doFinal(encryptedData);
    }

    // Helper: Convert Key to String for Database Storage
    public String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}