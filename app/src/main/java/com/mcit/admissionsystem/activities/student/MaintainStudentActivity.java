package com.mcit.admissionsystem.activities.student;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.AbstractAsyncActivity;
import com.mcit.admissionsystem.entities.Student;
import com.mcit.admissionsystem.entities.User;
import com.mcit.admissionsystem.http.RestClient;

public class MaintainStudentActivity extends AbstractAsyncActivity {

    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView userNameTextView;
    private TextView emailTextView;

    private String operation;
    private Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintain_student);

        firstNameTextView = findViewById(R.id.firstname);
        lastNameTextView = findViewById(R.id.lastname);
        userNameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        operation = extras.getString("operation");

        if (operation.compareTo("EDIT")==0) {
            student = new Gson().fromJson(extras.getString("student"), Student.class);
            firstNameTextView.setText(student.getFirstName());
            lastNameTextView.setText(student.getLastName());
            userNameTextView.setText(student.getUser().getUserName());
            emailTextView.setText(student.getUser().getEmail());
        }
    }

    public void save(View view) {

        if (operation.compareTo("ADD")==0) {
            student = new Student();

            User user = new User();
            student.setUser(user);
        }

        student.setFirstName(firstNameTextView.getText().toString());
        student.setLastName(lastNameTextView.getText().toString());
        student.getUser().setEmail(emailTextView.getText().toString());
        student.getUser().setUserName(userNameTextView.getText().toString());

        new MaintainStudentTask(student, operation).execute();

    }

    public void cancel(View view) {
        Intent intent = new Intent(MaintainStudentActivity.this, ListStudentActivity.class);
        startActivity(intent);
    }

    private class MaintainStudentTask extends AsyncTask<Void, Void, String> {

        private Student student;
        private String operation;

        public MaintainStudentTask(Student student, String operation) {
            this.student = student;
            this.operation = operation;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient<Student> restClient = RestClient.getInstance();
            try {
                String op = "new";
                if (operation.compareTo("EDIT")==0) op = "edit";
                String ret = restClient.post("api/student/" + op ,  student);
                return "success";
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());

                if (operation.compareTo("EDIT")==0)
                    return "Error editing student";
                else
                    return "Error adding student";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success")) {
               MaintainStudentActivity.this.cancel(null);
            } else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(MaintainStudentActivity.this, result, duration).show();
        }
    }

}
