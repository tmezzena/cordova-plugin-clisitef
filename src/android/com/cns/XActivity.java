package com.cns;

import android.app.Activity;
import android.os.Bundle;

public class XActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("activity_x", "layout", package_name));
    }
}
