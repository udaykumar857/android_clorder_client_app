package com.clorderclientapp.adapters;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.interfaces.ItemClick;
import com.clorderclientapp.modelClasses.CategoryModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.RecyclerViewClickListener;

import java.util.ArrayList;

public class AllDayMenuAdapter extends RecyclerView.Adapter<AllDayMenuAdapter.AllDayMenuViewHolder> {
    Context mContext;
    ArrayList<CategoryModel> allDayMenuList;
    private ItemClick mListener;

    public AllDayMenuAdapter(Context context, ArrayList<CategoryModel> allDayMenuArrayList) {
        mContext = context;
        allDayMenuList = allDayMenuArrayList;
//        mListener = (ItemClick) context;
    }

    @Override
    public AllDayMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.layout_all_day_menu_single_row, parent, false);
        return new AllDayMenuViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final AllDayMenuViewHolder holder, int position) {
        holder.menuListItemText.setText(allDayMenuList.get(position).getCategoryTitle());


//        if (allDayMenuList.get(position).isExpanded()) {
//            holder.menuItemRecyclerView.setVisibility(View.VISIBLE);
//            holder.menuListArrow.setImageResource(R.mipmap.rsz_up_arrow);
//        } else {
//            holder.menuItemRecyclerView.setVisibility(View.GONE);
//            holder.menuListArrow.setImageResource(R.mipmap.rsz_expand_button);
//        }

//        holder.categoryLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mListener.onParentClick(holder.getAdapterPosition());
//            }
//        });
//        LinearLayoutManager manager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
//        holder.menuItemRecyclerView.setLayoutManager(manager);
//        MenuItemAdapter menuItemAdapter = new MenuItemAdapter(mContext, Constants.CategoryItemList);
//        holder.menuItemRecyclerView.setAdapter(menuItemAdapter);
//        holder.menuItemRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(mContext,
//                new RecyclerViewClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
////                        mListener.onChildClick(holder.getAdapterPosition(), position);
//                    }
//                }));
    }

    @Override
    public int getItemCount() {
        return allDayMenuList.size();
    }

    class AllDayMenuViewHolder extends RecyclerView.ViewHolder {

        private TextView menuListItemText;
        private ImageView menuListArrow;
        RecyclerView menuItemRecyclerView;
        RelativeLayout categoryLayout;

        public AllDayMenuViewHolder(View itemView) {
            super(itemView);
            menuListItemText = (TextView) itemView.findViewById(R.id.menu_list_item_txt);
//            menuListArrow = (ImageView) itemView.findViewById(R.id.menu_list_arrow);
//            menuItemRecyclerView = (RecyclerView) itemView.findViewById(R.id.menuItemRecyclerView);
            categoryLayout = (RelativeLayout) itemView.findViewById(R.id.category_layout);
        }
    }
}