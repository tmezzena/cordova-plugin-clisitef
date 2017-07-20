package com.cns.plugin_clisitef;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogActivity extends Activity
{
	private static String package_name = "";
	private static Resources ResourceId;

	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);

		package_name = getApplication().getPackageName();
		ResourceId = getApplication().getResources();
		setContentView(ResourceId.getIdentifier("dialog", "layout", package_name));
		//setContentView (R.layout.dialog);

		TextView tv = (TextView) findViewById (ResourceId.getIdentifier("tvMsg", "id", package_name));
		tv.setText (getIntent ().getExtras ().getString ("message"));

		Button btn = (Button) findViewById (ResourceId.getIdentifier("btDlgOk", "id", package_name));
		btn.setOnClickListener (new OnClickListener ()
		{
			public void onClick (View v)
			{
				EditText ed = (EditText) findViewById (ResourceId.getIdentifier("edInput", "id", package_name));
				Intent i = new Intent ();
				i.putExtra ("input", ed.getText ().toString ());
				setResult (RESULT_OK, i);
				finish ();
			}
		});

		btn = (Button) findViewById (ResourceId.getIdentifier("btDlgCancelar", "id", package_name));
		btn.setOnClickListener (new OnClickListener ()
		{
			public void onClick (View v)
			{
				Intent i = new Intent ();
				setResult (RESULT_CANCELED, i);
				finish ();
			}
		});
	}
}
