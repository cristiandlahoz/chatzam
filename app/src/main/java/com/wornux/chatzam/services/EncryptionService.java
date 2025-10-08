package com.wornux.chatzam.services;

import android.util.Base64;
import com.wornux.chatzam.utils.CryptoUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EncryptionService {
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    
    @Inject
    public EncryptionService() {
    }
    
    public String encrypt(String plainText, String base64Key) {
        try {
            byte[] keyBytes = Base64.decode(base64Key, Base64.NO_WRAP);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            
            byte[] iv = CryptoUtils.generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            String ivHex = bytesToHex(iv);
            String encryptedHex = bytesToHex(encryptedBytes);
            
            return ivHex + ":" + encryptedHex;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedData, String base64Key) {
        try {
            String[] parts = encryptedData.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }
            
            byte[] iv = hexToBytes(parts[0]);
            byte[] encryptedBytes = hexToBytes(parts[1]);
            
            byte[] keyBytes = Base64.decode(base64Key, Base64.NO_WRAP);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
