package com.clorderclientapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.clorderclientapp.modelClasses.ItemDetailsModel;
import com.clorderclientapp.R;

import java.util.ArrayList;


public class OrderDetailsAdapter extends BaseAdapter {
    int size = 3;
    Context context;
    ArrayList<ItemDetailsModel> orderDetailsList;


    public OrderDetailsAdapter(Context context, ArrayList<ItemDetailsModel> itemDetailsModelArrayList) {
        this.context = context;
        orderDetailsList = itemDetailsModelArrayList;
    }

    @Override
    public int getCount() {
        return orderDetailsList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderDetailsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_order_history_details_single_row, null);
            viewHolder.quantity = (TextView) convertView.findViewById(R.id.quantity);
            viewHolder.itemName = (TextView) convertView.findViewById(R.id.itemname);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            viewHolder.note = (TextView) convertView.findViewById(R.id.note);
            viewHolder.makeItWith = (TextView) convertView.findViewById(R.id.makeitwith);
            viewHolder.noteText = (TextView) convertView.findViewById(R.id.note_text);
            viewHolder.makeItWithText = (TextView) convertView.findViewById(R.id.makeitwith_text);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.quantity.setText(orderDetailsList.get(position).getItemQuantity());
        viewHolder.itemName.setText(orderDetailsList.get(position).getItemName());
        viewHolder.price.setText(String.format("%s", "$" + orderDetailsList.get(position).getTotalPrice()));
        String notes = orderDetailsList.get(position).getNote();
        String isNote = orderDetailsList.get(position).getIsNoteVisible();
        String isMakeItwith = orderDetailsList.get(position).getIsMakeItWithVisible();

        if (isNote.equals("1")) {
            viewHolder.note.setText(notes);
            viewHolder.note.setVisibility(View.VISIBLE);
            viewHolder.noteText.setVisibility(View.VISIBLE);
        } else {
            viewHolder.noteText.setVisibility(View.GONE);
            viewHolder.note.setVisibility(View.GONE);

        }

        String makeItWiths = orderDetailsList.get(position).getOptions();
        if (isMakeItwith.equals("1")) {
            viewHolder.makeItWith.setText(makeItWiths);
            viewHolder.makeItWith.setVisibility(View.VISIBLE);
            viewHolder.makeItWithText.setVisibility(View.VISIBLE);
        } else {
            viewHolder.makeItWithText.setVisibility(View.GONE);
            viewHolder.makeItWith.setVisibility(View.GONE);
        }

        return convertView;
    }

    public class ViewHolder {
        TextView quantity;
        TextView itemName;
        TextView price;
        TextView note;
        TextView makeItWith;
        TextView noteText;
        TextView makeItWithText;
    }


}
