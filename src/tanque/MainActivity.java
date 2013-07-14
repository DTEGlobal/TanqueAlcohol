package tanque;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dte.tanque.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    G4Modbus myG4Modbus;
    ToggleButton ProcessOnOff;
    Timer ExploraESC;
    Timer UpdateUI;

    private double mNIVEL = 0.000875;
    private double bNIVEL = -0.6475;
    private double AREATANQUE = 2.224;
    private String LEVELUNITS = "mts";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ProcessOnOff = (ToggleButton)findViewById(R.id.toggleButton);

        myG4Modbus = new G4Modbus("/dev/ttyS1",19200,1);


        AnanlogInputsConfiguration myTankConfig = new AnanlogInputsConfiguration();
        mNIVEL = myTankConfig.getM(1);
        bNIVEL = myTankConfig.getB(1);
        LEVELUNITS = myTankConfig.getUnits(1);


        ExploraESC = new Timer();
        ExploraESC.schedule(new TimerTask() {
            @Override
            public void run() {
                myG4Modbus.HeartBeat();
            }
        }, 0, 500);

        UpdateUI = new Timer();
        UpdateUI.schedule(new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        }, 0, 1000);

        ProcessOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ProcessOnOff.isChecked()){
                    myG4Modbus.setCoil(1, true);
                }
                else {
                    myG4Modbus.setCoil(1,false);
                }
            }
        });

        ImageView ConfigScreen = (ImageView)findViewById(R.id.mimico);
        ConfigScreen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent();
                i.setClass(MainActivity.this, Configuration.class);
                UpdateUI.cancel();
                ExploraESC.cancel();
                MainActivity.this.finish();
                startActivity(i);
                return false;
            }
        });

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



        ProcessOnOff.post(new Runnable() {
            @Override
            public void run() {
                if(myG4Modbus.getBit("BC0")){
                    ProcessOnOff.setChecked(true);
                }
                else{
                    ProcessOnOff.setChecked(false);
                }
            }
        });


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

                double nivel = (myG4Modbus.getAI(0)*mNIVEL)+bNIVEL;
                String nivelToPrint = String.format("Nivel: %.2f "+LEVELUNITS,nivel);
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

