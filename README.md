# IziViews
<p><strong>IziViews is an Android library that simplifies and enhances the creation and initialization of an Application Project.</strong></p>
<p>Extended Activities, Fragments and Views, enhanced lifecycle Delegates, automations and new features to remove Android hassle and let the Developer focus on Development.</p>
<p>I have been developing and using this library for quite some time and decided to upload it, for anyone who might benefit from it and anyone willing to contribute to optimizing and expanding it.</p>
<br/>

## Features
 - [x] Extended Activities, Fragments and Views, with enhanced lifecycle delegates
 - [x] Automated View initialization, you won't have to do <strong>any</strong> findViewById
 - [x] Automatically saved instance state, simplified and enriched restoration
 - [x] Easily retain properties and control their behaviour throughout their lifecycle

## Usage
### Activity
```java
public class SampleActivity extends IziActivity {
    private Button sampleButton;
    private TextView sampleTextView;

    @Override
    protected int getContentResource() {
        return R.layout.activity_sample;
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.sample_fragment_container;
    }

    @Override
    public void onCreation() {
        sampleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),
                        "This is the creation delegate, execute on every configuration change",
                        Toast.LENGTH_LONG).show();
                
                // Add a new fragment
                addFragment(new SampleFragment());
            }
        });
    }

    @Override
    public void onInitialization() {
        sampleTextView.setText("This is the initialization delegate, executed only once.");
    }

    @Override
    public void onRestoration(@NonNull IziActivity savedInstance) {
        // Here is where we restore properties
    }

    @Override
    public void postShown() {
        // Executed just after the activity became visible
    }

    @Override
    public void postConcealed() {
        // Executed just after the activity became invisible
    }
}
```
<br/>
### Fragment
```java
public class SampleFragment extends IziFragment {
    private Button sampleButton2;
    
    @Override
    protected int getContentResource() {
        return R.layout.fragment_sample;
    }

    @Override
    public void onCreation() {
        sampleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add a new fragment to the Activity
                getParent().addFragment(new AnotherFragment());
            }
        });
    }

    @Override
    public void onInitialization() {

    }

    @Override
    public void onRestoration(@NonNull IziFragment savedInstance) {

    }

    @Override
    public void postShown() {

    }

    @Override
    public void postConcealed() {

    }
}
```
<br/>
### Restoring a property's state
<p>Set it to be saved, by tagging it with <strong>SaveForInstanceRestoration</strong>.</p>
```java
@SaveForInstanceRestoration
private TextView sampleTextView;
```
<p>Handle the restoration process on the <strong>onRestoration</strong> delegate.</p>
```java
@Override
public void onRestoration(@NonNull IziActivity savedInstance) {
    sampleTextView.setText(((SampleActivity) savedInstance).sampleTextView.getText());
}
```
<p>Views in the savedInstance object <strong>do not have</strong> a Context, to avoid leaking, so do not assign them directly, just get the desired data.</p>
<p>The restoration process is identical with Fragments.</p>
<br/>
### Retaining complex components, like Asyncs and Sockets
<p>Declare the Retainable object, adapting it to the desired type.</p>
```java
private Retainable<AsyncTask<Void, Void, Void>> asyncContainer;
```
<p>Initialize the component and the Retainable container.</p>
```java
AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>() { ... };

asyncContainer = new Retainable<AsyncTask<Void, Void, Void>>() {
    @Override
    public void preSubmerged(AsyncTask<Void, Void, Void> value) {
        // Do something, like removing the ui of the task
    }

    @Override
    public void postSurfaced(AsyncTask<Void, Void, Void> value) {
        // Do something, like restoring the ui of the task
        if (value.getStatus().equals(AsyncTask.Status.RUNNING)) {
            // restore the ui
        }
    }
};
```
<p>Assign the component to the Retainable container.</p>
```java
asyncContainer.setValue(async);
```
