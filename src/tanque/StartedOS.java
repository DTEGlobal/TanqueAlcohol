package tanque;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Created by Cesar on 7/15/13.
 */
public class StartedOS extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }

