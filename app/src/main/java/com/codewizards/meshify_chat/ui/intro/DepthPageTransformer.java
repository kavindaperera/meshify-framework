package com.codewizards.meshify_chat.ui.intro;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        int width = page.getWidth();
        if (position < -1.0f) {
            page.setAlpha(0.0f);
        } else if (position <= 0.0f) {
            page.setAlpha(1.0f);
            page.setTranslationX(0.0f);
            page.setScaleX(1.0f);
            page.setScaleY(1.0f);
        } else if (position <= 1.0f) {
            page.setAlpha(1.0f - position);
            page.setTranslationX(((float) width) * (-position));
            float abs = ((1.0f - Math.abs(position)) * 0.25f) + 0.75f;
            page.setScaleX(abs);
            page.setScaleY(abs);
        } else {
            page.setAlpha(0.0f);
        }
    }
}
