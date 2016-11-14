package gr.izikode.libs.iziviews.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gr.izikode.libs.iziviews.view.IziTabFragment;

/**
 * Created by izi.
 */

public class IziTabAdapter extends FragmentPagerAdapter {
    private Context adapterContext;
    private List<Class<? extends IziTabFragment>> adapterData;

    public IziTabAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);

        adapterContext = context;
        adapterData = new ArrayList<>();
    }

    @Override
    public IziTabFragment getItem(int position) {
        try {
            return adapterData.get(position).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        try {
            IziTabFragment instance = adapterData.get(position).newInstance();

            int titleResource = instance.getTitleResource();
            String title = adapterContext.getResources().getString(titleResource);

            int iconResource = instance.getIconResource();

            SpannableStringBuilder spannableStringBuilder = null;
            if (iconResource != 0) {
                spannableStringBuilder = new SpannableStringBuilder("   " + title);
                Drawable icon = adapterContext.getResources().getDrawable(iconResource);

                icon.setBounds(5, 5, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(icon, DynamicDrawableSpan.ALIGN_BASELINE);

                spannableStringBuilder.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannableStringBuilder = new SpannableStringBuilder(title);
            }

            return spannableStringBuilder;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public TabLayout.Tab getTab(TabLayout layout, int position) {
        TabLayout.Tab tab = layout.newTab();
        tab.setText(getPageTitle(position));
        return tab;
    }

    public int getCount() {
        return adapterData.size();
    }

    public void addData(List<Class<? extends IziTabFragment>> appendingData) {
        adapterData.addAll(appendingData);
        notifyDataSetChanged();
    }

    public void addData(Class<? extends IziTabFragment>... appendingData) {
        addData(Arrays.asList(appendingData));
    }

    public void setData(List<Class<? extends IziTabFragment>> newData) {
        adapterData.clear();
        addData(newData);
    }

    public void setData(Class<? extends IziTabFragment>... newData) {
        setData(Arrays.asList(newData));
    }

    public ArrayList<Class<? extends IziTabFragment>> getData() {
        return new ArrayList<>(adapterData);
    }

    public int getOffscreenPageLimit() {
        int limit = adapterData.size() / 2;
        return limit >= 3 ? limit : 3;
    }
}
