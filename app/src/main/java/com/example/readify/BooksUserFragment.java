package com.example.readify;

import android.graphics.ColorSpace;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.readify.adapters.AdapterPdfUser;
import com.example.readify.databinding.FragmentBooksUserBinding;
import com.example.readify.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {

  private String categoryId;
  private String category;
  private String uid;


  private ArrayList<ModelPdf>pdfArrayList;
  private AdapterPdfUser adapterPdfUser;



  private FragmentBooksUserBinding binding;

  private static final String TAG = "BOOKS_USER_TAG";
    public BooksUserFragment() {



    }


    public static BooksUserFragment newInstance(String categoryId, String category, String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("categoryId");
            uid = getArguments().getString("uid");


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()),container, false);

        Log.d(TAG, "onCreateView: Category: "+category);
        if (category.equals("All")){

            loadAllBooks();

        } else if (category.equals("Most Viewed")) {

            loadMostViewedDownloadedBooks("viewCounts");

        } else if (category.equals("Most Downloaded")) {

            loadMostViewedDownloadedBooks("downloadCounts");

        }else {

            loadCategorizedBooks();

        }

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                try {
                    adapterPdfUser.getFilter().filter(charSequence);
                }catch (Exception e){
                    Log.d(TAG, "onTexetChange: "+e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return binding.getRoot();
    }



    private void loadAllBooks() {

        pdfArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    ModelPdf model = ds.getValue(ModelPdf.class);

                    pdfArrayList.add(model);

                }

                    adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);

                binding.booksRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void loadMostViewedDownloadedBooks(String orderBy) {

        pdfArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    ModelPdf model = ds.getValue(ModelPdf.class);

                    pdfArrayList.add(model);

                }

                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);

                binding.booksRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadCategorizedBooks(){

        pdfArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            ModelPdf model = ds.getValue(ModelPdf.class);

                            pdfArrayList.add(model);

                        }

                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);

                        binding.booksRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }


}