package com.example.readify.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.readify.MyApplication;
import com.example.readify.R;
import com.example.readify.databinding.ProfileBinding;
import com.example.readify.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ProfileBinding binding;


    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;
    private static final String TAG = "PROFILE_TAG";
    private FirebaseAuth firebaseAuth;

    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ((Window) window).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }



        binding = ProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.accounttype.setText("N/A");
        binding.memberdate.setText("N/A");
        binding.favoritebook.setText("N/A");
        binding.accountstatus.setText("N/A");

//binding

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = firebaseAuth.getCurrentUser();
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();
        loadFavoriteBooks();

        binding.backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.accountstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (firebaseUser.isEmailVerified()){

                    Toast.makeText(ProfileActivity.this, "Already Verified", Toast.LENGTH_SHORT).show();
                }
                else {
                    emailVerificationDialog();
                }

            }
        });

    binding.Editprofile.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
        }
    });


    }

    private void emailVerificationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Email")
                .setMessage("Are you sure you want to send email verification "+firebaseUser.getEmail())
                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                     sendEmailVerification();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void sendEmailVerification() {
        progressDialog.setMessage("Sending email verification to your email "+firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Verification Sent "+firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadUserInfo() {

        Log.d(TAG, "loadUserInfo: Loading User Information"+firebaseAuth.getUid());

        if (firebaseUser.isEmailVerified()){
            binding.accountstatus.setText("Verified");
        } else {
            binding.accountstatus.setText("Not Verified");
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String UserType = ""+snapshot.child("UserType").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));


                        binding.emailtv.setText(email);
                        binding.nametv.setText(name);
                        binding.memberdate.setText(formattedDate);
                        binding.accounttype.setText(UserType);


                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.person)
                                .into(binding.profile);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void loadFavoriteBooks() {

        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String bookId = ""+ds.child("bookId").getValue();


                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            pdfArrayList.add(modelPdf);

                        }

                    binding.favoritebook.setText(""+pdfArrayList.size());

                    adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);


                    binding.booksRv.setAdapter(adapterPdfFavorite);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}