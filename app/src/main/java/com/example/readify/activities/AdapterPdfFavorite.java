package com.example.readify.activities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readify.MyApplication;
import com.example.readify.databinding.RowPdfFavoriteBinding;
import com.example.readify.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;

    private RowPdfFavoriteBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";



    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavorite(binding.getRoot());

    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {

        ModelPdf model = pdfArrayList.get(position);

        loadBookDetails(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);

            }
        });

        holder.removefavbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {

        String bookId  = model.getId();
        Log.d(TAG, "loadBooksDetails: Book Details of Book ID"+ bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewCounts = ""+snapshot.child("viewCounts").getValue();
                        String downloadCounts = ""+snapshot.child("downloadCounts").getValue();



                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));


                        MyApplication.loadCategory(categoryId, holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.LoadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);

                        holder.titleTv.setText(date);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    public class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removefavbutton;

        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

        pdfView = binding.pdfView;
        progressBar = binding.progressbar;
        titleTv = binding.titletv;
        removefavbutton = binding.removefavbutton;
        descriptionTv = binding.descriptionTv;
        categoryTv = binding.categoryTv;
        sizeTv = binding.sizeTv;
        dateTv = binding.dateTv;


        }
    }


}
