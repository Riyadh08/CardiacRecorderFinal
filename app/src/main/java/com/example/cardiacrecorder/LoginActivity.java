package com.example.cardiacrecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardiacrecorder.classes.DataModel;
import com.example.cardiacrecorder.classes.EachData;
import com.example.cardiacrecorder.roomDb.BoardViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText signInEmailEditText, signInPasswordEditText;
    private TextView signUpTextView;
    private Button signInButton;

    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("Sign In Activity");

        mAuth = FirebaseAuth.getInstance();

        signInEmailEditText = findViewById(R.id.signInEmailEditTextId);
        signInPasswordEditText = findViewById(R.id.signInPasswordEditTextId);
        signInButton = findViewById(R.id.signInButtonId);
        signUpTextView = findViewById(R.id.signUpTextViewId);
        progressBar = findViewById(R.id.ProgressBarId);

        signUpTextView.setOnClickListener(this);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.signInButtonId){
            UserLogin();
        }
        else if(view.getId() == R.id.signUpTextViewId){
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        }


    }

    private void UserLogin() {
        String email = signInEmailEditText.getText().toString().trim();
        String password = signInPasswordEditText.getText().toString().trim();

        if(email.isEmpty()){
            signInEmailEditText.setError("Enter an email address");
            signInEmailEditText.requestFocus();
            return;
        }

        if(password.isEmpty()){
            signInPasswordEditText.setError("Enter a password");
            signInPasswordEditText.requestFocus();
            return;
        }

        if(password.length() < 5){
            signInPasswordEditText.setError("Password should be at least 5 characters long");
            signInPasswordEditText.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
//                        finish();
                    downloadData((error, allData) -> {

                        if(error != null) {
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        BoardViewModel viewModel = new ViewModelProvider(LoginActivity.this).get(BoardViewModel.class);
                        for(EachData data : allData){
                            viewModel.insert(data);
                        }

                        SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("amILoggedIn",true);
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this,HomePage.class));
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    });
                }
                else{

                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void downloadData(DataListener listener){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getUid();

        if(userId == null){
            listener.onDataDownloaded(getString(R.string.failed_to_authenticate),null);
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("data").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<EachData> list = new ArrayList<>();
                if(snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            DataModel model = ds.getValue(DataModel.class);
                            if(model != null){
                                list.add(new EachData(model));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                listener.onDataDownloaded(null,list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDataDownloaded(error.getMessage(),null);
            }
        });
    }

    private interface DataListener{
        void onDataDownloaded(String error, List<EachData> allData);
    }
}