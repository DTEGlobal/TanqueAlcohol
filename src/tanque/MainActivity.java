package tanque;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.dte.tanque.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    G4Modbus myG4Modbus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView flow1_on = (ImageView)findViewById(R.id.flow1);
        final ImageView flow2_on = (ImageView)findViewById(R.id.flow2);
        final ImageView motor1_on = (ImageView)findViewById(R.id.motor1);
        final ImageView motor2_on = (ImageView)findViewById(R.id.motor2);

        myG4Modbus = new G4Modbus("/dev/ttyS1",19200);

        Timer ExploraESC = new Timer();

        ExploraESC.schedule(new TimerTask() {
            @Override
            public void run() {
                myG4Modbus.HeartBeat();
                flow1_on.post(new Runnable() {
                    @Override
                    public void run() {
                        if (flow1_on.getVisibility() == View.INVISIBLE)
                            flow1_on.setVisibility(View.VISIBLE);
                        else
                            flow1_on.setVisibility(View.INVISIBLE);
                        if (flow2_on.getVisibility() == View.INVISIBLE)
                            flow2_on.setVisibility(View.VISIBLE);
                        else
                            flow2_on.setVisibility(View.INVISIBLE);
                        if (motor1_on.getVisibility() == View.INVISIBLE)
                            motor1_on.setVisibility(View.VISIBLE);
                        else
                            motor1_on.setVisibility(View.INVISIBLE);
                        if (motor2_on.getVisibility() == View.INVISIBLE)
                            motor2_on.setVisibility(View.VISIBLE);
                        else
                            motor2_on.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, 0, 500);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


}
