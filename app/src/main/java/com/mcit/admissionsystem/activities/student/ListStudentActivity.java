package com.mcit.admissionsystem.activities.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.AbstractAsyncActivity;
import com.mcit.admissionsystem.activities.MenuActivity;
import com.mcit.admissionsystem.activities.professor.ListProfessorActivity;
import com.mcit.admissionsystem.entities.Student;
import com.mcit.admissionsystem.http.RestClient;

import java.util.Arrays;
import java.util.List;

public class ListStudentActivity extends AbstractAsyncActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_student);

        new LoadStudentsTask().execute();
    }

    public void listStudentReturn(View view) {
        Intent intent = new Intent(ListStudentActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    public void addStudent(View v) {
        Intent intent = new Intent(ListStudentActivity.this, MaintainStudentActivity.class);
        intent.putExtra("operation", "ADD");
        startActivity(intent);
    }

    private class LoadStudentsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.get("api/student" );
                return ret;
            } catch (Exception e) {
                return "Error Loading students";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();

            if (!result.contains("Error")) {
                Log.w("DEBUG", result);

                List<Student> students = null;
                try {
                    students = Arrays.asList(new Gson().fromJson(result, Student[].class));


                    TableLayout tableLayout = (TableLayout) findViewById(R.id.studentsTable);
                    tableLayout.removeAllViews();

                    TableRow tableHeader = new TableRow(getApplicationContext());
                    tableHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    TextView firstNameHeader = new TextView(getApplicationContext());
                    firstNameHeader.setTextColor(Color.WHITE);
                    firstNameHeader.setText("Name");
                    tableHeader.addView(firstNameHeader);
                    TextView emailHeader = new TextView(getApplicationContext());
                    emailHeader.setPadding(20,0,0,0);
                    emailHeader.setTextColor(Color.WHITE);
                    emailHeader.setText("E-Mail");
                    tableHeader.addView(emailHeader);

                    tableLayout.addView(tableHeader);

                    if (students != null) {

                        for (Student student : students) {
                            TableRow tableRow = new TableRow(getApplicationContext());

                            TextView name = new TextView(getApplicationContext());
                            name.setText(student.getFirstName() + " " + student.getLastName());
                            tableRow.addView(name);
                            TextView email = new TextView(getApplicationContext());
                            email.setPadding(20,0,0,0);
                            email.setText(student.getUser().getEmail());
                            tableRow.addView(email);
                            tableLayout.addView(tableRow);
                            Button edit = new Button(getApplicationContext());
                            edit.setBackgroundResource(R.drawable.ic_mode_edit_black_24dp);
                            edit.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            edit.setPadding(40,0,0,0);
                            edit.setOnClickListener(new onClickEdit(student));
                            tableRow.addView(edit);
                            Button delete = new Button(getApplicationContext());
                            delete.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            delete.setPadding(40,0,0,0);
                            delete.setBackgroundResource(R.drawable.ic_delete_black_24dp);
                            delete.setOnClickListener(new onClickDelete(student.getId()));
                            tableRow.addView(delete);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error converting students json to list of objects\n"
                            + e.getStackTrace());
                }

            } else
                Toast.makeText(ListStudentActivity.this, result, Toast.LENGTH_SHORT ).show();
        }
    }

    private class DeleteStudentTask extends AsyncTask<Void, Void, String> {

        private long id;

        public DeleteStudentTask(long id) {
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.delete("api/student", id );
                return ret;
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());
                return "Error deleting student";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success"))
                new LoadStudentsTask().execute();
            else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(ListStudentActivity.this, result, duration).show();
        }
    }

    private class onClickEdit implements View.OnClickListener {

        private Student student;

        public onClickEdit(Student student) {
            this.student = student;
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(ListStudentActivity.this, MaintainStudentActivity.class);
            intent.putExtra("student", new Gson().toJson(student));
            intent.putExtra("operation", "EDIT");
            startActivity(intent);

        }
    }

    private class onClickDelete implements View.OnClickListener {

        private long id;

        public onClickDelete(long id) {
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            new DeleteStudentTask(id).execute();
        }
    }
}
