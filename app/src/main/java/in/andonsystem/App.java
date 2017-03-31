package in.andonsystem;

import android.app.Application;

import com.android.volley.RequestQueue;

import org.greenrobot.greendao.database.Database;

import in.andonsystem.v2.entity.DaoMaster;
import in.andonsystem.v2.entity.DaoSession;

/**
 * Created by razamd on 3/31/2017.
 */

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    public static final boolean ENCRYPTED = true;
    private DaoSession daoSession;
    private RequestQueue mRequestQueue;

    private String[] teams;
    private String[] problems;

    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "andonsys-db-encrypted" : "andonsys-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public String[] getTeams() {
        return teams;
    }

    public void setTeams(String[] teams) {
        this.teams = teams;
    }

    public String[] getProblems() {
        return problems;
    }

    public void setProblems(String[] problems) {
        this.problems = problems;
    }
}
