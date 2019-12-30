package com.clorderclientapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.interfaces.HandleInterface;
import com.clorderclientapp.utils.Utils;

import io.realm.RealmList;


public class CartDetailsAdapter extends BaseAdapter {
    Context mContext;
    RealmList<CartItemModel> cartItemList;
    HandleInterface handleInterface;
    private float totalItemCost = 0.0f;


    public CartDetailsAdapter(Context context, RealmList<CartItemModel> cartItemModelArrayList) {
        cartItemList = cartItemModelArrayList;
        mContext = context;
        handleInterface = (HandleInterface) context;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cart_items_single_row, null);
            viewHolder.itemNameText = (TextView) convertView.findViewById(R.id.item_name_text);
            viewHolder.itemSelectedOptionsText = (TextView) convertView.findViewById(R.id.user_selected_options_text);
            viewHolder.itemQtyText = (TextView) convertView.findViewById(R.id.item_qty_text);
            viewHolder.itemCostText = (TextView) convertView.findViewById(R.id.item_cost_text);
            viewHolder.itemDeleteImage = (ImageView) convertView.findViewById(R.id.item_delete_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.itemNameText.setText(cartItemList.get(position).getItemTitle());
        if (cartItemList.get(position).getUserSelectedModifierOptions().equals("")) {
            viewHolder.itemSelectedOptionsText.setVisibility(View.GONE);
        } else {
            viewHolder.itemSelectedOptionsText.setVisibility(View.VISIBLE);
            viewHolder.itemSelectedOptionsText.setText(cartItemList.get(position).getUserSelectedModifierOptions());
        }

        viewHolder.itemQtyText.setText(String.format("%s", cartItemList.get(position).getItemOrderQuantity()));
        totalItemCost = cartItemList.get(position).getTotalItemPrice();
        viewHolder.itemCostText.setText(String.format("$%s", Utils.roundFloatString(totalItemCost, 2)));


        viewHolder.itemDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInterface.handleClick(null, position);
            }
        });

        return convertView;
    }


    public class ViewHolder {
        TextView itemNameText, itemSelectedOptionsText, itemQtyText, itemCostText;
        ImageView itemDeleteImage;
    }


}
