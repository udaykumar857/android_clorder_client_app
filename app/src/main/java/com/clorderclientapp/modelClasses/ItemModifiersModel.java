package com.clorderclientapp.modelClasses;

import com.clorderclientapp.RealmModels.OptionsModifiersModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ItemModifiersModel extends RealmObject {


    //            "Active": true,
//                "DisplayPrice": true,
//                "ID": 2,
//                "IsPriceField": false,
//                "MaxSelection": 0,
//                "Name": "How Spicy?",
//            "Price": null,
//                "Type": 0
    @PrimaryKey
    private int modifierSerialId;
    private int id;

    private boolean modifierActive;
    private boolean displayPrice;

    private boolean isPriceField;
    private int maxSelection;
    private String modifierName;
    private String price;
    private int modifierType;
    private boolean isModifierSelected;
    private RealmList<OptionsModifiersModel> optionsModifiersList;

    public int getModifierSerialId() {
        return modifierSerialId;
    }

    public void setModifierSerialId(int modifierSerialId) {
        this.modifierSerialId = modifierSerialId;
    }

    public boolean isModifierActive() {
        return modifierActive;
    }

    public void setModifierActive(boolean modifierActive) {
        this.modifierActive = modifierActive;
    }

    public boolean isDisplayPrice() {
        return displayPrice;
    }

    public void setDisplayPrice(boolean displayPrice) {
        this.displayPrice = displayPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPriceField() {
        return isPriceField;
    }

    public void setPriceField(boolean priceField) {
        isPriceField = priceField;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    public void setMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getModifierType() {
        return modifierType;
    }

    public void setModifierType(int modifierType) {
        this.modifierType = modifierType;
    }

    public RealmList<OptionsModifiersModel> getOptionsModifiersList() {
        return optionsModifiersList;
    }

    public void setOptionsModifiersList(RealmList<OptionsModifiersModel> optionsModifiersList) {
        this.optionsModifiersList = optionsModifiersList;
    }

    public boolean getIsModifierSelected() {
        return isModifierSelected;
    }

    public void setIsModifierSelected(boolean isModifierSelected) {
        this.isModifierSelected = isModifierSelected;
    }

    @Override
    public String toString() {
        return "ItemModifiersModel{" +
                "modifierType=" + modifierType + "\n" +
                ", price='" + price + '\'' + "\n" +
                ", modifierName='" + modifierName + '\'' + "\n" +
                ", maxSelection=" + maxSelection + "\n" +
                ", isPriceField=" + isPriceField + "\n" +
                ", displayPrice=" + displayPrice + "\n" +
                ", modifierActive=" + modifierActive + "\n" +
                ", id=" + id + "\n" +
                '}';
    }
}
