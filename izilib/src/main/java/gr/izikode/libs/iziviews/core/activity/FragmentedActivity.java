package gr.izikode.libs.iziviews.core.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import java.util.List;

import gr.izikode.libs.iziviews.util.Parameterized;
import gr.izikode.libs.iziviews.view.IziFragment;
import gr.izikode.libs.iziviews.view.PersistentFragment;


/**
 * Created by UserOne on 07/10/2017.
 */

public abstract class FragmentedActivity extends ContainerActivity {
    private static final String SAVED_INSTANCE_BUNDLE_KEY = "IziActivity_savedInstance_bundleKey";
    private static final String FRAGMENT_STACK_COUNT_INSTANCE_BUNDLE_KEY = "IziActivity_fragmentStackCount_bundleKey";

    private FragmentManager fragmentManager;
    private List<Fragment> currentFragments;

    private View fragmentContainer;
    private int fragmentStackCount;

    private PersistentFragment persistentFragment;

    public void addFragment(IziFragment fragment) {
        boolean addToBackStack = false;

        IziFragment topmost = getTopmostFragment();
        if (topmost != null && topmost.isStackable() && fragmentStackCount > 0) {
            addToBackStack = true;
        } else if (topmost != null && !topmost.isStackable()) {
            persistentFragment.removeRetainables(topmost);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (topmost != null && topmost.isStackable()) {
            transaction.setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation(),
                    topmost.getEnterAnimation(), topmost.getExitAnimation());
        } else {
            transaction.setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation());
        }

        transaction.add(fragmentContainer.getId(), fragment, fragment.tag);
        currentFragments.add(fragment);

        if (addToBackStack) {
            transaction.addToBackStack(topmost.tag);
        }

        if (fragment.isStackable()) {
            fragmentStackCount = (fragmentStackCount >= 0 ? fragmentStackCount : 0) + 1;
        }

        transaction.commit();

        if (topmost != null) {
            topmost.onPause();
        }
    }

    public void addFragment(Parameterized fragment) {
        try {
            addFragment(fragment.getFragment());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void addSoleFragment(IziFragment fragment) {
        clearFragments();
        addFragment(fragment);
    }

    public void addSoleFragment(Parameterized fragment) {
        try {
            addSoleFragment(fragment.getFragment());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public IziFragment getFragment(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null && fragment instanceof IziFragment) {
            return (IziFragment) fragment;
        }

        return null;
    }

    public IziFragment getTopmostFragment() {
        Fragment fragment = fragmentManager.findFragmentById(fragmentContainer.getId());
        if (fragment != null && fragment instanceof IziFragment) {
            return (IziFragment) fragment;
        }

        return null;
    }

    public void clearFragments() {
        for (Fragment fragment : fragmentManager.getFragments()) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }

        currentFragments.clear();
        persistentFragment.clearAll();

        fragmentStackCount = 0;
    }
}
