package com.example.ccisattendancechecker;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomeFragment extends Fragment {

 TabLayout tabLayout;
 ViewPager viewPager;
 private VPAdapter vpAdapter;
 private TextView usernameTextview;
 private TextView userRoleTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ShapeableImageView profileImage;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        profileImage = view.findViewById(R.id.profilePicImageView);
        usernameTextview = view.findViewById(R.id.nameTextview);
        userRoleTextView = view.findViewById(R.id.roleTextview);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tablayout);

        tabLayout.setupWithViewPager(viewPager);

         vpAdapter = new VPAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

         setupViewPager(vpAdapter);

         viewPager.setAdapter(vpAdapter);
         tabLayout.setupWithViewPager(viewPager);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String fname = bundle.getString("fname");
            String lname = bundle.getString("lname");
            String middleInitial = bundle.getString("middleInitial");
            String role = bundle.getString("userRole");
            String yearLevel = bundle.getString("yearLevel");
            String section = bundle.getString("section");
            String course = bundle.getString("course");
            String email = bundle.getString("email");
            String profileUrl = bundle.getString("profileUrl");

            loadProfilePicture(profileUrl);

            // Use the data as needed
         usernameTextview.setText(fname + " "+ middleInitial + ". " + lname);
         userRoleTextView.setText(role + "   "+ course + "  " + yearLevel + " - " + section);

        }

        return view;
    }

    private void loadProfilePicture(String profilePicUrl) {

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        // Load image using Glide
                        Glide.with(this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.avatar) // Show while loading
                                .error(R.drawable.avatar)      // Show if loading fails
                                .circleCrop()                          // Make image circular
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(profileImage);
                    } else {
                        // Set default profile picture if no URL is found
                        profileImage.setImageResource(R.drawable.avatar);
                    }
                }

    private void setupViewPager(VPAdapter vpdapter) {
        vpdapter.addFragment(new EventFragment(), "Events");
        vpdapter.addFragment(new RecordsFragment(), "Records");
    }
}




