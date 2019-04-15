package de.test.toolboxtest4;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private String alias = "TESTALIAS";
    private String emailadresse = "TEST@ALIAS.COM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**TextView text_alias = findViewById(R.id.text_alias);
        text_alias.setText(alias);

        TextView text_email = findViewById(R.id.text_email);
        text_email.setText(emailadresse);**/

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new KeyManagerFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_keymanager);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_encrypt:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EncryptFragment()).commit();
                break;
            case R.id.nav_decrypt:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DecryptFragment()).commit();
                break;
            case R.id.nav_cr:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CrFragment()).commit();
                break;
            case R.id.nav_keymanager:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new KeyManagerFragment()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
