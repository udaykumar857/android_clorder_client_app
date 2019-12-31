package com.clorderclientapp.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.interfaces.HandleInterface;
import com.clorderclientapp.modelClasses.ExistingCardModel;
import com.clorderclientapp.utils.Utils;

import java.util.ArrayList;

public class ExistingCardsAdapter extends BaseAdapter {
    private ArrayList<ExistingCardModel> existingCardList;
    private Context mContext;
    private HandleInterface handleInterface;
    private ArrayList<String> listOfPattern;


    public ExistingCardsAdapter(Context context, ArrayList<ExistingCardModel> existingCardModelArrayList) {
        mContext = context;
        existingCardList = existingCardModelArrayList;
        handleInterface = (HandleInterface) context;
    }

    @Override
    public int getCount() {
        return existingCardList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_card_single_row, null);
            viewHolder.cardTypeImg = (ImageView) convertView.findViewById(R.id.cartTypeImg);
            viewHolder.editBtn = (TextView) convertView.findViewById(R.id.editBtn);
            viewHolder.deleteBtn = (TextView) convertView.findViewById(R.id.deleteBtn);
            viewHolder.cardLayout = (LinearLayout) convertView.findViewById(R.id.card_layout);
            viewHolder.cardNumTxt = (TextView) convertView.findViewById(R.id.card_num_txt);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.cardNumTxt.setText(String.format("%s", existingCardList.get(position).getCreditCardNumber()));


        viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInterface.handleClick(v, position);
            }
        });
        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInterface.handleClick(v, position);
            }
        });

        viewHolder.cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInterface.handleClick(v, position);
            }
        });

        String ccNum = existingCardList.get(position).getCreditCardNumber();
        ArrayList<String> creditCardTypeList = Utils.getCreditCardTypeList();
        for (int i = 0; i < creditCardTypeList.size(); i++) {
            if (ccNum.matches(creditCardTypeList.get(i))) {
                switch (i) {

//                    public enum CreditCardTypeEnum
//                    {
//                        [Name("American Express")]
//                        AmericanExpress = 1,
//
//                            [Name("Discover")]
//                        Discover = 2,
//
//                            [Name("Master Card")]
//                        MasterCard = 4,
//
//                            [Name("Visa")]
//                        VISA = 8,
//
//                            [Name("Diner Club")]
//                        DinerClub = 16,
//
//                            [Name("JCB International")]
//                        JCB = 32
//
//                    }

                    case 0:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.visacard_logo);
                        break;
                    case 1:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.mastercard_logo);
                        break;
                    case 2:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.americanexpresscard_logo);
                        break;
                    case 3:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.dinersclubcard_logo);
                        break;
                    case 4:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.discovercard_logo);
                        break;
                    case 5:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.jcbcard_logo);
                        break;
                    default:
                        viewHolder.cardTypeImg.setImageResource(R.mipmap.mastercard_logo);
                        break;
                }
                break;
            }
        }


        return convertView;
    }

    public class ViewHolder {
        private TextView cardNumTxt;
        private ImageView cardTypeImg;
        private TextView editBtn, deleteBtn;
        private LinearLayout cardLayout;
    }


}
