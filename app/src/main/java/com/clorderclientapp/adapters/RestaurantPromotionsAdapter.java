package com.clorderclientapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.clorderclientapp.interfaces.HandleInterface;
import com.clorderclientapp.modelClasses.RestaurantPromotionsModel;
import com.clorderclientapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RestaurantPromotionsAdapter extends RecyclerView.Adapter<RestaurantPromotionsAdapter.RestaurantPromotionsViewHolder> {

    private ArrayList<RestaurantPromotionsModel> restaurantPromotionsList;
    private Context mContext;
    HandleInterface handleInterface;


    public RestaurantPromotionsAdapter(Context context, ArrayList<RestaurantPromotionsModel> restaurantPromotionsModelArrayList) {
        mContext = context;
        restaurantPromotionsList = restaurantPromotionsModelArrayList;
        handleInterface = (HandleInterface) mContext;
    }

    @Override
    public RestaurantPromotionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.layout_coupon_single_row, parent, false);
        return new RestaurantPromotionsViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final RestaurantPromotionsViewHolder holder, int position) {
        holder.couponIdBtn.setText(restaurantPromotionsList.get(position).getCouponTitle());
        holder.couponDescTxt.setText(restaurantPromotionsList.get(position).getCouponDesc());
        holder.couponExpiryDateTxt.setText(conversionDateFormat(restaurantPromotionsList.get(position).getDateExpire()));

        holder.couponIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInterface.handleClick(null, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantPromotionsList.size();
    }

    class RestaurantPromotionsViewHolder extends RecyclerView.ViewHolder {

        TextView couponDescTxt, couponExpiryDateTxt;
        Button couponIdBtn;

        public RestaurantPromotionsViewHolder(View itemView) {
            super(itemView);
            couponIdBtn = (Button) itemView.findViewById(R.id.coupon_id_btn);
            couponDescTxt = (TextView) itemView.findViewById(R.id.coupon_desc_txt);
            couponExpiryDateTxt = (TextView) itemView.findViewById(R.id.coupon_expiry_date_txt);
        }
    }


    private String conversionDateFormat(String expireDate) {

        String orderDate[] = (expireDate.split("T"));
        String dateSpilt[] = orderDate[0].split("-");
        String dateFormat = dateSpilt[1] + "/" + dateSpilt[2] + "/" + dateSpilt[0];
        return dateFormat;

    }
}
