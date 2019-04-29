package de.test.toolboxtest4;

import android.app.Service;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static de.test.toolboxtest4.ISO7816.OFFSET_CDATA;
import static de.test.toolboxtest4.ISO7816.OFFSET_INS;
import static de.test.toolboxtest4.ISO7816.OFFSET_LC;
import static de.test.toolboxtest4.ISO7816.SW_SUCCESS;
import static de.test.toolboxtest4.ISO7816.SW_UNKNOWN;

public class CardEmulation extends HostApduService {

    private static final String TAG = "CardEmulation";
    private static final byte[] SELECT_PKI_APPLET_CMD = {0x00, (byte) 0xA4,
            0x04, 0x00, 0x07, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00};

    private boolean selected = false;

    private final static byte APPLET_CLA = (byte) 0x80;
    private final static byte INS_VERIFY = (byte) 0xA0;
    private final static byte INS_GETCERT = (byte) 0xA1;
    private final static byte INS_GETCERT2 = (byte) 0xA2;

    byte[] crData;
    byte[] aliasData;
    byte[] cert;
    @Override
    public byte[] processCommandApdu(byte[] cmd, Bundle extras) {
        Log.d(TAG, "Incoming APDU: " + Arrays.toString(cmd));
        if (!selected) {
            if (Arrays.equals(cmd, SELECT_PKI_APPLET_CMD)) {
                selected = true;
                Log.d(TAG, "SELECT success");
                return toBytes(SW_SUCCESS);
            }

        }

        if (Arrays.equals(cmd, SELECT_PKI_APPLET_CMD)) {
            Log.d(TAG, "Already selected");
            return toBytes(SW_SUCCESS);
        }

        byte ins = cmd[OFFSET_INS];
        int dataLen = cmd[OFFSET_LC];
        if (ins == INS_VERIFY) {
            crData = Arrays.copyOfRange(cmd, OFFSET_CDATA, OFFSET_CDATA + dataLen);
            Log.d(TAG, "Data to sign: " + Arrays.toString(crData));
            byte[] signature = KeySafe.sign(crData);
            Log.d(TAG, "Sig: " + Arrays.toString(signature));
            byte[] response = new byte[signature.length + 2];
            System.arraycopy(signature, 0, response, 0, signature.length);
            System.arraycopy(toBytes(SW_SUCCESS), 0, response, signature.length, 2);
            Log.d(TAG, "Response(Sig): " + Arrays.toString(response));
            Log.d(TAG, "Response Length(Sig): " + response.length);
            return response;
        }

        if (ins == INS_GETCERT) {
            aliasData = Arrays.copyOfRange(cmd, OFFSET_CDATA, OFFSET_CDATA + dataLen);
            Log.d(TAG, "Get Cert of: " + Arrays.toString(aliasData));
            cert = KeySafe.getCert(aliasData);
            Log.d(TAG, "Cert: " + Arrays.toString(cert));
            Log.d(TAG, "Cert length: " + cert.length);
            byte[] response2 = new byte[200 + 2];
            System.arraycopy(cert, 0, response2, 0, 200);
            System.arraycopy(toBytes(SW_SUCCESS), 0, response2, 200, 2);
            Log.d(TAG, "Response(Cert): " + Arrays.toString(response2));
            Log.d(TAG, "Response Length(Cert): " + response2.length);
            return response2;
        }

        if (ins == INS_GETCERT2) {
            byte[] response3 = new byte[(cert.length - 200) + 2];
            System.arraycopy(cert, 200, response3, 0, (cert.length - 200));
            System.arraycopy(toBytes(SW_SUCCESS), 0, response3, (cert.length - 200), 2);
            Log.d(TAG, "Response(Cert2): " + Arrays.toString(response3));
            Log.d(TAG, "Response Length(Cert2): " + response3.length);
            return response3;
        }
        else {
            Log.d(TAG, "NEVER REACH THAT SHIT");
            return toBytes(SW_UNKNOWN);
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Disconnect reason: " + reason);
        selected = false;
    }

    private static byte[] toBytes(short s) {
        return new byte[] { (byte) ((s & 0xff00) >> 8), (byte) (s & 0xff) };
    }
}