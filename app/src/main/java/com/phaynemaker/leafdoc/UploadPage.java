package com.phaynemaker.leafdoc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.phaynemaker.leafdoc.ml.DiseaseDetectorFinal;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static com.phaynemaker.leafdoc.R.drawable.*;

public class UploadPage extends AppCompatActivity {

    public static final int CAMERA_PERMISSION_CODE = 30;
    public static final int CAMERA_REQUEST_CODE = 23;
    public static final int GALLERY_REQUEST_CODE = 7;
    Button btnUpload, btnGallery, btnCamera, btnInference, btnHome, btnAnother;
    ImageButton btnBack, btnDelete;
    ImageView ivUploadedImage;
    TextView tvActionDescription, tvResult;
    Boolean imageUp = false;
    public AlertDialog.Builder builder;
    public AlertDialog dialog;
    Bitmap img;
    int result;
    String class_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_page);

        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btndelete);
        btnUpload = findViewById(R.id.btnUpload);
        ivUploadedImage = findViewById(R.id.ivUploadedImage);
        tvActionDescription = findViewById(R.id.tvActionDescription);
        btnInference = findViewById(R.id.btnInference);

        btnInference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img = Bitmap.createScaledBitmap(img, 128, 128, true);
                try {
                    DiseaseDetectorFinal model = DiseaseDetectorFinal.newInstance(UploadPage.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(img);

                    ByteBuffer byteBuffer=tensorImage.getBuffer();

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    DiseaseDetectorFinal.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    // Releases model resources if no longer used.
                    model.close();

                    Float result1 =  outputFeature0.getFloatArray()[0];
                    Float result2 =  outputFeature0.getFloatArray()[1];
                    Float result3 =  outputFeature0.getFloatArray()[2];
                    Float result4 =  outputFeature0.getFloatArray()[3];

                    Float [] results = new Float[]{result1, result2, result3, result4};
                    result = getMax(results);
                    class_name = getClass_name(result);
                    //result = String.format("%s\n%s\n%s\n%s", result1, result2, result3, result4);
                    inferenceDialogPopUp();

                } catch (IOException e) {
                    // TODO Handle the exception

                }

            }
        });

        btnInference.setVisibility(View.GONE);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialogPopUp();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetLayout();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadPage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void uploadDialogPopUp(){
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.upload_image_dialog_box, null);

        //btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);

        builder.setView(view);

        dialog = builder.create();
        dialog.show();
        //dialog.setCanceledOnTouchOutside(false);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
                dialog.dismiss();
            }
        });


        /*btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();
                dialog.dismiss();
            }
        });*/
    }
    private int getMax(Float res []){
        int index = 5;
        Float maxValue = 0.0f;
        for(int i=0; i<res.length; i++){
            if (res[i]>maxValue){
                maxValue = res[i];
                index = i;
            }
        }
        return index;
    }

    private String getClass_name(int resultIndex){
        String class_is = "";
        switch (resultIndex){
            case 0:
               class_is = "Northern Leaf Blight";
               break;
            case 1:
                class_is = "Common Rust";
                break;
            case 2:
                class_is = "Gray Leaf Spot";
                break;
            case 3:
                class_is = "Healthy";
                break;
            default:
                class_is = "Null";
                break;
        }
        return class_is;
    }
    private void inferenceDialogPopUp(){
        builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.inference_dialogue, null);

        btnAnother = v.findViewById(R.id.btnAnother);
        btnHome = v.findViewById(R.id.btnHome);
        tvResult = v.findViewById(R.id.tvResult);

        builder.setView(v);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        tvResult.setText(class_name);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadPage.this, MainActivity.class);
                startActivity(intent);
                finish();
                dialog.dismiss();
            }
        });
        btnAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivUploadedImage.setImageResource(upload_image);
                tvActionDescription.setText(R.string.info);
                btnInference.setVisibility(View.GONE);
                btnUpload.setVisibility(View.VISIBLE);
                dialog.dismiss();
                resetLayout();
                uploadDialogPopUp();
            }
        });

    }

    private void askCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else {
            OpenCamera();
            dialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                OpenCamera();
            }
            else {
                Toast.makeText(this, "Camera Permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (resultCode== Activity.RESULT_OK && data != null){
                Bundle bundle = data.getExtras();
                Bitmap leafImage = (Bitmap) data.getExtras().get("data");
                ivUploadedImage.setImageBitmap(leafImage);
            }
            else {
                Toast.makeText(this, "Something's wrong", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE){
            if (resultCode== Activity.RESULT_OK){
                Uri contentUri = data.getData();
                ivUploadedImage.setImageURI(contentUri);
                try {
                    img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUp = true;
                updateLayout();
            }
        }
    }

    private void updateLayout() {
        btnInference.setVisibility(View.VISIBLE);
        btnUpload.setVisibility(View.GONE);
        tvActionDescription.setText(R.string.success_message);
        //ivUploadedImage.setImageResource(upload_bg);
    }
    private void resetLayout() {
        btnInference.setVisibility(View.GONE);
        btnUpload.setVisibility(View.VISIBLE);
        tvActionDescription.setText(R.string.info);
        ivUploadedImage.setImageResource(upload_bg);
    }

    private void OpenCamera(){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camera.resolveActivity(getPackageManager())!= null){
            startActivityForResult(camera, CAMERA_REQUEST_CODE);
        }
    }
}
