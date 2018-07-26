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
import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.AbstractAsyncActivity;
import com.mcit.admissionsystem.activities.MenuActivity;
import com.mcit.admissionsystem.entities.CS;
import com.mcit.admissionsystem.entities.Course;
import com.mcit.admissionsystem.entities.Student;
import com.mcit.admissionsystem.http.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCourseStudentsActivity extends AbstractAsyncActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_student_to_course);

        new LoadStudentCourseTask().execute();
    }

    public void csListReturn(View view) {
        Intent intent = new Intent(ListCourseStudentsActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    private class LoadStudentCourseTask extends AsyncTask<Void, Void, String> {

        private Map<Long, Course> courseMap;

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.get("api/cs" );
                return ret;
            } catch (Exception e) {
                return "Error Loading Course / Students";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();

            if (!result.contains("Error")) {
                Log.w("DEBUG", result);

                List<CS> csList = null;

                try {
                    csList = Arrays.asList(new Gson().fromJson(result, CS[].class));
                    Map<Long, List<Student>> csMap = new HashMap<>();
                    courseMap = new HashMap<>();

                    for (CS cs : csList) {
                        Long key = cs.getCourse().getId();
                        Student student = cs.getStudent();

                        if (csMap.containsKey(key)) {
                            List<Student> studentList = csMap.get(key);
                            studentList.add(student);
                            //csMap.get(key).add(student);
                        } else {
                            List<Student> students = new ArrayList<>();
                            students.add(student);
                            csMap.put(key, students);
                            courseMap.put(key, cs.getCourse());
                        }

                    }

                    TableLayout tableLayout = findViewById(R.id.studentcourseTable);
                    tableLayout.removeAllViews();

                    TableRow tableHeader = new TableRow(getApplicationContext());
                    tableHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    TextView firstNameHeader = new TextView(getApplicationContext());
                    firstNameHeader.setTextColor(Color.WHITE);
                    firstNameHeader.setText("Name");
                    tableHeader.addView(firstNameHeader);
                    TextView numberOfStudentsHeader = new TextView(getApplicationContext());
                    numberOfStudentsHeader.setPadding(20,0,0,0);
                    numberOfStudentsHeader.setTextColor(Color.WHITE);
                    numberOfStudentsHeader.setText("# of Students");
                    tableHeader.addView(numberOfStudentsHeader);

                    tableLayout.addView(tableHeader);

                    if (csMap != null && csMap.size() > 0) {

                        for (Map.Entry<Long, List<Student>> csEntry : csMap.entrySet()) {

                            TableRow tableRow = new TableRow(getApplicationContext());

                            TextView name = new TextView(getApplicationContext());

                            name.setText(courseMap.get(csEntry.getKey()).getName());
                            tableRow.addView(name);
                            TextView numberOfStudents = new TextView(getApplicationContext());
                            numberOfStudents.setPadding(20,0,0,0);
                            numberOfStudents.setText(String.valueOf(csEntry.getValue().size()-1));
                            tableRow.addView(numberOfStudents);
                            tableLayout.addView(tableRow);

                            Button manage = new Button(getApplicationContext());
                            manage.setBackgroundResource(R.drawable.ic_mode_edit_black_24dp);
                            manage.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            manage.setPadding(40,0,0,0);
                            Map<Course, List<Student>> courseListMap = new HashMap<>();
                            courseListMap.put(courseMap.get(csEntry.getKey()), csEntry.getValue());
                            manage.setOnClickListener(new ListCourseStudentsActivity.onClickManage(
                                    courseListMap.entrySet().iterator().next()
                                    ));
                            tableRow.addView(manage);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error converting student / course json to list of objects\n"
                            + e.getStackTrace());
                }

            } else
                Toast.makeText(ListCourseStudentsActivity.this, result, Toast.LENGTH_SHORT ).show();
        }
    }

    private class onClickManage implements View.OnClickListener {

        private Map.Entry<Course, List<Student>> courseListEntry;

        public onClickManage(Map.Entry<Course, List<Student>> courseListEntry) {
            this.courseListEntry = courseListEntry;
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(ListCourseStudentsActivity.this, MaintainCourseStudentsActivity.class);
            intent.putExtra("course", new Gson().toJson(courseListEntry.getKey()));
            intent.putExtra("students", new Gson().toJson(courseListEntry.getValue()));
            startActivity(intent);

        }
    }
}
