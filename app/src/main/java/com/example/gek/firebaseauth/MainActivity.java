package com.example.gek.firebaseauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * Как объединить к одному юзеру несколько систем авторизаций
 *  http://stackoverflow.com/questions/37768529/how-to-link-multiple-auth-providers-to-an-firebase-account/37796597
 *  */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnGoogle).setOnClickListener(this);
        findViewById(R.id.btnFaceBook).setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnGoogle:
                startActivity(new Intent(this, MyGoogleActivity.class));
                break;
            case R.id.btnFaceBook:
                startActivity(new Intent(this, MyFaceBookActivity.class));
                break;
        }
    }



}