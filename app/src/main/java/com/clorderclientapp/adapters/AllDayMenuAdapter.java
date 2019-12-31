package com.clorderclientapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.activites.MenuItemActivity;
import com.clorderclientapp.modelClasses.CategoryModel;
import com.clorderclientapp.R;

import java.util.ArrayList;

public class AllDayMenuAdapter extends RecyclerView.Adapter<AllDayMenuAdapter.AllDayMenuViewHolder> {
    Context mContext;
    ArrayList<CategoryModel> allDayMenuList;

    public AllDayMenuAdapter(Context context, ArrayList<CategoryModel> allDayMenuArrayList) {
        mContext = context;
        allDayMenuList = allDayMenuArrayList;
    }

    @Override
    public AllDayMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.layout_all_day_menu_single_row, parent, false);
        return new AllDayMenuViewHolder(row);
    }

    @Override
    public void onBindViewHolder(AllDayMenuViewHolder holder, int position) {
        holder.menuListItemText.setText(allDayMenuList.get(position).getCategoryTitle());
    }

    @Override
    public int getItemCount() {
        return allDayMenuList.size();
    }

    class AllDayMenuViewHolder extends RecyclerView.ViewHolder {

        private TextView menuListItemText;
        private ImageView menuListArrow;

        public AllDayMenuViewHolder(View itemView) {
            super(itemView);
            menuListItemText = (TextView) itemView.findViewById(R.id.menu_list_item_txt);
            menuListArrow = (ImageView) itemView.findViewById(R.id.menu_list_arrow);

        }
    }
}
