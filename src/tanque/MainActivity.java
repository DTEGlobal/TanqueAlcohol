package tanque;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dte.tanque.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    G4Modbus myG4Modbus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myG4Modbus = new G4Modbus("/dev/ttyS1",19200);

        Timer ExploraESC = new Timer();
        ExploraESC.schedule(new TimerTask() {
            @Override
            public void run() {
                myG4Modbus.HeartBeat();
            }
        }, 0, 500);

        Timer UpdateUI = new Timer();
        UpdateUI.schedule(new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        }, 0, 1000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void Update(){
        final ImageView flow1_on = (ImageView)findViewById(R.id.flow1);
        final ImageView flow2_on = (ImageView)findViewById(R.id.flow2);
        final ImageView motor1_on = (ImageView)findViewById(R.id.motor1);
        final ImageView motor2_on = (ImageView)findViewById(R.id.motor2);
        final ProgressBar tank = (ProgressBar)findViewById(R.id.tank);

        final TextView level = (TextView)findViewById(R.id.nivel);
        final TextView volumen = (TextView)findViewById(R.id.volumen);


        flow1_on.post(new Runnable() {
            @Override
            public void run() {
                if (myG4Modbus.getBit("ED4")){
                    flow1_on.setVisibility(View.VISIBLE);
                }
                else{
                    flow1_on.setVisibility(View.INVISIBLE);
                }
            }
        });
        flow2_on.post(new Runnable() {
            @Override
            public void run() {
                if (myG4Modbus.getBit("ED5")){
                    flow2_on.setVisibility(View.VISIBLE);
                }
                else{
                    flow2_on.setVisibility(View.INVISIBLE);
                }
            }
        });
        motor1_on.post(new Runnable() {
            @Override
            public void run() {
                if (myG4Modbus.getBit("ED2")){
                    motor1_on.setVisibility(View.VISIBLE);
                }
                else{
                    motor1_on.setVisibility(View.INVISIBLE);
                }
            }
        });
        motor2_on.post(new Runnable() {
            @Override
            public void run() {
                if (myG4Modbus.getBit("ED3")){
                    motor2_on.setVisibility(View.VISIBLE);
                }
                else{
                    motor2_on.setVisibility(View.INVISIBLE);
                }
            }
        });
        tank.post(new Runnable() {
            @Override
            public void run() {
                /* Altura Max = 2.59m = 100% */
                /* Altura Min = 0m    = 0%*/

                final double mNIVEL = 0.000875;
                final double bNIVEL = -0.6475;
                final double AREATANQUE = 2.224;


                double nivel = (myG4Modbus.getAI(0)*mNIVEL)+bNIVEL;
                String nivelToPrint = String.format("Nivel: %.2f mts",nivel);
                level.setText(nivelToPrint);

                double vol = AREATANQUE*nivel*1000;
                String volumenToPrint = String.format("Volumen: %.2f lts",vol);
                volumen.setText(volumenToPrint);

                Double percent = (100*nivel)/2.59;
                tank.setProgress(percent.intValue());
            }
        });
    }
}
