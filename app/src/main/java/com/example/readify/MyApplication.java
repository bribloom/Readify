package com.example.readify;

import static com.example.readify.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airbnb.lottie.L;
import com.example.readify.adapters.AdapterPdfAdmin;
import com.example.readify.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy",cal).toString();

        return date;

    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {

        String TAG = "DELETE_BOOK_TAG";



        Log.d(TAG, "deleteBook: Deleting Books");

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Deleting  " + bookTitle);
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from Storage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: Deleted from Storage");

                        Log.d(TAG, "onSuccess: Deleting Information from Database");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from Database");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Successfully", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.d(TAG,"onFailure: Failed to delete from Database due to "+e.getMessage());


                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG,"onFailure: Failed to delete from Database due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });



    }

    public static void LoadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String  TAG = "PDF_SIZE_TAG";



        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {

                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess:"+ pdfTitle + " "+bytes);

                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb>=1){
                            sizeTv.setText(String.format("%.2f", mb)+ "MB");
                        } else if (kb>=1) {
                            sizeTv.setText(String.format("%.2f", kb)+ "KB");
                        }else {
                            sizeTv.setText(String.format("%.2f", bytes)+ "bytes");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }


    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {



        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Log.d(TAG, "onSuccess: "+ pdfTitle + "Successfully got the file");

                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError"+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError"+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded" );


                                        if (pagesTv != null){
                                            pagesTv.setText(""+nbPages);
                                        }

                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: Failed getting file from url due to "+e.getMessage() );

                    }
                });


    }


    public static void loadCategory(String categoryId, TextView categoryview) {




        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String category = ""+snapshot.child("category").getValue();

                        categoryview.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void incrementBookViewCount(String bookId){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String viewCounts = ""+snapshot.child("viewCounts").getValue();

                        if (viewCounts.equals("") || viewCounts.equals("null") ){
                            viewCounts = "0";
                        }

                        long newViewCounts = Long.parseLong(viewCounts) + 1;


                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewCounts", newViewCounts);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(hashMap);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    public  static void downloadBook(Context context, String bookId,  String bookTitle, String bookUrl){
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading Book");

    String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: "+ nameWithExtension);


        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading "+ nameWithExtension);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Download");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving Book");
                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to Download due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to "+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadBook: Saving downloaded Book");
        try {

            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to Download Folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

        }catch (Exception e){

            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to "+e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    private static void incrementBookDownloadCount(String bookId) {

    Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing Book Download Count");

    DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Books");
    ref.child(bookId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String downloadCounts = ""+snapshot.child("downloadCounts").getValue();
                    Log.d(TAG_DOWNLOAD, "onDataChange: Download Count: "+ downloadCounts);

                    if (downloadCounts.equals("") || downloadCounts.equals("null")){
                        downloadCounts = "0";
                    }

                    long newDownloadCounts = Long.parseLong(downloadCounts) +1 ;
                    Log.d(TAG_DOWNLOAD, "onDataChange: New Download Count: "+ newDownloadCounts);


                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("downloadCounts", newDownloadCounts);


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                    ref.child(bookId).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG_DOWNLOAD, "onSuccess: Download Count updated");

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG_DOWNLOAD, "onFailure: Faited to update Download Count due to"+ e.getMessage());

                                }
                            });



                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }


    //remove functions loadPdfPageCount
 /*public static void loadPdfPageCount(Context context, String pdfUrl, TextView pagesTv){

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference
                .getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        PDFView pdfView = new PDFView(context, null);
                        pdfView.fromBytes(bytes)
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        pagesTv.setText(""+nbPages);
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

 }
*/


 public static void addToFavorite(Context context, String bookId){

     FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

     if (firebaseAuth.getCurrentUser() == null){
         Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
     }else {
         long timestamp = System.currentTimeMillis();

         HashMap<String, Object> hashMap = new HashMap<>();
         hashMap.put("bookId", ""+bookId);
         hashMap.put("timestamp", ""+ timestamp);

         DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
         ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                 .setValue(hashMap)
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void unused) {

                         Toast.makeText(context, "Added to your favorites list", Toast.LENGTH_SHORT).show();

                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Toast.makeText(context, "Failed to add to favorites due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                     }
                 });

     }


 }
        public static void removeFromFavorite(Context context, String bookId){


            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
            }else {


                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(context, "Remove from your favorites list", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Failed to remove from favorites due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

            }


        }
}
