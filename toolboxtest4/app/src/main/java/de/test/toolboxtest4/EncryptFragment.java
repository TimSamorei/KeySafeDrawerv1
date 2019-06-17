package de.test.toolboxtest4;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class EncryptFragment extends Fragment {

    EditText input;
    EditText output;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt, container, false);

        Spinner enc_spinner = (Spinner) view.findViewById(R.id.enc_spinner);
        ArrayList<String> dropdownitems = new ArrayList<String>();
        for (KeyListItem item : KeySafe.getKeyList()) {
            dropdownitems.add(item.getAlias());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, dropdownitems);
        enc_spinner.setAdapter(adapter);

        input = view.findViewById(R.id.enc_text_input);
        output = view.findViewById(R.id.enc_text_output);
        input.setText("test");

        enc_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                String inputt = input.getText().toString();
                byte[] outputt = Crypto.encrypt(input.getText().toString(), parent.getItemAtPosition(position).toString(), "");
                output.setText(toHex(outputt));
                CardEmulation.setKeyAlias(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder buff = new StringBuilder();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }
}
