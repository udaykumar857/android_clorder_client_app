package com.clorderclientapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clorderclientapp.modelClasses.OrderHistoryModel;
import com.clorderclientapp.utils.Utils;
import com.clorderclientapp.R;

import java.util.ArrayList;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {
    Context mContext;
    private ArrayList<OrderHistoryModel> orderHistoryList;

    public OrderHistoryAdapter(Context context, ArrayList<OrderHistoryModel> orderHistoryModelArrayList) {
        mContext = context;
        orderHistoryList = orderHistoryModelArrayList;
    }

    @Override
    public OrderHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.layout_order_history_single_row, parent, false);
        return new OrderHistoryViewHolder(row);
    }

    @Override
    public void onBindViewHolder(OrderHistoryViewHolder holder, int position) {
        holder.itemName.setText(orderHistoryList.get(position).getClientName());
        holder.orderIdTxt.setText(String.format("%s", "Order Id: " + orderHistoryList.get(position).getOrderId()));
        holder.orderDateTxt.setText(orderHistoryList.get(position).getOrderDate());
        holder.orderTotalTxt.setText(String.format("%s", "Total :$" + Utils.roundFloatString(Float.parseFloat(orderHistoryList.get(position).getOrderTotal()), 2)));
        holder.orderOptionsTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, orderIdTxt, orderTotalTxt, orderOptionsTxt, orderDateTxt;

        public OrderHistoryViewHolder(View itemView) {
            super(itemView);
            orderOptionsTxt = (TextView) itemView.findViewById(R.id.order_options_txt);
            orderTotalTxt = (TextView) itemView.findViewById(R.id.order_total_txt);
            orderIdTxt = (TextView) itemView.findViewById(R.id.order_id_txt);
            itemName = (TextView) itemView.findViewById(R.id.item_name);
            orderDateTxt = (TextView) itemView.findViewById(R.id.order_date_txt);
        }
    }
}
