package gr.izikode.libs.iziviews.core.delegate;

import android.support.annotation.NonNull;

/**
 * Created by UserOne on 10/10/2017.
 */

public interface LifecycleDelegates<S> {
    void onCreation();
    void onInitialization();
    void onRestoration(@NonNull S savedInstance);

    void preShown();
    void postShown();
    void preConcealed();
    void postConcealed();

    void preDismissed();
}
