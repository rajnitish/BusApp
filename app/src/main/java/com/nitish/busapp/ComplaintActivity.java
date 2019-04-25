package com.nitish.busapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ComplaintActivity extends Activity {
    private EditText mEditTextSubject;
    private EditText mNameText,mEmailText,mPhoneText,mFeedbackText;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaints_view);

        mNameText=findViewById(R.id.name_id);
        mEmailText=findViewById(R.id.email_id);
        mPhoneText=findViewById(R.id.phone_id);
        mFeedbackText=findViewById(R.id.text_id);


        Button buttonSend = findViewById(R.id.buttonsend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
    }
    private void sendMail(){

        String[] recipientList=new String[]{"raj.nitp@gmail.com","mpsiitd2018@gmail.com "};
        String subject = "Consumer Feedback/Complaint for BusApp V0.9";
        String message = "Dear BusApp Team \n\n" +"Greetings! "+ mFeedbackText.getText().toString() + "\n\n Thanks & Regards \n Name: " + mNameText.getText().toString() + "\n Email Id :" + mEmailText.getText().toString() + "\n Phone no: "
                + mPhoneText.getText().toString();


        boolean flag = false;

        if(mNameText.getText().toString().isEmpty() || mEmailText.getText().toString().isEmpty() || mPhoneText.getText().toString().isEmpty() || mFeedbackText.getText().toString().isEmpty())
        {
            progressDialog = new ProgressDialog(ComplaintActivity.this);
            progressDialog.setMessage("Please fill all details ");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
        else
            flag = true;

        if(flag) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, recipientList);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message);

            intent.setType("message/rfc822");
            startActivity(intent);
           // startActivity(Intent.createChooser(intent, " Choose your email Client"));

            progressDialog = new ProgressDialog(ComplaintActivity.this);
            progressDialog.setMessage("Thanks User for Feedback !!! ");
            progressDialog.setCancelable(true);
            progressDialog.show();

            int finishTime = 10; //10 secs
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, finishTime * 1000);
        }
    }
}
