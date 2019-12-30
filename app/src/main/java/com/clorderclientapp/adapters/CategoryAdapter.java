package com.clorderclientapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
        String img = allDayMenuList.get(position).getImageUrl();
        if (img.equals("")) {
            img = "https://s3.amazonaws.com/Clorder/Client/chickenplate-wb.png";
        }
//        Glide.with(mContext).load(img).asBitmap().placeholder(R.mipmap.restaurant_128).into(new SimpleTarget<Bitmap>(150, 150) {
//            @Override
//            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                Drawable drawable = new BitmapDrawable(mContext.getResources(), resource);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.categoryName.setBackground(drawable);
//                }
//            }
//        });

        Glide.with(mContext).asBitmap().load(img).placeholder(R.mipmap.restaurant_128).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Drawable drawable = new BitmapDrawable(mContext.getResources(),resource);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    holder.categoryName.setBackground(drawable);
                }

            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

//        Glide.with(mContext).load(img).asBitmap().listener(new RequestListener<String, Bitmap>() {
//            @Override
//            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
//                return false;
//            }
//
//            @Override
//            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    Log.d("resource",""+resource);
//                    Drawable drawable = new BitmapDrawable(mContext.getResources(), resource);
//                    holder.categoryName.setBackground(drawable);
//                }
//                return false;
//            }
//        }).placeholder(R.mipmap.restaurant_128);
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

