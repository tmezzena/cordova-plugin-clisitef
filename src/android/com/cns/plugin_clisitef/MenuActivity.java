package com.cns.plugin_clisitef;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.cns.testesitef.R;

import java.util.StringTokenizer;

/**
 * Controller da tela de menu
 */
public class MenuActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.menu);

    TextView tv = (TextView) findViewById(R.id.tvTitle);
    tv.setText(getIntent().getExtras().getString("mensagemVisor"));
    
    ListView lv = (ListView) findViewById(R.id.lv);
    ArrayAdapter<String> itensMenu = new ArrayAdapter<String>(this, R.layout.menu_item);
    lv.setAdapter(itensMenu);
    lv.setOnItemClickListener(itemMenuClickListener);
    
    // Quebra o texto do getBuffer() retornado pela continua,
    // alimentando os itens de menu
    String itens = getIntent().getExtras().getString("message").toString ();
    StringTokenizer st = new StringTokenizer (itens, ";", false);
    while (st.hasMoreTokens ()) {
    	itensMenu.add (st.nextToken ());
    }
    
    Button btn = (Button) findViewById(R.id.btMenuCancelar);
    btn.setOnClickListener(new OnClickListener() {     
      public void onClick(View v) {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();        
      }
    });
  }
  
  private OnItemClickListener itemMenuClickListener = new OnItemClickListener() {
  	// Click do item de menu
    public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
      Intent i = new Intent();
      String item = ((TextView) v).getText().toString();
      StringTokenizer st = new StringTokenizer (item, ":", false);
      
      i.putExtra("input", st.nextToken ());
      setResult(RESULT_OK, i);
      finish();
    }
  };
}
