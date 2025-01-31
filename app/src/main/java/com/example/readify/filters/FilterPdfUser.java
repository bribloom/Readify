package com.example.readify.filters;

import android.widget.Filter;

import com.example.readify.adapters.AdapterPdfUser;
import com.example.readify.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter{


    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {

        FilterResults results = new FilterResults();
        if (charSequence!=null || charSequence.length()>0){

            charSequence =charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count =filteredModels.size();
            results.values =filteredModels;

        }else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {

        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)results.values;


        adapterPdfUser.notifyDataSetChanged();

    }
}
