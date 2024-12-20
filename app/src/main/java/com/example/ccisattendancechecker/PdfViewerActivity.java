package com.example.ccisattendancechecker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.github.pdfviewer.PDFView;

import java.io.File;

public class  PdfViewerActivity extends AppCompatActivity {

    private PDFView pdfView;
    private File pdfFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pdf_viewer);

        pdfView = findViewById(R.id.pdfView);
        Button btnPrint = findViewById(R.id.btnPrint);

        // Get PDF file path from intent
        String pdfFilePath = getIntent().getStringExtra("pdfFilePath");
        pdfFile = new File(pdfFilePath);

        // Display the PDF
        if (pdfFile.exists()) {
            pdfView.fromFile(pdfFile)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .load();
        } else {
            finish();
        }

        btnPrint.setOnClickListener(v -> printPdf());
    }


    private void printPdf() {
        // Print the PDF using Android's PrintManager
        Uri pdfUri = Uri.fromFile(pdfFile);

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (printManager != null) {
            PdfDocumentAdapter printAdapter = new PdfDocumentAdapter(this, pdfUri);
            printManager.print("PDF Document", printAdapter, null);
        }
    }
}