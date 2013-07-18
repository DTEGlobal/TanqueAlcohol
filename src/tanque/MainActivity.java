package tanque;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
    ToggleButton Motor1OnOff;
    ToggleButton Motor2OnOff;

    Timer ExploraESC;
    Timer UpdateUI;

    private double mNIVEL = 0.000875;
    private double bNIVEL = -0.595;
    private double AREATANQUE = 2.38;
    private String LEVELUNITS = "mts";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Motor1OnOff = (ToggleButton)findViewById(R.id.arranque_m1);
        Motor2OnOff = (ToggleButton)findViewById(R.id.arranque_m2);

        myG4Modbus = new G4Modbus("/dev/ttyS1",19200,1);


        AnalogInputsConfiguration myTankConfig = new AnalogInputsConfiguration();
        mNIVEL = myTankConfig.getM(1);
        bNIVEL = myTankConfig.getB(1);
        LEVELUNITS = myTankConfig.getUnits(1);


        ExploraESC = new Timer();
        ExploraESC.schedule(new TimerTask() {
            @Override
            public void run() {
                myG4Modbus.HeartBeat();
            }
        }, 0, 100);

        UpdateUI = new Timer();
        UpdateUI.schedule(new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        }, 0, 200);

        UpdateUI.schedule(new TimerTask() {
            @Override
            public void run() {
                Update_Buttons();
            }
        }, 0, 10);


        Motor1OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myG4Modbus.getBit("SD1")) {
                    myG4Modbus.setCoil(2, false);
                } else {
                    myG4Modbus.setCoil(2, true);
                }

            }
        });

        Motor1OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Thread myWait = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!myG4Modbus.getBit("SD1")) {
                            myG4Modbus.setCoil(2, false);
                        }
                        Thread.currentThread().interrupt();
                    }
                };
                myWait.start();
            }
        });

        Motor2OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myG4Modbus.getBit("SD2")) {
                    myG4Modbus.setCoil(3, false);
                } else {
                    myG4Modbus.setCoil(3, true);
                }

            }
        });

        Motor2OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Thread myWait = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!myG4Modbus.getBit("SD2")) {
                            myG4Modbus.setCoil(3, false);
                        }
                        Thread.currentThread().interrupt();
                    }
                };
                myWait.start();
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
                /* Ctas = 3700 = 100% */
                /* Ctas = 740  = 0%*/

                int Ctas = myG4Modbus.getAI(0);
                double nivel = (Ctas*mNIVEL)+bNIVEL;
                String nivelToPrint = String.format("Nivel:\n%.2f "+LEVELUNITS,nivel);
                level.setText(nivelToPrint);

                double vol = AREATANQUE*nivel*1000;
                String volumenToPrint = String.format("Volumen:\n%.2f lts",vol);
                volumen.setText(volumenToPrint);

                double mCtas = 100.0/(3700-740);
                double bCtas = -740*mCtas;

                Double percent = (mCtas*Ctas)+bCtas;

                tank.setProgress(percent.intValue());
            }
        });
    }
    private void Update_Buttons(){
        Motor1OnOff.post(new Runnable() {
            @Override
            public void run() {
                if(myG4Modbus.getBit("SD1")){
                    Motor1OnOff.setChecked(true);
                }
                else{
                    Motor1OnOff.setChecked(false);
                }
            }
        });

        Motor2OnOff.post(new Runnable() {
            @Override
            public void run() {
                if(myG4Modbus.getBit("SD2")){
                    Motor2OnOff.setChecked(true);
                }
                else{
                    Motor2OnOff.setChecked(false);
                }
            }
        });
    }
}

