package com.codewizards.meshify_chat.util;

import android.content.Context;
import android.content.res.Resources;

public class ViewUtil {
    public static int dpToPx(Context context, int dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static int dpToPx(int dp) {
        return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToSp(int dp) {
        return (int) (dpToPx(dp) / Resources.getSystem().getDisplayMetrics().scaledDensity);
    }
}
