package com.cns.plugin_clisitef;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Controller da tela de Confirma/Cancela
 */
public class SimNaoActivity extends Activity
{
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);
		String package_name = getApplication().getPackageName();
		Resources ResourceId = getApplication().getResources();
		setContentView(ResourceId.getIdentifier("sim_nao", "layout", package_name));
		//setContentView(R.layout.sim_nao);

		TextView tv = (TextView) findViewById (ResourceId.getIdentifier("tvSimNaoMsg", "id", package_name));
		tv.setText (getIntent ().getExtras ().getString ("message"));

		Button btn = (Button) findViewById (ResourceId.getIdentifier("btSNSim", "id", package_name));
		btn.setOnClickListener (new OnClickListener ()
		{
			public void onClick (View v)
			{
				Intent i = new Intent ();
				i.putExtra ("input", "0");
				setResult (RESULT_OK, i);
				finish ();
			}
		});

		btn = (Button) findViewById (ResourceId.getIdentifier("btSNNao", "id", package_name));
		btn.setOnClickListener (new OnClickListener ()
		{
			public void onClick (View v)
			{
				Intent i = new Intent ();
				i.putExtra ("input", "1");
				setResult (RESULT_OK, i);
				finish ();
			}
		});
		
		btn = (Button) findViewById (ResourceId.getIdentifier("btSNCancelar", "id", package_name));
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
