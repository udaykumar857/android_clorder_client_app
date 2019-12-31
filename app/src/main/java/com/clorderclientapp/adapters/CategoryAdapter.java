package com.clorderclientapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.clorderclientapp.R;
import com.clorderclientapp.interfaces.ItemClick;
import com.clorderclientapp.modelClasses.CategoryModel;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context mContext;
    ArrayList<CategoryModel> allDayMenuList;
    private ItemClick mListener;

    public CategoryAdapter(Context context, ArrayList<CategoryModel> allDayMenuArrayList) {
        mContext = context;
        allDayMenuList = allDayMenuArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.category_single_row, parent, false);
        return new CategoryAdapter.ViewHolder(row);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.categoryName.setText(allDayMenuList.get(position).getCategoryTitle());

        Glide.with(mContext).load(allDayMenuList.get(position).getImageUrl()).asBitmap().listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Drawable drawable = new BitmapDrawable(mContext.getResources(), resource);
                    holder.categoryName.setBackground(drawable);
                }
                return false;
            }
        }).placeholder(R.mipmap.restaurant_128);

    }

    @Override
    public int getItemCount() {
        return allDayMenuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private FrameLayout categoryFrame;
        private TextView categoryName;
        private ImageView categoryImg;

        public ViewHolder(View itemView) {
            super(itemView);
//            categoryFrame = (FrameLayout) itemView.findViewById(R.id.categoryFrame);
            categoryName = (TextView) itemView.findViewById(R.id.categoryName);
//            categoryImg = (ImageView) itemView.findViewById(R.id.categoryImg);
        }

        @Override
        public void onClick(View v) {

        }
    }
}

