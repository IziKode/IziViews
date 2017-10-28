package gr.izikode.libs.iziviews.core.activity;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by UserOne on 08/10/2017.
 */

public abstract class ContainerActivity extends AppCompatActivity {

    protected abstract @LayoutRes int getContentResource();

    protected abstract @IdRes int getFragmentContainerId();
}
