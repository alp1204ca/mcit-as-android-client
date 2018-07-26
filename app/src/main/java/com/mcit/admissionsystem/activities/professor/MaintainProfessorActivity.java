package com.mcit.admissionsystem.activities.professor;

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
import com.mcit.admissionsystem.entities.Professor;
import com.mcit.admissionsystem.entities.User;
import com.mcit.admissionsystem.http.RestClient;

public class MaintainProfessorActivity extends AbstractAsyncActivity {

    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView userNameTextView;
    private TextView emailTextView;

    private String operation;
    private Professor professor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintain_professor);

        firstNameTextView = findViewById(R.id.professor_firstname);
        lastNameTextView = findViewById(R.id.professor_lastname);
        userNameTextView = findViewById(R.id.professor_username);
        emailTextView = findViewById(R.id.professor_email);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        operation = extras.getString("operation");

        if (operation.compareTo("EDIT")==0) {
            professor = new Gson().fromJson(extras.getString("professor"), Professor.class);
            firstNameTextView.setText(professor.getFirstName());
            lastNameTextView.setText(professor.getLastName());
            userNameTextView.setText(professor.getUser().getUserName());
            emailTextView.setText(professor.getUser().getEmail());
        }
    }

    public void save(View view) {

        if (operation.compareTo("ADD")==0) {
            professor = new Professor();

            User user = new User();
            professor.setUser(user);
        }

        professor.setFirstName(firstNameTextView.getText().toString());
        professor.setLastName(lastNameTextView.getText().toString());
        professor.getUser().setEmail(emailTextView.getText().toString());
        professor.getUser().setUserName(userNameTextView.getText().toString());

        new MaintainProfessorTask(professor, operation).execute();

    }

    public void cancel(View view) {
        Intent intent = new Intent(MaintainProfessorActivity.this, ListProfessorActivity.class);
        startActivity(intent);
    }

    private class MaintainProfessorTask extends AsyncTask<Void, Void, String> {

        private Professor professor;
        private String operation;

        public MaintainProfessorTask(Professor professor, String operation) {
            this.professor = professor;
            this.operation = operation;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient<Professor> restClient = RestClient.getInstance();
            try {
                String op = "new";
                if (operation.compareTo("EDIT")==0) op = "edit";
                String ret = restClient.post("api/professor/" + op ,  professor);
                return "success";
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());

                if (operation.compareTo("EDIT")==0)
                    return "Error editing professor";
                else
                    return "Error adding professor";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success")) {
                MaintainProfessorActivity.this.cancel(null);
            } else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(MaintainProfessorActivity.this, result, duration).show();
        }
    }

}
