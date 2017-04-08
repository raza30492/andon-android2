package in.andonsystem.v2.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.List;

import in.andonsystem.App;
import in.andonsystem.R;
import in.andonsystem.v2.entity.Buyer;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.service.UserService;
import in.andonsystem.v2.util.Constants;

public class NotificationActivity2 extends AppCompatActivity {

    private final String TAG = NotificationActivity2.class.getSimpleName();

    private Context mContext;
    private App app;
    private UserService userService;
    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        app = (App) getApplication();
        userService = new UserService(app);
        userPref = getSharedPreferences(Constants.USER_PREF,0);
        User user = userService.findByEmail(userPref.getString(Constants.USER_EMAIL, null));

        List<Buyer> buyers = user.getBuyers();

        for (Buyer b: buyers){
            Log.d(TAG,"buyer: " + b.getName());
        }

    }

}
