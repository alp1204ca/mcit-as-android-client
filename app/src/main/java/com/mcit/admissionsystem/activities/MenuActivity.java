package com.mcit.admissionsystem.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.course.ListCourseActivity;
import com.mcit.admissionsystem.activities.coursestudent.ListCourseStudentsActivity;
import com.mcit.admissionsystem.activities.professor.ListProfessorActivity;
import com.mcit.admissionsystem.activities.student.ListStudentActivity;
import com.mcit.admissionsystem.http.RestClient;

import org.springframework.web.client.HttpClientErrorException;

public class MenuActivity extends AbstractAsyncActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void maintainStudentClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ListStudentActivity.class);
        startActivity(intent);
    }

    public void maintainProfessorClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ListProfessorActivity.class);
        startActivity(intent);
    }

    public void maintainCourseClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ListCourseActivity.class);
        startActivity(intent);
    }

    public void assignStudentToCourseClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ListCourseStudentsActivity.class);
        startActivity(intent);
    }

    public void logoutClick(View view) {
        new LogoutTask().execute();
    }

    private class LogoutTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.post("logout", null);
                return "User successfully logged out";
            } catch (HttpClientErrorException e) {
                return "Error logging user out";
            } catch (Exception e) {
                return "Error - try again later";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();
            Toast.makeText(MenuActivity.this, result, Toast.LENGTH_SHORT ).show();

            if (result.contains("success"))
                RestClient.getInstance().cleanUpHeaders();

            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
