package de.test.toolboxtest4;

import android.app.Service;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

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

    byte[] crData;

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
            return toBytes(SW_SUCCESS);
        }

        byte ins = cmd[OFFSET_INS];
        switch (ins) {
            case INS_VERIFY:
                int dataLen = cmd[OFFSET_LC];
                crData = Arrays.copyOfRange(cmd, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                Log.d(TAG, "Data to sign: " + Arrays.toString(crData));
                Runnable r = new Runnable() {


                    @Override
                    public void run() {
                        try {
                            byte[] signature = KeySafe.sign(crData);
                            byte[] response = new byte[signature.length + 2];
                            System.arraycopy(signature, 0, response, 0,
                                    signature.length);
                            System.arraycopy(toBytes(SW_SUCCESS), 0, response,
                                    signature.length, 2);
                            sendResponseApdu(response);
                        } catch (Exception e) {
                            Log.d(TAG, "Error: " + e.getMessage(), e);
                            sendResponseApdu(toBytes(SW_UNKNOWN));
                        }

                    }
                };
                Thread t = new Thread(r);
                t.start();

                return null;
        }

        return null;

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