package in.andonsystem;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.andonsystem.v2.activity.HomeActivity2;
import in.andonsystem.v2.adapter.RadioAdapter;
import in.andonsystem.v2.authenticator.AuthConstants;
import in.andonsystem.v2.util.Constants;

public class AuthActivity extends AppCompatActivity {

    private final String TAG = AuthActivity.class.getSimpleName();

    private Context mContext;

    private ListView listView;
    private int mSelected = -1;
    private AccountManager mAccountManager;
    private Account[] accounts;
    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.i(TAG, "onCreate()");
        AppClose.activity2 = this;
        mContext = this;
        mAccountManager = AccountManager.get(this);
        userPref = getSharedPreferences(Constants.USER_PREF,0);

        listView = (ListView) findViewById(R.id.list_view_accounts);


        //accounts = mAccountManager.getAccountsByType(AuthConstants.VALUE_ACCOUNT_TYPE);
        accounts = mAccountManager.getAccounts();
        if(accounts.length == 0){
            getTokenForAccountCreateIfNeeded();
            return;
        }
        Log.i(TAG, "no of accounts: " + accounts.length);

        List<String> list = new ArrayList<>();
        for (Account a : accounts){
            list.add(a.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick");
                view.setSelected(true);
                mSelected = position;
            }
        });

    }

    public void login(View v){
        Log.i(TAG, "selected = " + mSelected);
        if (mSelected == -1){
            showMessage("Select an account");
            return;
        }
        Account account = accounts[mSelected];
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    processResult(bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,e.getMessage());
                    //showMessage(e.getMessage());
                }
            }
        }).start();
    }

    public void addAccount(View v){
        Log.i(TAG, "selected = " + mSelected);
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(AuthConstants.VALUE_ACCOUNT_TYPE, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    processResult(bnd);

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }, null);
    }

    public void deleteAccount(View v){
        if (mSelected == -1){
            showMessage("Select an account");
            return;
        }
        Log.i(TAG, "selected = " + mSelected);
        Account account = accounts[mSelected];

        mAccountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                try {
                    Boolean result = future.getResult();
                    updateListView();
                    Log.i(TAG, "Account removed =" + result);
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                }

            }
        }, null);
    }

    private void getTokenForAccountCreateIfNeeded() {
        AccountManager mAccountManager = AccountManager.get(this);
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(AuthConstants.VALUE_ACCOUNT_TYPE, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            processResult(bnd);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
    }

    private void processResult(Bundle result){
        String username = result.getString(AccountManager.KEY_ACCOUNT_NAME);
        String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putString(Constants.USER_EMAIL,username);
        editor.putString(Constants.USER_ACCESS_TOKEN,authToken);
        editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
        editor.commit();
        Intent i = new Intent(this, HomeActivity2.class);
        startActivity(i);
    }

    private void updateListView(){
        accounts = mAccountManager.getAccounts();
        Log.i(TAG, "no of accounts: " + accounts.length);

        List<String> list = new ArrayList<>();
        for (Account a : accounts){
            list.add(a.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick");
                view.setSelected(true);
                mSelected = position;
            }
        });
    }

    private void showMessage(String msg){
        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
    }
}
