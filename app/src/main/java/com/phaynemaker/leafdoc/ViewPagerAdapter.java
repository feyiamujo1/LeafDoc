package com.phaynemaker.leafdoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapter extends PagerAdapter {

    Context context;

    int images[] = {
            R.drawable.onb,
            R.drawable.onb,
            R.drawable.onb
    };

    int topics[] ={
            R.string.topic_one,
            R.string.topic_two,
            R.string.topic_three
    };

    int descriptions[] = {
            R.string.description_one,
            R.string.description_two,
            R.string.description_three
    };

    public ViewPagerAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return topics.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding_layout,null);
        //View view = layoutInflater.inflate(R.layout.onboarding_layout, container, false);

        ImageView slideImage = (ImageView) view.findViewById(R.id.ivImageOne);
        TextView slideTopic = (TextView) view.findViewById(R.id.tvTopic);
        TextView slideDescription = (TextView) view.findViewById(R.id.tvDescription);

        slideImage.setImageResource(images[position]);
        slideTopic.setText(topics[position]);
        slideDescription.setText(descriptions[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
