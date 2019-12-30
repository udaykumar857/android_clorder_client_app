package com.clorderclientapp.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clorderclientapp.R;
import com.clorderclientapp.modelClasses.CategoryItemModel;
import com.clorderclientapp.utils.Utils;

import java.util.ArrayList;


public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {
    Context mContext;
    ArrayList<CategoryItemModel> itemModelArrayList;

    public MenuItemAdapter(Context context, ArrayList<CategoryItemModel> categoryItemModelArrayList) {
        mContext = context;
        itemModelArrayList = categoryItemModelArrayList;
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.menu_item_single_row, parent, false);
        return new MenuItemViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final MenuItemViewHolder holder, final int position) {

        holder.itemNameText.setText(itemModelArrayList.get(position).getItemTitle());
        if (itemModelArrayList.get(position).getItemDesc().length() > 0) {
            holder.itemDescText.setVisibility(View.VISIBLE);
            holder.itemDescText.setText(itemModelArrayList.get(position).getItemDesc());
        } else {
            holder.itemDescText.setVisibility(View.GONE);
        }

        holder.itemCostText.setText(String.format("%s", "$" + Utils.roundFloatString(Float.parseFloat(itemModelArrayList.get(position).getItemPrice()), 2)));
        holder.itemLayout.setBackgroundColor(Color.parseColor("#F2F2F2"));

//        if ((position % 2) == 0) {
//            holder.itemLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
//        } else {
//            holder.itemLayout.setBackgroundColor(Color.parseColor("#F2F2F2"));
//        }

        if (itemModelArrayList.get(position).getItemDesc().length() > 0) {
            holder.itemDescText.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("lineCount", "" + position + "\t  " + "" + holder.itemDescText.getLineCount());
                    if (holder.itemDescText.getLineCount() > 2) {
                        holder.itemMoreText.setVisibility(View.VISIBLE);
                        holder.itemDescText.setMaxLines(2);
                    } else {
                        holder.itemMoreText.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            holder.itemMoreText.setVisibility(View.GONE);
        }

        Glide.with(mContext).load(itemModelArrayList.get(position).getItemImageUrl()).placeholder(R.mipmap.restaurant_128).into(holder.itemImage);

//        Glide.with(mContext).load(itemModelArrayList.get(position).getItemImageUrl()).asBitmap().listener(new RequestListener<String, Bitmap>() {
//            @Override
//            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
//                return false;
//            }
//
//            @Override
//            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                return false;
//            }
//        }).placeholder(R.mipmap.restaurant_128).into(holder.itemImage);

    }

    @Override
    public int getItemCount() {
        return itemModelArrayList.size();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {

        TextView itemNameText, itemCostText, itemDescText, itemMoreText;
        LinearLayout itemLayout;
        ImageView itemImage;


        public MenuItemViewHolder(View itemView) {
            super(itemView);
            itemNameText = (TextView) itemView.findViewById(R.id.item_name_text);
            itemCostText = (TextView) itemView.findViewById(R.id.item_cost_text);
            itemDescText = (TextView) itemView.findViewById(R.id.user_selected_options_text);
            itemMoreText = (TextView) itemView.findViewById(R.id.item_more_txt);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);
            itemImage = (ImageView) itemView.findViewById(R.id.item_image);
        }
    }
}
