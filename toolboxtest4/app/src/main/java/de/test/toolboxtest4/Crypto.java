package de.test.toolboxtest4;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

class Crypto {
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
}
