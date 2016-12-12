package gr.izikode.libs.iziviews.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

/**
 * Created by UserOne on 12/12/2016.
 */

public class IziContainerLayout extends FrameLayout {
    public IziContainerLayout(Context context) {
        super(context);
    }

    public IziContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IziContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IziContainerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        for (int index = 0; index < getChildCount(); index++)
            getChildAt(index).dispatchApplyWindowInsets(insets);

        return insets;
    }
}
