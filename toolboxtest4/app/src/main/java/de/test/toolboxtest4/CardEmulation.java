package de.test.toolboxtest4;

import android.app.AlertDialog;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.test.toolboxtest4.ISO7816.OFFSET_CDATA;
import static de.test.toolboxtest4.ISO7816.OFFSET_INS;
import static de.test.toolboxtest4.ISO7816.OFFSET_LC;
import static de.test.toolboxtest4.ISO7816.SW_DATA_INVALID;
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
    private final static byte INS_ENCRYPT = (byte) 0xD0;
    private final static byte INS_DECRYPT = (byte) 0xE0;
    private final static byte INS_INIT = (byte) 0xF0;

    private static String keyAlias = "alias 0";

    byte[] response;
    byte[] cert;
    byte[] signature;
    byte[] data;
    byte[] encdata;
    byte[] decdata;
    List<byte[]> dataList;

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "Incoming APDU: " + Arrays.toString(apdu));
        Log.d(TAG, "Incoming APDU(HEX): " + toHex(apdu));
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
                //dataArray = splitData(data);
                //signature = Crypto.sign(dataArray[0].getBytes(), dataArray[1]);
                response = createResponse(Integer.toString(signature.length).getBytes());
                return response;

            case INS_GETCERT:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                cert = KeySafe.getCert(data);
                response = createResponse(Integer.toString(cert.length).getBytes());
                return response;

            case INS_GETDATA:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                dataList = splitData(data);
                response = createResponse(getData(new String(dataList.get(0)), new Integer(new String(dataList.get(1))), new Integer(new String(dataList.get(2)))));
                return response;

            case INS_ENCRYPT:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                dataList = splitData(data);
                encdata = Crypto.encrypt(new String(dataList.get(0)), keyAlias, new String(dataList.get(2)));
                response = createResponse(Integer.toString(encdata.length).getBytes());
                return response;

            case INS_DECRYPT:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                dataList = splitData(data);
                decdata = Crypto.decrypt(dataList.get(0), keyAlias, new String(dataList.get(2)));
                response = createResponse(Integer.toString(decdata.length).getBytes());
                return response;

            case INS_INIT:
                data = Arrays.copyOfRange(apdu, OFFSET_CDATA, OFFSET_CDATA + dataLen);
                dataList = splitData(data);
                Log.d(TAG, "ALIASTEST: " + "alias " + new String(dataList.get(1)));
                boolean validKey = KeySafe.checkKey("alias " + new String(dataList.get(1)));
                Log.d(TAG, "ALIASTEST: " + validKey);

                if (validKey) {
                    setKeyAlias("alias " + new String(dataList.get(1)));
                    return toBytes(SW_SUCCESS);
                } else {
                    return toBytes(SW_DATA_INVALID);
                }

                default:
                    return toBytes(SW_UNKNOWN);
        }

    }

    private List<byte[]> splitData(byte[] data) {
        int lastDiv = data.length;
        List<byte[]> retList = new ArrayList<byte[]>();
        int counter = data.length-1;

        while(data[counter] != "#".getBytes()[0]) {
            counter--;
        }
        byte[] ret2 = new byte[lastDiv-counter-1];
        System.arraycopy(data, counter+1, ret2, 0, lastDiv-counter-1);
        lastDiv = counter;

        counter--;
        while(data[counter] != "#".getBytes()[0]) {
            counter--;
        }
        byte[] ret1 = new byte[lastDiv-counter-1];
        System.arraycopy(data, counter+1, ret1, 0, lastDiv-counter-1);
        lastDiv = counter;

        byte[] ret0 = new byte[counter];
        System.arraycopy(data, 0, ret0, 0, counter);

        retList.add(ret0);
        retList.add(ret1);
        retList.add(ret2);
        return retList;
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

            case "encryption":
                byte[] dataenc = new byte[numBytes];
                System.arraycopy(encdata, chunck*200, dataenc, 0, numBytes);
                return dataenc;

            case "decryption":
                byte[] datadec = new byte[numBytes];
                System.arraycopy(decdata, chunck*200, datadec, 0, numBytes);
                return datadec;

                default:
                    return null;
        }
    }

    private byte[] createResponse(byte[] bytes) {
        byte[] response = new byte[bytes.length + 2];
        System.arraycopy(bytes, 0, response, 0, bytes.length);
        System.arraycopy(toBytes(SW_SUCCESS), 0, response, bytes.length, 2);
        Log.d(TAG, "Outgoing APDU: " + Arrays.toString(response));
        Log.d(TAG, "Outgoing APDU(HEX): " + toHex(response));
        return response;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Disconnect reason: " + reason);
    }

    private byte[] toBytes(short s) {
        return new byte[] { (byte) ((s & 0xff00) >> 8), (byte) (s & 0xff) };
    }

    private static String toHex(byte[] bytes) {
        StringBuilder buff = new StringBuilder();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    public static void setKeyAlias(String keyAlias) {
        CardEmulation.keyAlias = keyAlias;
    }
}