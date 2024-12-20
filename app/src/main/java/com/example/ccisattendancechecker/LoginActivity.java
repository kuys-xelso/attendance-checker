package com.example.ccisattendancechecker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize ViewPager and Adapter
        ViewPager buttonViewpager = findViewById(R.id.viewpager);
        VPAdapterButton adapter = new VPAdapterButton(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);


        // Add fragments to the adapter
        adapter.addFragment(new MayorFragment());
        adapter.addFragment(new StudentFragment());
        buttonViewpager.setAdapter(adapter);

        //initialization

        Button buttonMayor = findViewById(R.id.mayorButton);
        Button buttonStudent = findViewById(R.id.studentButton);

        buttonMayor.setOnClickListener(view -> {
            buttonViewpager.setCurrentItem(0);
            updateButtonStyles(buttonMayor, buttonStudent);
        });

        buttonStudent.setOnClickListener(view -> {
            buttonViewpager.setCurrentItem(1);
            updateButtonStyles(buttonStudent, buttonMayor);
        });


        buttonViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // Update button styles based on the selected page
                if (position == 0) {
                    updateButtonStyles(buttonMayor, buttonStudent);
                } else if (position == 1) {
                    updateButtonStyles(buttonStudent, buttonMayor);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateButtonStyles(Button activeButton, Button inactiveButton) {
        // For background drawable
        activeButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_btn));
        inactiveButton.setBackground(ContextCompat.getDrawable(this, R.drawable.inactive_btn));

        // For text colors
        activeButton.setTextColor(ContextCompat.getColor(this, R.color.activeTextColor));
        inactiveButton.setTextColor(ContextCompat.getColor(this, R.color.activeTextColor));
    }

}