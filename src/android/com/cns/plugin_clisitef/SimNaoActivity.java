package com.cns.plugin_clisitef;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.cns.testesitef.R;

/**
 * Controller da tela de Confirma/Cancela
 */
public class SimNaoActivity extends Activity
{
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sim_nao);

		TextView tv = (TextView) findViewById (R.id.tvSimNaoMsg);
		tv.setText (getIntent ().getExtras ().getString ("message"));

		Button btn = (Button) findViewById (R.id.btSNSim);
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

		btn = (Button) findViewById (R.id.btSNNao);
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
		
		btn = (Button) findViewById (R.id.btSNCancelar);
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
