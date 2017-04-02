package in.andonsystem.v2.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import in.andonsystem.R;
import in.andonsystem.v2.util.LoginUtil;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private final String TAG = AuthenticatorActivity.class.getSimpleName();

    private EditText username;
    private EditText password;

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private String mAccountUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        Log.d(TAG,"onCreate()");

        username = (EditText) findViewById(R.id.userId);
        password = (EditText) findViewById(R.id.password);

        mAccountManager = AccountManager.get(this);

        final Intent intent = getIntent();
        mAccountUsername = intent.getStringExtra(AuthConstants.ARG_ACCOUNT_USERNAME);
        mAuthTokenType = intent.getStringExtra(AuthConstants.ARG_AUTH_TOKEN_TYPE);

        if(mAuthTokenType == null){
            mAuthTokenType = AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS;
        }

        if(mAccountUsername != null){
            username.setText(mAccountUsername);
        }

    }

    public void signIn(View v){
        mAccountUsername = username.getText().toString();
        String passwd = password.getText().toString();

        String accountType = getIntent().getStringExtra(AuthConstants.ARG_ACCOUNT_TYPE);

        Bundle resp = LoginUtil.authenticate(mAccountUsername,passwd);

        if (resp.getString(AuthConstants.ARG_AUTHENTICATION_ERROR) != null){
            Toast.makeText(this,resp.getString(AuthConstants.ARG_AUTHENTICATION_ERROR),Toast.LENGTH_LONG).show();
            return;
        }

        //handle fail cases

        String authToken = resp.getString(AuthConstants.ARG_ACCESS_TOKEN);
        String refreshToken = resp.getString(AuthConstants.ARG_REFRESH_TOKEN);
        Log.i(TAG,"refreshToken = " + refreshToken);

        final Account account = new Account(mAccountUsername, AuthConstants.VALUE_ACCOUNT_TYPE);

        if(getIntent().getBooleanExtra(AuthConstants.ARG_IS_ADDING_NEW_ACCOUNT, false)){
            mAccountManager.addAccountExplicitly(account,null,null);
            mAccountManager.setAuthToken(account,AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS,authToken);
        }
        mAccountManager.setUserData(account,AuthConstants.ARG_REFRESH_TOKEN, resp.getString(AuthConstants.ARG_REFRESH_TOKEN));

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, mAccountUsername);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        data.putString(AccountManager.KEY_AUTHTOKEN, authToken);

        Intent intent = new Intent();
        intent.putExtras(data);

        setAccountAuthenticatorResult(data);
        finish();
    }


}
