package de.test.toolboxtest4;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;

class KeySafe {

    private static int counter = 0;

    public static KeyListItem[] getKeyList() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ArrayList<String> aliases = Collections.list(ks.aliases());
            KeyListItem[] ret = new KeyListItem[aliases.size()];
            int i = 0;
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
                ret[i] = new KeyListItem(alias, algorithm+" KEY", "Publickey: " + format + "...");
                i++;
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

    public static void genKey() {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                    kpg.initialize(new KeyGenParameterSpec.Builder(
                            "alias " + counter,
                            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                            .setDigests(KeyProperties.DIGEST_SHA256,
                                    KeyProperties.DIGEST_SHA512)
                            .build());
                    counter++;
                    KeyPair kp = kpg.generateKeyPair();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
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
}
