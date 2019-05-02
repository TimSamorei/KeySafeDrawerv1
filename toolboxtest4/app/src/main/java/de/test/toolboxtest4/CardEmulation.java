package de.test.toolboxtest4;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
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


    private final static byte APPLET_CLA = (byte) 0x80;
    private final static byte INS_GETSIGNATURE = (byte) 0xA0;
    private final static byte INS_GETCERT = (byte) 0xB0;
    private final static byte INS_GETDATA = (byte) 0xC0;

    byte[] response;
    byte[] cert;
    byte[] signature;
    byte[] data;
    String[] dataArray;

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "Incoming APDU: " + Arrays.toString(apdu));
        Log.d(TAG, "Incoming APDU(String): " + new String(apdu));

        if (Arrays.equals(apdu, SELECT_PKI_APPLET_CMD)) {
            Log.d(TAG, "Applet selected");
            return toBytes(SW_SUCCESS);
        }

        byte ins = apdu[OFFSET_INS];
        int dataLen = apdu[OFFSET_LC];

        switch (ins) {
            case INS_GETSIGNATURE:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                dataArray = splitData(data);
                signature = Crypto.sign(dataArray[0].getBytes(), dataArray[1]);
                response = createResponse(Integer.toString(signature.length).getBytes());
                return response;

            case INS_GETCERT:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                cert = KeySafe.getCert(data);
                response = createResponse(Integer.toString(cert.length).getBytes());
                return response;

            case INS_GETDATA:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                dataArray = splitData(data);
                response = createResponse(getData(dataArray[0], new Integer(dataArray[1]), new Integer(dataArray[2])));
                return response;

                default:
                    return toBytes(SW_UNKNOWN);
        }

    }

    private byte[] getData(String get, int numBytes, int chunck) {
        switch (get) {
            case "certificate":
                byte[] datacert = new byte[numBytes];
                System.arraycopy(cert, chunck*200, datacert, 0, numBytes);
                return datacert;

            case "signature":
                byte[] datasig = new byte[numBytes];
                System.arraycopy(signature, chunck*200, datasig, 0, numBytes);
                return datasig;

                default:
                    return null;
        }
    }

    private byte[] createResponse(byte[] bytes) {
        byte[] response = new byte[bytes.length + 2];
        System.arraycopy(bytes, 0, response, 0, bytes.length);
        System.arraycopy(toBytes(SW_SUCCESS), 0, response, bytes.length, 2);
        Log.d(TAG, "Outgoing APDU: " + Arrays.toString(response));
        Log.d(TAG, "Outgoing APDU(String): " + new String(response));
        return response;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Disconnect reason: " + reason);
    }

    private byte[] toBytes(short s) {
        return new byte[] { (byte) ((s & 0xff00) >> 8), (byte) (s & 0xff) };
    }

    private String[] splitData(byte[] data) {
        String[] ret = new String(data).split("#");
        return ret;
    }
}