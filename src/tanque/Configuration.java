package tanque;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dte.tanque.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cesar on 7/13/13.
 */
public class Configuration  extends Activity {
    G4Modbus myG4modbus;
    Timer getAICounts;
    TextView counts;
    TextView eu;
    double mNIVEL,bNIVEL;

    EditText m;
    EditText b;
    EditText units;
    AnanlogInputsConfiguration myAIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.configuration);
        m = (EditText)findViewById(R.id.m);
        b = (EditText)findViewById(R.id.b);
        eu = (TextView)findViewById(R.id.eu);
        counts = (TextView)findViewById(R.id.counts);
        units = (EditText)findViewById(R.id.units);
        myG4modbus = new G4Modbus("/dev/ttyS1",19200,1);



        myAIC = new AnanlogInputsConfiguration();
        mNIVEL= myAIC.getM(1);
        String mNivelToPrint = String.format("%.6f ",mNIVEL);
        bNIVEL= myAIC.getB(1);
        String bNivelToPrint = String.format("%.6f ",bNIVEL);
        m.setHint("Current M: " + mNivelToPrint);
        b.setHint("Current B: " + bNivelToPrint);
        units.setHint("Current Units: " + myAIC.getUnits(1));












        getAICounts = new Timer();
        getAICounts.schedule(new TimerTask() {
            @Override
            public void run() {
                myG4modbus.HeartBeat();
                counts.post(new Runnable() {
                    @Override
                    public void run() {
                        counts.setText("" + myG4modbus.getAI(0));
                        double nivel = (myG4modbus.getAI(0) * mNIVEL) + bNIVEL;
                        String nivelToPrint = String.format("%.6f ", nivel);
                        eu.setText(nivelToPrint);
                    }
                });

            }
        },0,500);



        counts.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (m.getText().toString().equals("")) {

                } else {
                    Log.i("G4MB - Change Activity","m.getText = "+m.getText());
                    myAIC.updateM(1, Double.valueOf(m.getText().toString()));
                }
                if (b.getText().toString().equals("")) {

                } else {
                    myAIC.updateB(1, Double.valueOf(b.getText().toString()));
                }
                if (units.getText().toString().equals("")) {

                } else {
                    myAIC.updateUnits(1, units.getText().toString());
                }
                getAICounts.cancel();
                Intent i = new Intent();
                i.setClass(Configuration.this, MainActivity.class);
                Configuration.this.finish();
                startActivity(i);
                return false;
            }
        });

    }
}
