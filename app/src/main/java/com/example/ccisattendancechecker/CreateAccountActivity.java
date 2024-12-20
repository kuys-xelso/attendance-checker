package com.example.ccisattendancechecker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserRegistrationManager registrationManager;

    private ProgressBar progressBar;
    private EditText edt_password, edt_confirm_password, edt_email, edt_student_id;
    private Button btn_create_account;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Navigate to the login activity
        TextView btn_tv_login = findViewById(R.id.btn_tv_login);
        btn_tv_login.setOnClickListener(v -> {
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
        });

        registrationManager = new UserRegistrationManager();

        progressBar = findViewById(R.id.progressBar);
        edt_password = findViewById(R.id.edt_password);
        edt_confirm_password =findViewById(R.id.edt_confirm_password);
        edt_email = findViewById(R.id.edt_email);
        edt_student_id = findViewById(R.id.edt_student_id);
        btn_create_account = findViewById(R.id.createAccount_button);

        //password toggle
        setupPasswordToggle(edt_password,true);
        setupPasswordToggle(edt_confirm_password, false);

        //focus listener handling
        setupAutoHideOnFocusLoss(edt_password, true);
        setupAutoHideOnFocusLoss(edt_confirm_password, false);

        btn_create_account.setOnClickListener(v -> attemptRegistration());

    }


    private void attemptRegistration(){
        String studentId = edt_student_id.getText().toString().trim();
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();
        String confirmPassword = edt_confirm_password.getText().toString().trim();

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Student ID is required", Toast.LENGTH_SHORT).show();
            edt_student_id.requestFocus();
            return;
        }
        if (email.isEmpty()) {
           Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            edt_email.requestFocus();
            return;
        }
        if (!email.contains("@bisu.edu.ph")) {
            Toast.makeText(this, "Please use BISU email account", Toast.LENGTH_SHORT).show();
            edt_email.requestFocus();
            return;
        }
        if (password.isEmpty()) {
           Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            edt_password.requestFocus();
            return;
        }
        if (confirmPassword.isEmpty()) {
          Toast.makeText(this, "Confirm Password is required", Toast.LENGTH_SHORT).show();
            edt_confirm_password.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            edt_confirm_password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registrationManager.checkAndRegisterUser(studentId, email, password,
                new UserRegistrationManager.RegistrationCallback(){
                    @Override
                    public void onSuccess() {
                        // Registration successful
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateAccountActivity.this,
                                "Registration successful",
                                Toast.LENGTH_SHORT).show();
                        // Navigate to login or main screen
                        startActivity(new Intent(CreateAccountActivity.this,
                                LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        // Registration failed
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateAccountActivity.this,
                                error,
                                Toast.LENGTH_LONG).show();
                    }
                });
   }


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