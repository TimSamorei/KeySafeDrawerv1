package de.test.toolboxtest4;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Crypto {

    private static final String TAG = "CardEmulation";
    private static final byte[] FIXED_IV = new byte[]{ 55, 54, 53, 52, 51, 50,
            49, 48, 47,
            46, 45, 44 };

    public static byte[] sign(byte[] signData, String keyAlias) {
        try {
            PrivateKey privateKey = KeySafe.getKey(keyAlias);
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(privateKey);
            sig.update(signData);
            return sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(String data, String key, String mechanism) {

        SecretKey sk = KeySafe.getSecretKey(key);

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sk,
                    new GCMParameterSpec(128, FIXED_IV));
            byte[] encodedBytes = cipher.doFinal(data.getBytes());
            return encodedBytes;
        } catch (NoSuchAlgorithmException e) {
            Log.v(TAG, "ALG");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Log.v(TAG, "PAD");
            e.printStackTrace();
        } catch (BadPaddingException e) {
            Log.v(TAG, "BADPAD");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Log.v(TAG, "INV");
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            Log.v(TAG, "BLOCKSIZE");
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            Log.v(TAG, "ALGPARA");
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(byte[] data, String key, String mechanism) {
        SecretKey sk = KeySafe.getSecretKey(key);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sk, new GCMParameterSpec(128, FIXED_IV));
            byte[] decodedBytes = cipher.doFinal(data);
            return decodedBytes;
        } catch (NoSuchAlgorithmException e) {
            Log.v(TAG, "NoSuchAlgo");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Log.v(TAG, "NoSuchPadd");
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            Log.v(TAG, "InvAlgo");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Log.v(TAG, "InvaldKey");
            e.printStackTrace();
        } catch (BadPaddingException e) {
            Log.v(TAG, "BadPadd");
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            Log.v(TAG, "BlockSize");
            e.printStackTrace();
        }
        Log.v(TAG, "RET NULL");
        return null;
    }
}
