package com.example.selenium.common.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CryptoUtils {

    private static int keySize = 8;
    private static int ivSize = 4;
    private static int iterations = 1;
    private static String hashAlgorithm = "MD5";



    public static String createSalt(int length) {

        // 1. Random, byte 객체 생성
        SecureRandom r = new SecureRandom();
        byte[] salt = new byte[length];

        // 2. 난수 생성
        r.nextBytes(salt);

        // 3. byte to String (10진수의 문자열로 변경
        StringBuffer sb = new StringBuffer();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        };

        return sb.toString();
    }

    public static Map<String, byte[]> EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        return EvpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
    }

    private static Map<String, byte[]> EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations, String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {

        keySize = keySize / 32;
        ivSize = ivSize / 32;

        int targetKeySize = keySize + ivSize;
        byte[] derivedBytes = new byte[targetKeySize * 4];
        int numberOfDerivedWords = 0;
        byte[] block = null;
        MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hasher.update(block);
            }
            hasher.update(password);
            // Salting
            block = hasher.digest(salt);
            hasher.reset();
            // Iterations : 키 스트레칭(key stretching)
            for (int i = 1; i < iterations; i++) {
                block = hasher.digest(block);
                hasher.reset();
            }
            System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4, Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));
            numberOfDerivedWords += block.length / 4;
        }

        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);

        Map<String, byte[]> map = new ConcurrentHashMap<>();
        map.put("keyBytes", resultKey);
        map.put("ivBytes", resultIv);
        map.put("derivedBytes", derivedBytes);

        return map; // key + iv
    }
}
