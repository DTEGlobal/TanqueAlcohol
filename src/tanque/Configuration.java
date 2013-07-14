package tanque;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dte.tanque.R;

import java.util.Timer;

/**
 * Created by Cesar on 7/13/13.
 */
public class Configuration  extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.configuration);
        EditText m = (EditText)findViewById(R.id.m);
        EditText b = (EditText)findViewById(R.id.b);
        EditText nivel = (EditText)findViewById(R.id.nivel);
        TextView counts = (TextView)findViewById(R.id.counts);
        TextView eu = (TextView)findViewById(R.id.eu);
        G4Modbus myG4modbus = new G4Modbus("/dev/ttyS1",19200,1);
        AnanlogInputsConfiguration myAIC = new AnanlogInputsConfiguration();
        m.setHint("Current M: "+String.valueOf(myAIC.getM(1)));
        b.setHint("Current B: " + String.valueOf(myAIC.getB(1)));
        eu.setHint("Current Units: " + String.valueOf(myAIC.getUnits(1)));
        counts.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent();
                i.setClass(Configuration.this, MainActivity.class);
                Configuration.this.finish();
                startActivity(i);
                return false;
            }
        });

    }
}
