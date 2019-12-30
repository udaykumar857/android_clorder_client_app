package com.clorderclientapp.interfaces;


public interface ItemClick {
    
    void onParentClick(int position);

    void onChildClick(int parentPosition, int childPosition);
}
