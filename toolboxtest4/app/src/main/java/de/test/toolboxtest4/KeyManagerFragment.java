package de.test.toolboxtest4;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

public class KeyManagerFragment extends Fragment {

    private ArrayList<KeyListItem> keys;
    private KeyList keyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_keymanager, container, false);

        // 1. get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.keylist);

        // this is data fro recycler view
        keys = new ArrayList<KeyListItem>();
        keys = KeySafe.getKeyList();

        // 2. set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 3. create an adapter
        keyList = new KeyList(keys);
        // 4. set adapter
        recyclerView.setAdapter(keyList);
        // 5. set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Button button_keygen = (Button) view.findViewById(R.id.button_keygen);
        button_keygen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                keys.add(KeySafe.genKey());
                keyList.notifyDataSetChanged();
            }
        });
        Button button_flushkeysafe = (Button) view.findViewById(R.id.button_flushkeysafe);
        button_flushkeysafe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                KeySafe.flush();
                keys.clear();
                keyList.notifyDataSetChanged();
            }
        });

        return view;
    }
}
