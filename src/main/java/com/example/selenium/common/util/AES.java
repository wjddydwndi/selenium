package com.example.selenium.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AES {

    private static byte[] key; //16Byte == 128bit
    private static byte[] initVector; //16Byte

    //인코더 생성
    private static final Base64.Encoder enc = Base64.getEncoder();
    //디코더 생성
    private static final Base64.Decoder dec = Base64.getDecoder();

    public AES(byte[] key, byte[] initVector) {
        this.key = key;
        this.initVector = initVector;
    }

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector); // 초기화백터 byte로 변경
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES"); // byte로 변경

            //cipher를 만들
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //AES, CBC모드, partial block 채우기
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv); // mode

            //실제로 암호화 하는 부분
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return enc.encodeToString(encrypted); //암호문을 base64로 인코딩하여 출력 해줌

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(dec.decode(encrypted)); //base64 to byte

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
