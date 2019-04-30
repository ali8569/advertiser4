package ir.markazandroid.advertiser.activity.authentication;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.activity.BaseActivity;
import ir.markazandroid.advertiser.network.OnResultLoaded;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.Phone;
import ir.markazandroid.advertiser.view.ButtonHandler;

public class EnterNameActivity extends BaseActivity {

    private EditText username;
    private TextInputLayout usernameLayout;
    private Button submit;
    private ButtonHandler buttonHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);

        username=findViewById(R.id.username);
        usernameLayout=findViewById(R.id.username_layout);
        submit=findViewById(R.id.submit);

        buttonHandler=new ButtonHandler(this,submit,0,false);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()){
                    buttonHandler.click();
                    submit();
                }
            }
        });
    }

    private boolean valid() {
        boolean valid =true;
        String name = username.getText().toString();
        usernameLayout.setErrorEnabled(false);

        if (name.isEmpty()){
            usernameLayout.setError("نام کاربری نمی تواند خالی باشد");
            valid=false;
        }
        return valid;
    }


    private void submit() {

        getNetworkManager().sendName(username.getText().toString(), new OnResultLoaded.ActionListener<Phone>() {
            @Override
            public void onSuccess(final Phone successResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        proceedToMainActivity(successResult);
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
                        Toast.makeText(EnterNameActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void proceedToMainActivity(Phone phone) {
        Intent intent = new Intent(this,ShowAuthenticationDetailsActivity.class);
        intent.putExtra(ShowAuthenticationDetailsActivity.PHONE,phone);
        startActivity(intent);
        finish();
    }

    private void handleServerError(ErrorObject error) {
        usernameLayout.setError(error.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        buttonHandler.dispose();
    }
}
