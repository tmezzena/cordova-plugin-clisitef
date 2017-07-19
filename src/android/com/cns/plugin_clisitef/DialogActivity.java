package com.cns.plugin_clisitef;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cns.testesitef.R;

public class DialogActivity extends Activity
{
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);
		setContentView (R.layout.dialog);

		TextView tv = (TextView) findViewById (R.id.tvMsg);
		tv.setText (getIntent ().getExtras ().getString ("message"));

		Button btn = (Button) findViewById (R.id.btDlgOk);
		btn.setOnClickListener (new OnClickListener ()
		{
			public void onClick (View v)
			{
				EditText ed = (EditText) findViewById (R.id.edInput);
				Intent i = new Intent ();
				i.putExtra ("input", ed.getText ().toString ());
				setResult (RESULT_OK, i);
				finish ();
			}
		});

		btn = (Button) findViewById (R.id.btDlgCancelar);
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
