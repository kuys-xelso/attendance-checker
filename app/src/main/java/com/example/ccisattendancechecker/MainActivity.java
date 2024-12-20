package com.example.ccisattendancechecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    AccountFragment accountFragment = new AccountFragment();
    RecordsFragment recordsFragment = new RecordsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        Intent intent = getIntent();
        String fname = intent.getStringExtra("fname");
        String lname = intent.getStringExtra("lname");
        String role = intent.getStringExtra("userRole");
        String yearLevel = intent.getStringExtra("yearLevel");
        String section = intent.getStringExtra("section");
        String email = intent.getStringExtra("email");
        String course = intent.getStringExtra("course");
        String middleInitial = intent.getStringExtra("middleInitial");
        String profileUrl = intent.getStringExtra("profileUrl");

        Bundle bundle = new Bundle();
        bundle.putString("fname", fname);
        bundle.putString("lname", lname);
        bundle.putString("userRole", role);
        bundle.putString("yearLevel", yearLevel);
        bundle.putString("section", section);
        bundle.putString("email", email);
        bundle.putString("course", course);
        bundle.putString("middleInitial", middleInitial);
        bundle.putString("profileUrl", profileUrl);

        homeFragment.setArguments(bundle);
        recordsFragment.setArguments(bundle);
        accountFragment.setArguments(bundle);


        //initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();

// bottom nav bar item selected listener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                    return true;
                }
                if (item.getItemId() == R.id.search){
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, accountFragment).commit();
                    return true;
                }
                return false;
            }
        });

        //initialize floating action bar
        FloatingActionButton generateQR = findViewById(R.id.generateQrFab);

       generateQR.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(MainActivity.this, GenerateEvent.class));
           }
       });
    }

    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}