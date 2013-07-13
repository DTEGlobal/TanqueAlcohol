package tanque;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


}
