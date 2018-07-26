package com.mcit.admissionsystem.activities.coursestudent;

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
import com.google.gson.reflect.TypeToken;
import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.AbstractAsyncActivity;
import com.mcit.admissionsystem.entities.CS;
import com.mcit.admissionsystem.entities.Course;
import com.mcit.admissionsystem.entities.Student;
import com.mcit.admissionsystem.http.RestClient;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class AddStudentToCourseActivity extends AbstractAsyncActivity {


    private Course course;
    private List<Student> students;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_to_course);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        Type listType = new TypeToken<List<Student>>() {}.getType();
        course = new Gson().fromJson(extras.getString("course"), Course.class );
        students = new Gson().fromJson(extras.getString("students"),listType );

        new LoadStudentsTask(course.getId()).execute();
    }

    public void cancel(View view) {
        Intent intent = new Intent(AddStudentToCourseActivity.this, MaintainCourseStudentsActivity.class);
        intent.putExtra("course", new Gson().toJson(course));
        intent.putExtra("students", new Gson().toJson(students));
        startActivity(intent);
    }

    private class onClickAdd implements View.OnClickListener {

        private Student student;

        public onClickAdd(Student student) {
            this.student = student;
        }

        @Override
        public void onClick(View v) {

            CS cs = new CS();
            cs.setStudent(student);
            cs.setCourse(course);
            new AddStudentToCourseActivity.AddStudentToCourseTask(cs).execute();
        }
    }

    private class LoadStudentsTask extends AsyncTask<Void, Void, String> {

        private long id;

        public LoadStudentsTask(long id) {
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
                String ret = restClient.get("api/cs/all-students-not-in-course/" + id );
                return ret;
            } catch (Exception e) {
                return "Error Loading students not in course";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();

            if (!result.contains("Error")) {
                Log.w("DEBUG", result);

                List<Student> students_ = null;
                try {
                    students_ = Arrays.asList(new Gson().fromJson(result, Student[].class));

                    TableLayout tableLayout = (TableLayout) findViewById(R.id.studentsNotIncourseTable);
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

                    if (students_ != null) {

                        for (Student student : students_) {
                            TableRow tableRow = new TableRow(getApplicationContext());

                            TextView name = new TextView(getApplicationContext());
                            name.setText(student.getFirstName() + " " + student.getLastName());
                            tableRow.addView(name);
                            TextView email = new TextView(getApplicationContext());
                            email.setPadding(20,0,0,0);
                            email.setText(student.getUser().getEmail());
                            tableRow.addView(email);
                            tableLayout.addView(tableRow);
                            Button add = new Button(getApplicationContext());

                            add.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            add.setPadding(40,0,0,0);
                            add.setOnClickListener(new onClickAdd(student));
                            add.setBackgroundResource(R.drawable.ic_add);
                            tableRow.addView(add);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error converting students json to list of objects\n"
                            + e.getStackTrace());
                }

            } else
                Toast.makeText(AddStudentToCourseActivity.this, result, Toast.LENGTH_SHORT ).show();
        }
    }


    private class AddStudentToCourseTask extends AsyncTask<Void, Void, String> {

        private CS cs;

        public AddStudentToCourseTask(CS cs) {
            this.cs = cs;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient<CS> restClient = RestClient.getInstance();
            try {
                String ret = restClient.post("api/cs/new" ,  cs);
                students.add(cs.getStudent());
                return "success";
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());
                return "Error adding student to course";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success")) {
                AddStudentToCourseActivity.this.cancel(null);
            } else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(AddStudentToCourseActivity.this, result, duration).show();
        }
    }
}
