package com.example.readify.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.readify.R;
import com.example.readify.databinding.PdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private PdfAddBinding binding;

    private FirebaseAuth firebaseAuth;


    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private static final String TAG = "ADD_PDF_TAG";


    private ProgressDialog progressDialog;

    private static final int PDF_PICK_CODE = 1000;

    private Uri pdfUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ((Window) window).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }

        setContentView(R.layout.pdf_add);
        binding = PdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();

        loadPdfCategories();


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PdfAddActivity.this, DashboardAdminActivity.class);
                startActivity(intent);
            }
        });


        binding.attachfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });


        binding.pdfbookcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });


        binding.uploadpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateData();

            }
        });


    }

    private  String title = "", description = "" ;
    private void validateData() {

        Log.d(TAG, "ValidateData: Validating Data");


        title = binding.pdfbooktitle.getText().toString().trim();
        description = binding.pdfbookdescrip.getText().toString().trim();


        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Enter Category", Toast.LENGTH_SHORT).show();
        } else if (pdfUri==null) {
            Toast.makeText(this, "Pick PDF", Toast.LENGTH_SHORT).show();
            
        }else {
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {

        Log.d(TAG, "uploadPdfToStorage: Uploading to Storage");

        progressDialog.setMessage("Uploading PDF");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Books/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.d(TAG, "onSuccess: PDF uploaded to storage");
                    Log.d(TAG, "onSuccess: Getting PDF URL");


                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();


                        UploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: PDF upload failed due to"+ e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void UploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {

        Log.d(TAG, "uploadPdfToStorage: Uploading PDF info to Firebase DB");

        progressDialog.setMessage("Uploading PDF information");

        String uid = firebaseAuth.getUid();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadedPdfUrl);
        hashMap.put("timestamp", + timestamp);
        hashMap.put("viewCounts", 0);
        hashMap.put("downloadCounts", 0);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess: Successfully Uploaded");
                        Toast.makeText(PdfAddActivity.this, "Sucessfully Uploaded", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: Failed to upload to DB due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to DB due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void loadPdfCategories() {

    Log.d(TAG, "loadPdfCategories: Loading pdf categories");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    String categoryId = ""+dataSnapshot.child(("id")).getValue();
                    String categoryTitle = ""+dataSnapshot.child(("category")).getValue();


                    categoryIdArrayList.add(categoryId);
                    categoryTitleArrayList.add(categoryTitle);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;

    private void categoryPickDialog() {

        Log.d(TAG, "categoryPickDialog: showing category pick dialog");


        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i=0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        selectedCategoryTitle = categoryTitleArrayList.get(i);
                        selectedCategoryId = categoryIdArrayList.get(i);

                        binding.pdfbookcategory.setText(selectedCategoryTitle);


                        Log.d(TAG, "onClick: Selected Category: "+ selectedCategoryId+ ""+ selectedCategoryTitle);

                    }
                })
                .show();

    }

    private void pdfPickIntent() {

        Log.d(TAG,"pdfPickIntent: starting pdf pick intent");

        Intent intent  = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: "+ pdfUri);
            }
        }else {
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}