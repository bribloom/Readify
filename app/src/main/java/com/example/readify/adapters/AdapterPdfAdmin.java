package com.example.readify.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readify.MyApplication;
import com.example.readify.activities.PdfDetailActivity;
import com.example.readify.activities.PdfeditActivity;
import com.example.readify.databinding.RowPdfAdminBinding;
import com.example.readify.filters.FilterPdfAdmin;
import com.example.readify.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {


    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private static final String TAG = "PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;

    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent, false);

        return new HolderPdfAdmin(binding.getRoot());
    }



    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();

        String formattedDate  = MyApplication.formatTimestamp(timestamp);


        holder.titleview.setText(title);
        holder.descriptionview.setText(description);
        holder.dateview.setText(formattedDate);

        MyApplication.loadCategory(""+categoryId, holder.categoryview);
        MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl, ""+title,holder.pdfView,holder.progressBar, null);
        MyApplication.LoadPdfSize(""+pdfUrl,""+title,holder.sizeview);
/*
       MyApplication.loadCategory(""+categoryId, holder.categoryview);
        MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,""+title, holder.pdfView, holder.progressBar);
        MyApplication.loadPdfSize(""+pdfUrl,""+title, holder.sizeview);

*/
        holder.morebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoreOptionsDialog(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                intent.putExtra("categoryTitle", categoryId);
                context.startActivity(intent);
            }
        });

    }







    private void MoreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        String[]  options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0){

                            Intent intent = new Intent(context, PdfeditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);

                        } else if (i==1) {

                            MyApplication.deleteBook(context,""+bookId,""+bookUrl,""+bookTitle);

                           // deleteBook(model, holder);
                        }

                    }
                })
                .show();

    }



    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter =new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleview, descriptionview, categoryview, sizeview, dateview;

        ImageButton morebutton;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfviewgit;
            progressBar = binding.progressbarpdf;
            titleview = binding.titleTv;
            descriptionview = binding.descriptionTv;
            categoryview = binding.categoryTv;
            sizeview = binding.sizeTv;
            dateview = binding.dateTv;
            morebutton = binding.moreButton;


        }
    }

}
