package com.example.ccisattendancechecker;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        Button changepassButton = findViewById(R.id.changePassBtn);
        currentPasswordEditText =  findViewById(R.id.current_password);
        newPasswordEditText = findViewById(R.id.new_password);
        confirmPasswordEditText = findViewById(R.id.retype_password);
        ImageView backbutton = findViewById(R.id.backButtonChangePass);
        progressBar = findViewById(R.id.progressBar);

        // password toggle listener
        setupPasswordToggle(currentPasswordEditText,true);
        setupPasswordToggle(newPasswordEditText,false);
        setupPasswordToggle(confirmPasswordEditText,false);

        //focus listener
        setupAutoHideOnFocusLoss(currentPasswordEditText,true);
        setupAutoHideOnFocusLoss(newPasswordEditText,false);
        setupAutoHideOnFocusLoss(confirmPasswordEditText,false);


        changepassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(newPassword)) {
            Toast.makeText(this, "Password must be at least 8 characters and include numbers, letters, and special characters.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Re-authenticate the user
            String email = user.getEmail();
            if (email != null) {
                progressBar.setVisibility(View.GONE);

                AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update password
                        user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                //progressbar hide


                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                finish();

                            } else {
                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                clearFields();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Re-authentication failed", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                });
            } else {
                Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
                clearFields();
            }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            clearFields();
        }
    }


    private boolean isValidPassword(String password) {
        // Check for at least 8 characters
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one letter
        if (!password.matches(".*[a-zA-Z].*")) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Check for at least one special character
        if (!password.matches(".*[@#$%^&+=!_].*")) {
            return false;
        }

        return true;
    }

    private void clearFields() {
        currentPasswordEditText.setText("");
        newPasswordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    //toggle password method
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText editText, boolean isPasswordField) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() -
                        editText.getCompoundDrawables()[2].getBounds().width() -
                        editText.getPaddingEnd())) {

                    if (isPasswordField) {
                        isPasswordVisible = !isPasswordVisible;
                        updatePasswordVisibility(editText, isPasswordVisible);
                    } else {
                        isConfirmPasswordVisible = !isConfirmPasswordVisible;
                        updatePasswordVisibility(editText, isConfirmPasswordVisible);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    // password focus listener
    private void setupAutoHideOnFocusLoss(EditText editText, boolean isPasswordField) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Automatically hide the password when focus is lost
                if (isPasswordField) {
                    isPasswordVisible = false;
                } else {
                    isConfirmPasswordVisible = false;
                }
                updatePasswordVisibility(editText, false);
            }
        });
    }

//update password visibility
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updatePasswordVisibility(EditText editText, boolean isVisible) {

        Drawable startDrawable = editText.getCompoundDrawables()[0];
        Drawable endDrawable ;

        if (isVisible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            endDrawable = getResources().getDrawable(R.drawable.icon_visibility_on);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            endDrawable = getResources().getDrawable(R.drawable.icon_visibility_off);
        }

        editText.setCompoundDrawablesWithIntrinsicBounds(
                startDrawable, null, endDrawable, null);

        // Move cursor to the end of the text
        editText.setSelection(editText.getText().length());
    }
}