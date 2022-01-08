package com.example.signaturepad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    TextView btn_cancel,btn_save;
    GestureOverlayView SignaturePad;

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    private GestureOverlayView gestureOverlayView = null;

    private Button redrawButton = null;

    private Button saveButton = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_cancel=(TextView)findViewById(R.id.btn_cancel);
        btn_save=(TextView)findViewById(R.id.btn_save);
        SignaturePad=(GestureOverlayView)findViewById(R.id.signature_pad);

        init();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndSaveSignature();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gestureOverlayView.clear(false);
            }
        });
    }

    private void init()
    {
        if(gestureOverlayView==null)
        {
            gestureOverlayView = (GestureOverlayView)findViewById(R.id.signature_pad);
        }

    }


    private void checkPermissionAndSaveSignature()
    {
        try {

            // Check whether this app has write external storage permission or not.
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // If do not grant write external storage permission.
            if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
            {
                // Request user to grant write external storage permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
            }else
            {
                saveSignature();
            }

        } catch (Exception e) {
            Log.v("Signature Gestures", e.getMessage());
            e.printStackTrace();
        }
    }


    private void saveSignature()
    {
        try {

            // First destroy cached image.
            gestureOverlayView.destroyDrawingCache();

            // Enable drawing cache function.
            gestureOverlayView.setDrawingCacheEnabled(true);

            // Get drawing cache bitmap.
            Bitmap drawingCacheBitmap = gestureOverlayView.getDrawingCache();

            // Create a new bitmap
//            Bitmap bitmap = Bitmap.createBitmap(drawingCacheBitmap);
            Bitmap bitmap =getScaledBitmap(drawingCacheBitmap,350,300);

            convertImageBitmap(bitmap);

        } catch (Exception e) {
            Log.v("Signature Gestures", e.getMessage());
            e.printStackTrace();
        }
    }

    private Bitmap getScaledBitmap(Bitmap bitmap, int width, int height) {
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        int nWidth = bWidth;
        int nHeight = bHeight;

        if(nWidth > width)
        {
            int ratio = bWidth / width;
            if(ratio > 0)
            {
                nWidth = width;
                nHeight = bHeight / ratio;
            }
        }
        if(nHeight > height)
        {
            int ratio = bHeight / height;
            if(ratio > 0)
            {
                nHeight = height;
                nWidth = bWidth / ratio;
            }
        }

        return Bitmap.createScaledBitmap(bitmap, nWidth, nHeight, true);

    }

    private void convertImageBitmap(Bitmap scaledBitmap) throws IOException {

        File imageFile = createImageFile();
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
//            selectedImage.compress(Bitmap.CompressFormat.WEBP, 60, os);
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //call your uploaf image api here
//        presenter.onUploadSignurl(imageFile);

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(timeStamp, ".png", storageDir);
        String imgFileLocation = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveSignature();
            } else {
                Toast.makeText(getApplicationContext(), "You denied write external storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

}