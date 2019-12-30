package com.clorderclientapp.RealmModels;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class OptionsModifiersModel extends RealmObject {

    //    {
//        "Default": false,
//            "FieldId": 2,
//            "MaxSelectionPerField": 0,
//            "OptionId": 5,
//            "Price": null,
//            "Title": "Mild Spicy"
//    },
    @PrimaryKey
    public int optionSerialId;
    public int modifierSerialId;

    private boolean isOptionsDefault;
    private int fieldId;
    private int maxSelectionPerField;
    private int OptionId;
    private String price;
    private String title;

    public int getModifierSerialId() {
        return modifierSerialId;
    }

    public void setModifierSerialId(int modifierSerialId) {
        this.modifierSerialId = modifierSerialId;
    }

    public int getOptionSerialId() {
        return optionSerialId;
    }

    public void setOptionSerialId(int optionSerialId) {
        this.optionSerialId = optionSerialId;
    }

    public boolean isOptionsDefault() {
        return isOptionsDefault;
    }

    public void setOptionsDefault(boolean optionsDefault) {
        isOptionsDefault = optionsDefault;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public int getMaxSelectionPerField() {
        return maxSelectionPerField;
    }

    public void setMaxSelectionPerField(int maxSelectionPerField) {
        this.maxSelectionPerField = maxSelectionPerField;
    }

    public int getOptionId() {
        return OptionId;
    }

    public void setOptionId(int optionId) {
        OptionId = optionId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
