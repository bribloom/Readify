package com.example.readify.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.readify.MyApplication;
import com.example.readify.R;
import com.example.readify.adapters.AdapterComment;
import com.example.readify.databinding.DialogCommentAddBinding;
import com.example.readify.databinding.PdfDetailBinding;
import com.example.readify.models.ModelComment;
import com.example.readify.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {


    private PdfDetailBinding binding;
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment>commentArrayList;



    private AdapterComment adapterComment;
    String bookId, bookTitle, bookUrl;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ((Window) window).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }


        binding = PdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        binding.downloadbtn.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null)
            checkIsFavorite();

        loadBookDetails();
        loadComments();


        MyApplication.incrementBookViewCount(bookId);




        binding.backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.readbookbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 =new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        binding.downloadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking Permission");
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){


                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                MyApplication.downloadBook(PdfDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                }else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

        });

        binding.favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }else {
                    if (isInMyFavorite){
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);

                    }else {
                        MyApplication.addToFavorite(PdfDetailActivity.this, bookId);
                    }
                }
            }
        });

        binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You are not logged in", Toast.LENGTH_SHORT).show();
                }
                else {

                    addCommentDialog();
                }
            }
        });



    }


    private void loadComments() {


        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            ModelComment modelComment = ds.getValue(ModelComment.class);

                            commentArrayList.add(modelComment);
                        }
                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);

                        binding.commentRv.setAdapter(adapterComment);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private String comment = "";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding =DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


        commentAddBinding.backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        commentAddBinding.submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment = commentAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)){

                    Toast.makeText(PdfDetailActivity.this, "Enter your comment", Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

    private void addComment() {

        progressDialog.setMessage("Adding Comment");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object>hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("bookId", ""+bookId);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("comment", ""+comment);
        hashMap.put("uid", ""+firebaseAuth.getUid());


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{

                if (isGranted){
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyApplication.downloadBook(this,""+bookId, ""+bookTitle, ""+bookUrl);

                }else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied");
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }

            });

    private void loadBookDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        bookTitle = "" +snapshot.child("title").getValue();
                        String description = "" +snapshot.child("description").getValue();
                        String categoryId = "" +snapshot.child("categoryId").getValue();
                        String viewCounts = "" +snapshot.child("viewCounts").getValue();
                        String downloadCounts = "" +snapshot.child("downloadCounts").getValue();
                        bookUrl = "" +snapshot.child("url").getValue();
                        String timestamp = "" +snapshot.child("timestamp").getValue();

                        binding.downloadbtn.setVisibility(View.VISIBLE);


                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(""+categoryId, binding.categoryTv);

                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,binding.pdfVieww, binding.progressbd,binding.pagesTv);

                        MyApplication.LoadPdfSize(""+bookUrl,""+bookTitle,binding.sizeTv);

                        //remove
                        //remove
                      /*  MyApplication.loadPdfPageCount(
                                PdfDetailActivity.this,
                                ""+bookUrl,
                                binding.pagesTv);
                        */
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewCounts.replace("null", "N/A"));
                        binding.dowloadsTv.setText(downloadCounts.replace("null", "N/A"));
                        binding.dateTv.setText(date);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {



                    }
                });

    }

    private void checkIsFavorite(){


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            isInMyFavorite = snapshot.exists();
                            if (isInMyFavorite){
                                binding.favbtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.favorite, 0, 0);
                                binding.favbtn.setText("Remove Favorite");
                            }else {
                                binding.favbtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.favorite_border, 0, 0);
                                binding.favbtn.setText("Add Favorite");

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


        }


    }

