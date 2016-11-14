package gr.izikode.libs.iziviews.view;

import android.support.annotation.NonNull;

/**
 * Created by UserOne on 09/11/2016.
 */

public interface LifecycleDelegates<T> {
    void onViewLoading();
    void onCreation();
    void onInitialization();
    void onRestoration(@NonNull T savedInstance);
    void preShown();
    void postShown();
    void preConcealed();
    void postConcealed();
    void preDismissed();
}