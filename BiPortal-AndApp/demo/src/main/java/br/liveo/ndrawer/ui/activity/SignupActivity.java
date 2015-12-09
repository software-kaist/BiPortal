package br.liveo.ndrawer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    EditText _locationText;
    EditText _emailText;
    EditText _passwordText;
    EditText _ageText;
    Button _signupButton;
    TextView _loginLink;
    RadioGroup _sexGroup;
    RadioButton _sex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Intent intent = new Intent(this.getIntent());

        _locationText = (EditText)findViewById(R.id.input_addr);
        _emailText = (EditText)findViewById(R.id.input_email);
        _passwordText = (EditText)findViewById(R.id.input_password);
        _ageText = (EditText)findViewById(R.id.input_age);
        _signupButton = (Button)findViewById(R.id.btn_signup);
        _loginLink = (TextView)findViewById(R.id.link_login);

        _sexGroup = (RadioGroup) findViewById(R.id.radiogroup1);
        _sex = (RadioButton) findViewById(_sexGroup.getCheckedRadioButtonId());

        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        String address = _locationText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String age = _ageText.getText().toString();
        String sex = _sex.getText().toString();


        Log.d("info", email + "/" + password + "/" + address + "/" + age + "/" + sex);

        try {
            String url = "http://125.131.73.198:3000/signup";
            RequestClass rc = new RequestClass(url);
            rc.AddParam("useremail", email);
            rc.AddParam("userpassword", password);
            rc.AddParam("userlocation", address);
            rc.AddParam("usersex", sex);
            rc.AddParam("userage", age);

            rc.Execute(1);
            String response = rc.getResponse();

            if(response.length() != 0) {
                onSignupSuccess();
            } else {
                isUser();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void onSignupSuccess() {
        Toast.makeText(getBaseContext(), "가입하신 Email로 로그인하세요", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "잘못된 항목이 있습니다", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public void isUser() {
        Toast.makeText(getBaseContext(), "이미 가입된 Email입니다", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String location = _locationText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String age = _ageText.getText().toString();
        String sex = _sex.getText().toString();

        if (location.isEmpty()) {
            _locationText.setError("지역 항목이 비었습니다");
            valid = false;
        } else {
            _locationText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("올바른 이메일을 입력하세요");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("비밀번호를 입력하세요");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (age.isEmpty()) {
            _passwordText.setError("나이를 입력하세요");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (sex.isEmpty()) {
            _passwordText.setError("성별을 선택하세요");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}