package com.sentinel.secure_vault.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // Standard IV length for GCM
    private static final int GCM_TAG_LENGTH = 128; // Authentication Tag length

    // 1. GENERATE A NEW SECRET KEY
    // We will call this every time a new file is uploaded.
    public SecretKey generateKey() throws Exception {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // AES-256 for maximum security
        return keyGen.generateKey();
    }

    // 2. ENCRYPT DATA (The "Locking" Process)
    // Returns: [IV] + [Encrypted Data] combined
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        // A. Generate a random IV (The "Spice")
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // B. Initialize the Cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        // C. Perform the Encryption
        byte[] encryptedBytes = cipher.doFinal(data);

        // D. Combine IV + Encrypted Data
        // We need the IV later to decrypt, so we stick it at the front.
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedBytes);
        return byteBuffer.array();
    }

    // 3. DECRYPT DATA (The "Unlocking" Process)
    // Expects: [IV] + [Encrypted Data]
    public byte[] decrypt(byte[] encryptedDataWithIv, SecretKey key) throws Exception {
        // A. Extract the IV (First 12 bytes)
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedDataWithIv);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv); // Reads the first 12 bytes into 'iv'

        // B. Extract the actual Encrypted Content (The rest)
        byte[] encryptedContent = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedContent);

        // C. Initialize Cipher for Decryption
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        // D. Decrypt
        return cipher.doFinal(encryptedContent);
    }

    // Helper: Convert Key to String (to store in DB)
    public String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Helper: Convert String back to Key (to use for decryption)
    public SecretKey decodeKey(String keyStr) {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }
}