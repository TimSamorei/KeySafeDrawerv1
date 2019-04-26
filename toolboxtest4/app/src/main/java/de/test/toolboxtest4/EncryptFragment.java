package de.test.toolboxtest4;

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

        enc_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                input.setText("test");
                output.setText("test");
                String inputt = input.getText().toString();
                Log.v("encc",inputt);
                String outputt = KeySafe.encrypt(input.getText().toString(), parent.getItemAtPosition(position).toString());
                Log.v("encc",outputt);
                output.setText(outputt);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }
}
