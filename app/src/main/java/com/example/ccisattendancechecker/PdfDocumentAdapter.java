package com.example.ccisattendancechecker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfDocumentAdapter extends PrintDocumentAdapter {

    private final Context context;
    private final Uri pdfUri;

    public PdfDocumentAdapter( Context context, Uri pdfUri) {
        this.context = context;
        this.pdfUri = pdfUri;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onLayout(
            @NonNull PrintAttributes oldAttributes,
            @NonNull PrintAttributes newAttributes,
            @NonNull CancellationSignal cancellationSignal,
            @NonNull LayoutResultCallback callback,
            Bundle extras) {
        // Layout logic here
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        // Provide information about the document
        PrintDocumentInfo info = new PrintDocumentInfo.Builder("PDF Document")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build();

        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(
            @NonNull PageRange[] pages,
            @NonNull ParcelFileDescriptor destination,
            @NonNull CancellationSignal cancellationSignal,
            @NonNull WriteResultCallback callback) {
        // Logic to write the PDF
        try (FileInputStream input = new FileInputStream(context.getContentResolver().openFileDescriptor(pdfUri, "r").getFileDescriptor());
             FileOutputStream output = new FileOutputStream(destination.getFileDescriptor())) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    return;
                }
                output.write(buffer, 0, bytesRead);
            }

            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        } catch (IOException e) {
            Log.e("PdfDocumentAdapter", "Error writing PDF: " + e.getMessage(), e);
            callback.onWriteFailed(e.toString());
        }
    }
    @Override
    public void onFinish() {
        super.onFinish();
    }

}

