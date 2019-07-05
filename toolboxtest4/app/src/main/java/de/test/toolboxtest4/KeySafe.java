package de.test.toolboxtest4;

import android.app.AlertDialog;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

class KeySafe {

    private static int counter = 0;
    private static final String TAG = "KeySafe";

    public static ArrayList<KeyListItem> getKeyList() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ArrayList<String> aliases = Collections.list(ks.aliases());
            ArrayList<KeyListItem> ret = new ArrayList<KeyListItem>();
            for (String alias : aliases){
                KeyStore.Entry entry = ks.getEntry(alias, null);
                String algorithm = "FAILED";
                String format = "FAILED";
                try {
                    KeyStore.PrivateKeyEntry key = (KeyStore.PrivateKeyEntry) entry;
                    algorithm = key.getPrivateKey().getAlgorithm();
                    format = ((RSAPublicKey) key.getCertificate().getPublicKey()).getModulus().toString().substring(0, 15);
                } catch (Exception e) {
                    KeyStore.SecretKeyEntry key = (KeyStore.SecretKeyEntry) entry;
                    algorithm = key.getSecretKey().getAlgorithm();
                    format = key.getSecretKey().getFormat();
                }

                ret.add(new KeyListItem(alias, algorithm+" KEY", "Publickey: " + format + "..."));
            }
            return ret;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static KeyListItem genRSAKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            kpg.initialize(new KeyGenParameterSpec.Builder(
                    "alias " + counter,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                    .setKeySize(512)
                    .build());
            counter++;
            KeyPair kp = kpg.generateKeyPair();
            return new KeyListItem("alias " + counter, kp.getPrivate().getAlgorithm(), "Publickey: " + ((RSAPublicKey) kp.getPublic()).getModulus().toString().substring(0, 15) + "...");
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static KeyListItem genAESKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            kg.init(
                    new KeyGenParameterSpec.Builder("alias " + counter,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            // NOTE no Random IV. According to above this is less secure but acceptably so.
                            .setRandomizedEncryptionRequired(false)
                            .build());
            counter++;
            SecretKey sk = kg.generateKey();
            return new KeyListItem("alias " + counter, "AES", "Secretkey: " + sk.getFormat());

        } catch(Exception e) {
            Log.v("AES",e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static void flush() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ArrayList<String> aliases = Collections.list(ks.aliases());
            for (String alias : aliases) {
                ks.deleteEntry(alias);
            }
            } catch(KeyStoreException e){
                e.printStackTrace();
            } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String input, String alias) {
        KeyStore ks = null;
        Cipher cipher;
        byte [] encryptedBytes;
        String encrypted;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            cipher =  Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            PrivateKey privateKeyEntry = ((KeyStore.PrivateKeyEntry) ks.getEntry(alias, null)).getPrivateKey();
            OAEPParameterSpec sp = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry, sp);
            encryptedBytes = cipher.doFinal(input.getBytes());
            encrypted = new String(encryptedBytes);
            return encrypted;
        } catch (KeyStoreException e) {
            Log.v("encc","KEYSTORE" + e.getMessage());
            Log.v("encc",e.getMessage());
            return "ERROR";
        } catch (IOException e) {
            Log.v("encc","IO" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (CertificateException e) {
            Log.v("encc","CERT" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.v("encc","NOALGO" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            Log.v("encc","EXC" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Log.v("encc","PADDING" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (BadPaddingException e) {
            Log.v("encc","BADPAD" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            Log.v("encc","BLOCKSIZE" + e.getMessage());
            //Log.v("encc",e.getMessage());
            return "ERROR";
        } catch (InvalidKeyException e) {
            Log.v("encc","INVKEY" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            Log.v("encc","INVALGO" + e.getMessage());
            Log.v("encc",e.getMessage());
            e.printStackTrace();
        }


        return "ERROR";
    }

    public static byte[] sign(byte[] crData) {
        try {
            KeyStore ks = null;
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            PrivateKey privateKeyEntry = ((KeyStore.PrivateKeyEntry) ks.getEntry("alias 1", null)).getPrivateKey();

            Log.d(TAG, "Data to sign: " + Arrays.toString(crData));
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(privateKeyEntry);
            sig.update(crData);
            return sig.sign();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return "Error".getBytes();
    }

    public static byte[] getCert(byte[] aliasData) {
        try {
            KeyStore ks = null;
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.PrivateKeyEntry key =  (KeyStore.PrivateKeyEntry) ks.getEntry(new String(aliasData), null);

            Certificate cert = key.getCertificate();
            return cert.getEncoded();
        } catch (KeyStoreException e) {
            Log.v(TAG,"ERRROR");
            e.printStackTrace();
        } catch (IOException e) {
            Log.v(TAG,"ERRROR");
            e.printStackTrace();
        } catch (CertificateException e) {
            Log.v(TAG,"ERRROR");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.v(TAG,"ERRROR");
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            Log.v(TAG,"ERRROR");
            e.printStackTrace();
        }
        return "Error".getBytes();
    }

    public static PrivateKey getKey(String keyAlias) {
        try {
            KeyStore ks = null;
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            PrivateKey privateKeyEntry = ((KeyStore.PrivateKeyEntry) ks.getEntry(keyAlias, null)).getPrivateKey();
            return privateKeyEntry;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKey getSecretKey(String keyAlias) {
        try {
            KeyStore ks = null;
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            SecretKey secretKey = ((KeyStore.SecretKeyEntry) ks.getEntry(keyAlias, null)).getSecretKey();
            return secretKey;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkKey(String s) {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            return ks.containsAlias(s);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
