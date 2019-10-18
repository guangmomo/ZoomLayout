package com.xuliwen.zoom.demo;

import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class MyPagerAdapter extends PagerAdapter {


    @Override
    public int getCount() {
        return 3;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        if (position == 0) {
            view = View.inflate(container.getContext(), R.layout.layout_long_picture, null);
        } else if (position == 1) {
            view = View.inflate(container.getContext(), R.layout.layout_short_picture, null);
        } else {
            view = View.inflate(container.getContext(), R.layout.layout_other_picture, null);
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
