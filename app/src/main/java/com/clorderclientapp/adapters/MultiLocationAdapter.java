package com.clorderclientapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.modelClasses.MultiLocationModel;

import java.util.ArrayList;

public class MultiLocationAdapter extends RecyclerView.Adapter<MultiLocationAdapter.MultiLocationViewHolder> {

    private Context mContext;
    private ArrayList<MultiLocationModel> multiLocationList;

    public MultiLocationAdapter(Context mContext, ArrayList<MultiLocationModel> multiLocationModelArrayList) {
        this.mContext = mContext;
        multiLocationList=multiLocationModelArrayList;
    }

    @Override
    public MultiLocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.layout_multi_location_single_row, parent, false);
        return new MultiLocationAdapter.MultiLocationViewHolder(row);
    }

    @Override
    public void onBindViewHolder(MultiLocationViewHolder holder, int position) {
        holder.restTxt.setText(multiLocationList.get(position).getRestaurantName());
        holder.addressTxt.setText(multiLocationList.get(position).getAddress());
        holder.restImg.setBackgroundResource(R.mipmap.logo_oc);


    }

    @Override
    public int getItemCount() {
        return multiLocationList.size();
    }

    class MultiLocationViewHolder extends RecyclerView.ViewHolder {

        TextView restTxt,addressTxt;
        ImageView restImg;

        public MultiLocationViewHolder(View itemView) {
            super(itemView);
            restTxt = (TextView) itemView.findViewById(R.id.restNameTxt);
            addressTxt= (TextView) itemView.findViewById(R.id.addressTxt);
            restImg = (ImageView) itemView.findViewById(R.id.restImg);
        }
    }

}
