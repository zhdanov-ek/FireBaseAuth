package com.example.gek.firebaseauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnGoogle;
    Button btnFaceBook;

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
                startActivity(new Intent(this, GoogleActivity.class));
                break;
            case R.id.btnFaceBook:
                startActivity(new Intent(this, FaceBookActivity.class));
                break;
        }
    }



}