package com.mcit.admissionsystem.activities.course;

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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.AbstractAsyncActivity;
import com.mcit.admissionsystem.activities.MenuActivity;
import com.mcit.admissionsystem.activities.student.ListStudentActivity;
import com.mcit.admissionsystem.entities.Course;
import com.mcit.admissionsystem.http.RestClient;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListCourseActivity extends AbstractAsyncActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_course);

        new LoadCoursesTask().execute();
    }

    public void listCourseReturn(View view) {
        Intent intent = new Intent(ListCourseActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    public void addCourse(View v) {
        Intent intent = new Intent(ListCourseActivity.this, MaintainCourseActivity.class);
        intent.putExtra("operation", "ADD");
        startActivity(intent);
    }

    private class LoadCoursesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.get("api/course" );
                return ret;
            } catch (Exception e) {
                return "Error Loading courses";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();

            if (!result.contains("Error")) {
                Log.w("DEBUG", result);

                List<Course> courses = null;
                try {
                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapter(Date.class, new JsonDeserializer() {
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            //return new Date(json.getAsJsonPrimitive().getAsLong());

                            DateFormat format = new SimpleDateFormat("MMMM d, yyyy h:mm:ss a");
                            String date = json.getAsString();
                            try {
                                return format.parse(date);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    Gson gson = builder.create();

                    courses = Arrays.asList(gson.fromJson(result, Course[].class));

                    TableLayout tableLayout = (TableLayout) findViewById(R.id.coursesTable);
                    tableLayout.removeAllViews();

                    TableRow tableHeader = new TableRow(getApplicationContext());
                    tableHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    TextView courseNameHeader = new TextView(getApplicationContext());
                    courseNameHeader.setTextColor(Color.WHITE);
                    courseNameHeader.setText("Name");
                    tableHeader.addView(courseNameHeader);
                    TextView professorHeader = new TextView(getApplicationContext());
                    professorHeader.setPadding(20,0,0,0);
                    professorHeader.setTextColor(Color.WHITE);
                    professorHeader.setText("Professor");
                    tableHeader.addView(professorHeader);

                    tableLayout.addView(tableHeader);

                    if (courses != null) {

                        for (Course course : courses) {
                            TableRow tableRow = new TableRow(getApplicationContext());

                            TextView name = new TextView(getApplicationContext());
                            name.setText(course.getName());
                            tableRow.addView(name);
                            TextView professor = new TextView(getApplicationContext());
                            professor.setPadding(20,0,0,0);
                            if (course.getProfessor()!=null) {
                                professor.setText(course.getProfessor().getFirstName() + " " + course.getProfessor().getLastName());
                            }
                            tableRow.addView(professor);
                            tableLayout.addView(tableRow);
                            Button edit = new Button(getApplicationContext());
                            edit.setBackgroundResource(R.drawable.ic_mode_edit_black_24dp);
                            edit.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            edit.setPadding(40,0,0,0);
                            edit.setOnClickListener(new onClickEdit(course));
                            tableRow.addView(edit);
                            Button delete = new Button(getApplicationContext());
                            delete.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            delete.setPadding(40,0,0,0);
                            delete.setBackgroundResource(R.drawable.ic_delete_black_24dp);
                            delete.setOnClickListener(new onClickDelete(course.getId()));
                            tableRow.addView(delete);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error converting courses json to list of objects\n"
                            + e.getStackTrace());
                }

            } else
                Toast.makeText(ListCourseActivity.this, result, Toast.LENGTH_SHORT ).show();
        }
    }

    private class DeletecourseTask extends AsyncTask<Void, Void, String> {

        private long id;

        public DeletecourseTask(long id) {
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
                String ret = restClient.delete("api/course", id );
                return ret;
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());
                return "Error deleting course";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success"))
                new LoadCoursesTask().execute();
            else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(ListCourseActivity.this, result, duration).show();
        }
    }

    private class onClickEdit implements View.OnClickListener {

        private Course course;

        public onClickEdit(Course course) {
            this.course = course;
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(ListCourseActivity.this, MaintainCourseActivity.class);
            intent.putExtra("course", new Gson().toJson(course));
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
            new DeletecourseTask(id).execute();
        }
    }
}
