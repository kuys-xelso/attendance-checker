package com.example.ccisattendancechecker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class StudentQrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_qr);

        ImageButton backButton = findViewById(R.id.backButton);


        Intent intent = getIntent();
        String qrContent = intent.getStringExtra("qrContent");

        if (qrContent != null) {
            Bitmap qrCodeBitmap = generateQrCode(qrContent);
            ImageView qrImageView = findViewById(R.id.qrImageView);
            qrImageView.setImageBitmap(qrCodeBitmap);
        }

        backButton.setOnClickListener(view -> {
           Intent intent1 = new Intent(StudentQrActivity.this, LoginActivity.class);
            startActivity(intent1);
            finish();
        });

    }

    private Bitmap generateQrCode(String qrCodeString) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(qrCodeString, BarcodeFormat.QR_CODE,900,900);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);

        }catch (WriterException e){
            throw new RuntimeException(e);
        }

    }
}