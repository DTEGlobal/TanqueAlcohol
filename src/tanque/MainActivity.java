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
