package ir.markazandroid.advertiser.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.activity.BaseActivity;
import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.object.Phone;

public class ShowAuthenticationDetailsActivity extends BaseActivity {

    public static final String PHONE="ShowAuthenticationDetailsActivity.PHONE" ;

    private TextView textView;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_authentication_details);

        Phone phone = (Phone) getIntent().getSerializableExtra(PHONE);

        textView=(TextView) findViewById(R.id.username_layout);
        submit=findViewById(R.id.submit);

        textView.setText("نام دستگاه: "+phone.getName()+"\r\n"+"رمز: "+phone.getPassword());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowAuthenticationDetailsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
