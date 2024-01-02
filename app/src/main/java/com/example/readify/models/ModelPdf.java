package com.example.readify.models;

public class ModelPdf {

    String uid, id, title, description, categoryId, url;
    long timestamp, viewCounts, downloadCounts;

    boolean favorite;


    public ModelPdf(String uid, String id, String title, String description, String categoryId, String url, long timestamp, long viewCounts, long downloadCounts, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.viewCounts = viewCounts;
        this.downloadCounts = downloadCounts;
        this.favorite = favorite;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getViewCounts() {
        return viewCounts;
    }

    public void setViewCounts(long viewCounts) {
        this.viewCounts = viewCounts;
    }

    public long getDownloadCounts() {
        return downloadCounts;
    }

    public void setDownloadCounts(long downloadCounts) {this.downloadCounts = downloadCounts;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public ModelPdf() {


    }



}
