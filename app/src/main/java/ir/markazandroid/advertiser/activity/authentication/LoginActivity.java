package ir.markazandroid.advertiser.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.activity.BaseActivity;
import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.network.OnResultLoaded;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.Phone;
import ir.markazandroid.advertiser.object.User;
import ir.markazandroid.advertiser.view.ButtonHandler;

/**
 * Coded by Ali on 06/02/2018.
 */

public class LoginActivity extends BaseActivity {

    private EditText username, password;
    private TextInputLayout usernameLayout, passwordLayout;
    private Button login;
    private ButtonHandler buttonHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        usernameLayout = findViewById(R.id.username_layout);
        passwordLayout = findViewById(R.id.password_layout);
        login = findViewById(R.id.login);

        buttonHandler = new ButtonHandler(this, login, 0, false);

        String uuid = getSharedPreferences("pref", MODE_PRIVATE).getString("uuid", null);
        if (uuid == null) {
            login.setOnClickListener(v -> {
                if (valid()) {
                    buttonHandler.click();
                    login();
                }
            });
        } else {
            // buttonHandler.click();
            doLogin(uuid);
        }

    }


    private void login() {
        User user = new User();
        user.setUsername(username.getText().toString());
        user.setPassword(password.getText().toString());
        user.setToken(FirebaseInstanceId.getInstance().getToken());

        getNetworkManager().register(user, new OnResultLoaded.ActionListener<Phone>() {
            @Override
            public void onSuccess(final Phone successResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSharedPreferences("pref", MODE_PRIVATE).edit().putString("uuid", successResult.getUuid()).apply();
                        doLogin(successResult.getUuid());
                    }
                });
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleServerError(error);
                    }
                });
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void doLogin(String uuid) {
        buttonHandler.click();
        getNetworkManager().login(uuid, new OnResultLoaded.ActionListener<Phone>() {
            @Override
            public void onSuccess(final Phone successResult) {
                runOnUiThread(() -> {
                    if (successResult.getStatus() == Phone.STATUS_NO_LOGIN)
                        proceedToEnterNameActivity();
                    else
                        proceedToMainActivity();
                });
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleServerError(error);
                    }
                });
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void proceedToEnterNameActivity() {
        Intent intent = new Intent(this, EnterNameActivity.class);
        startActivity(intent);
        finish();
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleServerError(ErrorObject error) {
        usernameLayout.setError(error.getMessage());
    }

    private boolean valid() {
        boolean valid = true;
        String name = username.getText().toString();
        String pass = password.getText().toString();
        passwordLayout.setErrorEnabled(false);
        usernameLayout.setErrorEnabled(false);

        if (name.isEmpty()) {
            usernameLayout.setError("نام کاربری نمی تواند خالی باشد");
            valid = false;
        }

        if (pass.isEmpty()) {
            passwordLayout.setError("رمز عبور نمی تواند خالی باشد");
            valid = false;
        }
        return valid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        buttonHandler.dispose();
    }


}
