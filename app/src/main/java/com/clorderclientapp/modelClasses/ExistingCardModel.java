package com.clorderclientapp.modelClasses;


import android.os.Parcel;
import android.os.Parcelable;

public class ExistingCardModel implements Parcelable {


//    {
//            "BillingZipCode": "84095",
//                "CardId": 52723,
//                "CreditCardCSC": "2223",
//                "CreditCardExpired": "2017-01-02T00:00:00",
//                "CreditCardName": "Demo333",
//                "CreditCardNumber": "4444333322222222",
//                "CreditCardType": 8,
//                "IsDeleted": false
//        }


    public ExistingCardModel() {
    }

    private String billingZipCode;
    private String cardId;
    private String creditCardCSC;
    private String creditCardExpired;
    private String creditCardName;
    private String creditCardNumber;
    private String creditCardType;
    private boolean isDeleted;


    protected ExistingCardModel(Parcel in) {
        billingZipCode = in.readString();
        cardId = in.readString();
        creditCardCSC = in.readString();
        creditCardExpired = in.readString();
        creditCardName = in.readString();
        creditCardNumber = in.readString();
        creditCardType = in.readString();
        isDeleted = in.readByte() != 0;
    }

    public static final Creator<ExistingCardModel> CREATOR = new Creator<ExistingCardModel>() {
        @Override
        public ExistingCardModel createFromParcel(Parcel in) {
            return new ExistingCardModel(in);
        }

        @Override
        public ExistingCardModel[] newArray(int size) {
            return new ExistingCardModel[size];
        }
    };

    public String getBillingZipCode() {
        return billingZipCode;
    }

    public void setBillingZipCode(String billingZipCode) {
        this.billingZipCode = billingZipCode;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCreditCardCSC() {
        return creditCardCSC;
    }

    public void setCreditCardCSC(String creditCardCSC) {
        this.creditCardCSC = creditCardCSC;
    }

    public String getCreditCardExpired() {
        return creditCardExpired;
    }

    public void setCreditCardExpired(String creditCardExpired) {
        this.creditCardExpired = creditCardExpired;
    }

    public String getCreditCardName() {
        return creditCardName;
    }

    public void setCreditCardName(String creditCardName) {
        this.creditCardName = creditCardName;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        this.creditCardType = creditCardType;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(billingZipCode);
        dest.writeString(cardId);
        dest.writeString(creditCardCSC);
        dest.writeString(creditCardExpired);
        dest.writeString(creditCardName);
        dest.writeString(creditCardNumber);
        dest.writeString(creditCardType);
        dest.writeByte((byte) (isDeleted ? 1 : 0));
    }
}
