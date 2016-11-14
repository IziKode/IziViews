package gr.izikode.libs.iziviews.view;

import android.support.annotation.NonNull;

/**
 * Created by izi.
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