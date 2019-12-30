package com.clorderclientapp.modelClasses;

import java.sql.Time;
import java.util.ArrayList;

public class MenuModel {
    public int menuTypeId;
    public String menuTitle;
    public int clientId;
    public boolean menuIsDefault;
    public int menuOrderNo;
    public boolean menuIsVisible;
    public String menuStartTime;
    public String menuEndTime;

    public ArrayList<CategoryModel> categoryArrayList;


    public int getMenuTypeId() {
        return menuTypeId;
    }

    public void setMenuTypeId(int menuTypeId) {
        this.menuTypeId = menuTypeId;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public boolean isMenuIsDefault() {
        return menuIsDefault;
    }

    public void setMenuIsDefault(boolean menuIsDefault) {
        this.menuIsDefault = menuIsDefault;
    }

    public int getMenuOrderNo() {
        return menuOrderNo;
    }

    public void setMenuOrderNo(int menuOrderNo) {
        this.menuOrderNo = menuOrderNo;
    }

    public boolean isMenuIsVisible() {
        return menuIsVisible;
    }

    public void setMenuIsVisible(boolean menuIsVisible) {
        this.menuIsVisible = menuIsVisible;
    }

    public String getMenuStartTime() {
        return menuStartTime;
    }

    public void setMenuStartTime(String menuStartTime) {
        this.menuStartTime = menuStartTime;
    }

    public String getMenuEndTime() {
        return menuEndTime;
    }

    public void setMenuEndTime(String menuEndTime) {
        this.menuEndTime = menuEndTime;
    }

    public ArrayList<CategoryModel> getCategoryArrayList() {
        return categoryArrayList;
    }

    public void setCategoryArrayList(ArrayList<CategoryModel> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }
}
