package com.example.gek.firebaseauth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * / Прежде чем начать необходимо включить в консоли файрбейс аутентификацию через фейсбук.
 * Потом зайти на https://developers.facebook.com и добавить свое приложение после чего получить
 * идентификатор и ключ, который нужно ввести в коносль файрбейс (Auth/FaceBook)
 * Затем с помощью keytool получить ключ, который ввести в проект фейсбук через консоль
 * разработчика.
 *
 *  В build.gradle добавляем mavenCentral() и com.facebook.android:facebook-android-sdk:4.9.0
 *
 * Добавьте новую строку с именем facebook_app_id в strings.xml.
 * В качестве значения используйте свой ID приложения Facebook.
 * В манифесте добавьте строку с мета данными указывающую на выше добавленную строку с ID
 *
 *
 * Если к учетке файрбейса привязан эмейл, который уже есть в списке юзеров ФАЙРБЕЙС, то авторизация
 * не пройдет. Будет ошибка с указанием коллизии емейлов. Если первый зарегит емейл именно фейсбук
 * то гугл затрет учетку фейсбука и все таки авторизируется
 *
 * https://developers.facebook.com/docs/android/getting-started
 * https://developers.facebook.com/docs/facebook-login/android
 *
 */
public class MyFaceBookActivity extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = "FACEBOOK_ACTIVITY";
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;
    private TextView tvStatusFireBase, tvStatusFacebook, tvLog;
    private Button btnSignOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_face_book);

        tvStatusFireBase = (TextView) findViewById(R.id.tvStatusFireBase);
        tvStatusFacebook = (TextView) findViewById(R.id.tvStatusFacebook);
        tvLog = (TextView) findViewById(R.id.tvLog);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();

        // Проверяем есть ли аутентификация файрбейс
        if (mAuth.getCurrentUser() != null){
            print("OnCreate: user FIREBASE not null");
            String s = "Provider: " + mAuth.getCurrentUser().getProviderId() +
                    "\nEmail: " + mAuth.getCurrentUser().getEmail() +
                    "\nUid: " + mAuth.getCurrentUser().getUid();
            print(s);
            tvStatusFireBase.setText(s);
        }

        // лисенер аутентификации в файрбейс
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    String s = "Provider: " + user.getProviderId() +
                            "\nEmail: " + user.getEmail() +
                            "\nUid: " + user.getUid();
                    tvStatusFireBase.setText(s);
                    print(" \nFirebase AuthListener catch change state \n Sign IN.\n User ID: " + user.getUid());
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    mDb.child("free").child("history_sign_in").push().setValue(user.getEmail());

                    mDb.child("users").child(user.getUid()).child("user_name").setValue(user.getDisplayName());
                    mDb.child("users").child(user.getUid()).child("user_email").setValue(user.getEmail());
                    mDb.child("users").child(user.getUid()).child("user_provider").setValue("facebook");

                } else {
                    // User is signed out
                    tvStatusFireBase.setText("no user");
                    print(" \nFirebase AuthListener catch change state \n Sign OUT");
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                print("Work facebook-callback (onSuccess):\n " + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        // Полученный результат с Facebook SDK передаем в CallbackManager
        print("\nReceive result from Facebook SDK");
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Аутентификация в файрбейс через полученный с помощью фейсбука токен
    private void handleFacebookAccessToken(AccessToken token) {
        print("\nAuth in FireBase with Facebook token " + token);
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MyFaceBookActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            // Если емейл уже в файрбейсе такой есть то это ошибка будет такого типа
                            // Такая ситуация возникает когда до фейбука аутентификацию прошел к примеру
                            // гугл с таким же емейлом
                            if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "User with Email id already exists",
                                        Toast.LENGTH_SHORT).show();
                                print("\n ERROR \n" + task.getException().toString());
                            }
                            // очищаем данные по фейсбуку
                            LoginManager.getInstance().logOut();
                            tvStatusFacebook.setText("no user");
                        } else {
                            tvStatusFacebook.setText(credential.getProvider() + "\n " + credential.toString());
                        }
                    }
                });
    }

    public void signOut() {
        // Убираем утентификацию с FireBase
        mAuth.signOut();
        // Убираем утентификацию с Facebook
        LoginManager.getInstance().logOut();
        tvStatusFacebook.setText("no user");
        print("\n CLEAR ALL AUTH\n");
    }



    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnSignOut) {
            signOut();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void print(String text){
        tvLog.append(text + "\n");
    }

}
