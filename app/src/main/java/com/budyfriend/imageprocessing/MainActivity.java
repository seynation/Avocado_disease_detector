package com.budyfriend.imageprocessing;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseAutoMLLocalModel localModel;
    private FirebaseAutoMLRemoteModel remoteModel;
    private FirebaseVisionImageLabeler labeler;
    private FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
    private ProgressDialog progressDialog;
    private FirebaseModelDownloadConditions conditions;
    private FirebaseVisionImage image;
    private TextView textView;
    private Button button, buttonT;
    private Layout layout;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        textView = findViewById(R.id.text);
        button = findViewById(R.id.selectImage);
        buttonT = findViewById(R.id.predict);
        imageView = findViewById(R.id.image);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromRemoteModel();
            }
        });

        buttonT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.append("_____________________________________" + "\n\n");
                showProgressBar();
                processImageLabeler(labeler, image);

            }
        });

    }


    private void fromRemoteModel() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "seynation"), 20);
        }
    }

    private void showProgressBar() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
//		    progressDialog.cancel();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            final Uri filePathChoose = data.getData();
            remoteModel = new FirebaseAutoMLRemoteModel.Builder("Ovarcado_anthrac_20210720125728").build();
            conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();
            FirebaseModelManager.getInstance().download(remoteModel, conditions);
            CropImage.activity(filePathChoose)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setFixAspectRatio(true)
                    .setActivityTitle("Profile")
                    .setCropMenuCropButtonTitle("Done")
                    .setAspectRatio(1000, 1000)
                    .start(MainActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    Uri uri = result.getUri();
                    imageView.setImageURI(uri);
                    textView.setText("");
                    setLabelerFromRemoteLabel(uri);
                    setLabelerFromLocalModel(uri);
                } else {
                    progressDialog.cancel();
                }
            } else {
                progressDialog.cancel();
            }
        }


    }

    private void setLabelerFromLocalModel(Uri uri) {
        localModel = new FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath("model/manifest.json")
                .build();

        try {
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.0f)
                            .build();

            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
            //processImageLabeler(labeler, image);
        } catch (FirebaseMLException | IOException e) {
            e.printStackTrace();
        }

    }

    private void setLabelerFromRemoteLabel(final Uri uri) {
        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                    FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder.setConfidenceThreshold(0.0f).build();
                    try {
                        labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                        image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
                        //processImageLabeler(labeler, image);
                    } catch (FirebaseMLException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void processImageLabeler(FirebaseVisionImageLabeler labeler, FirebaseVisionImage image) {
        String labelNames = "";

        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                int noLabel = 0;
                for (FirebaseVisionImageLabel label : firebaseVisionImageLabels) {
                    String eachLabel = label.getText().toUpperCase();
                    float confindence = label.getConfidence();
                    noLabel += 1;

                    if (noLabel == 1) {
                        textView.append(eachLabel + " : " + ("" + confindence * 100).subSequence(0, 4) + "%" + "\n");
                        textView.append("RESULTS: " + eachLabel + "\n\n");
                        textView.append("_____________________________________" + "\n\n");
                    }

                    progressDialog.cancel();
                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
