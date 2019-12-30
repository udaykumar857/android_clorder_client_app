package com.clorderclientapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.utils.Utils;

import io.realm.RealmList;

public class OrderConfirmationListAdapter extends BaseAdapter {
    Context mContext;
    private RealmList<CartItemModel> cartItemList;

    public OrderConfirmationListAdapter(Context context, RealmList<CartItemModel> cartItemModelArrayList) {
        mContext = context;
        cartItemList = cartItemModelArrayList;
    }

    @Override
    public int getCount() {
        return cartItemList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_order_confirmation_single_row, null);
            viewHolder.itemQty = (TextView) convertView.findViewById(R.id.item_qty);
            viewHolder.itemName = (TextView) convertView.findViewById(R.id.item_name);
            viewHolder.itemPrice = (TextView) convertView.findViewById(R.id.item_price);
            viewHolder.modifierTxt = (TextView) convertView.findViewById(R.id.modifier_txt);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.itemQty.setText(String.format("%s", cartItemList.get(position).getItemOrderQuantity() + "X"));
        viewHolder.itemName.setText(String.format("%s", cartItemList.get(position).getItemTitle()));
        viewHolder.itemPrice.setText(String.format("%s", "$" + Utils.roundFloatString(cartItemList.get(position).getTotalItemPrice(), 2)));
        viewHolder.modifierTxt.setText(cartItemList.get(position).getUserSelectedModifierOptions());

        return convertView;
    }

    public class ViewHolder {
        TextView itemQty, itemName, itemPrice, modifierTxt;
    }
}
