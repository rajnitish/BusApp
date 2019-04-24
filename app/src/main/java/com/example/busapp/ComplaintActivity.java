package com.example.busapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ComplaintActivity extends Activity {
    private EditText mEditTextSubject;
    private EditText mEditTextMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaints_view);
      //  mEditTextSubject=findViewById(R.id.edit_text_subject);
      //  mEditTextMessage=findViewById(R.id.edit_text_message);

         Button buttonSend = findViewById(R.id.buttonsend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
    }
    private void sendMail(){
//        String recipient = "anuj.avengerx@gmail.com";
        String[] recipientList=new String[]{"raj.nitp@gmail.com","anuj.avengerx@gmail.com","jaishreeram@gmail.com"};
        String subject = "Customer_Complaint: "+mEditTextSubject.getText().toString();
        String message = mEditTextMessage.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, recipientList);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, " Choose your email Client"));
    }
}
