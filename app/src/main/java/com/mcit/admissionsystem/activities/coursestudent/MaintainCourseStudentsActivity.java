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
 import com.mcit.admissionsystem.activities.professor.MaintainProfessorActivity;
 import com.mcit.admissionsystem.activities.student.ListStudentActivity;
 import com.mcit.admissionsystem.activities.student.MaintainStudentActivity;
 import com.mcit.admissionsystem.entities.CS;
 import com.mcit.admissionsystem.entities.Course;
 import com.mcit.admissionsystem.entities.Professor;
 import com.mcit.admissionsystem.entities.Student;
 import com.mcit.admissionsystem.http.RestClient;

 import java.lang.reflect.Type;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

public class MaintainCourseStudentsActivity extends AbstractAsyncActivity {

     private TextView nameTextView;
     private TextView professorTextView;
     private TextView startDateTextView;
     private TextView endDateTextView;

     private SimpleDateFormat sdf;
     private android.text.format.DateFormat df;

     private Course course;
     private List<Student> students;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_maintain_course_students);

         sdf = new SimpleDateFormat("MM/dd/yyyy");
         df = new android.text.format.DateFormat();

         nameTextView = findViewById(R.id.cs_name);
         professorTextView = findViewById(R.id.cs_professor);
         startDateTextView = findViewById(R.id.cs_startdate);
         endDateTextView= findViewById(R.id.cs_enddate);

         Intent intent = getIntent();
         Bundle extras = intent.getExtras();

         Type listType = new TypeToken<List<Student>>() {}.getType();

         course = new Gson().fromJson(extras.getString("course"),Course.class );
         students = new Gson().fromJson(extras.getString("students"),listType );

         nameTextView.setText(course.getName());
         professorTextView.setText(
                 course.getProfessor().getFirstName() + " " +
                 course.getProfessor().getLastName()
         );
         startDateTextView.setText(df.format("MM/dd/yyy", course.getStartDate()));
         endDateTextView.setText(df.format("MM/dd/yyy", course.getEndDate()));

         new LoadStudentsTask().execute();
     }

    public void csCancel(View view) {
        Intent intent = new Intent(MaintainCourseStudentsActivity.this, ListCourseStudentsActivity.class);
        startActivity(intent);
    }

     public void addCSStudent(View view) {
         Intent intent = new Intent(MaintainCourseStudentsActivity.this, AddStudentToCourseActivity.class);
         intent.putExtra("course", new Gson().toJson(course));
         intent.putExtra("students", new Gson().toJson(students));
         startActivity(intent);
     }

    private class LoadStudentsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            TableLayout tableLayout = findViewById(R.id.csStudentsTable);
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

            if (students != null && students.size() > 1) {

                for (Student student : students) {

                    if (student != null && student.getUser() != null) {
                        TableRow tableRow = new TableRow(getApplicationContext());

                        TextView name = new TextView(getApplicationContext());
                        name.setText(student.getFirstName() + " " + student.getLastName());
                        tableRow.addView(name);

                        TextView email = new TextView(getApplicationContext());
                        email.setPadding(20, 0, 0, 0);
                        email.setText(student.getUser().getEmail());
                        tableRow.addView(email);

                        Button delete = new Button(getApplicationContext());
                        delete.setLayoutParams(new TableRow.LayoutParams(80, 100));
                        delete.setPadding(40, 0, 0, 0);
                        delete.setBackgroundResource(R.drawable.ic_delete_black_24dp);
                        delete.setOnClickListener(new onClickRemove(course.getId(), student.getId()));
                        tableRow.addView(delete);

                        tableLayout.addView(tableRow);


                    }
                }
            }

            dismissProgressDialog();
        }
    }

    private class RemoveStudentTask extends AsyncTask<Void, Void, String> {

        private long courseId;
        private long studentId;

        public RemoveStudentTask(long courseId, long studentId) {
            this.courseId = courseId;
            this.studentId = studentId;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.delete("api/cs", courseId, studentId );
                return ret;
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());
                return "Error removing student from course";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            List<Student> students_ = new ArrayList<>();
            if (result.contains("success")) {
                for (Student s : students)
                    if (s == null || s.getId() != studentId) students_.add(s);
                students = students_;
                new LoadStudentsTask().execute();
            } else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(MaintainCourseStudentsActivity.this, result, duration).show();
        }
    }


    private class onClickRemove implements View.OnClickListener {

        private long courseId;
        private long studentId;

        public onClickRemove(long courseId, long studentId) {
            this.courseId = courseId;
            this.studentId = studentId;
        }

        @Override
        public void onClick(View v) {
            new MaintainCourseStudentsActivity.RemoveStudentTask(courseId, studentId).execute();
        }
    }



 }
