package com.clorderclientapp.modelClasses;


public class CategoryModel {

    public String categoryDesc;
    public int categoryId;
    public String categoryTitle;
    public int categoryMode;
    private boolean isExpanded;
    private int itemsCnt;
    private String imageUrl;

    public String getCategoryDesc() {
        return categoryDesc;
    }

    public void setCategoryDesc(String categoryDesc) {
        this.categoryDesc = categoryDesc;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public int getCategoryMode() {
        return categoryMode;
    }

    public void setCategoryMode(int categoryMode) {
        this.categoryMode = categoryMode;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public int getItemsCnt() {
        return itemsCnt;
    }

    public void setItemsCnt(int itemsCnt) {
        this.itemsCnt = itemsCnt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
