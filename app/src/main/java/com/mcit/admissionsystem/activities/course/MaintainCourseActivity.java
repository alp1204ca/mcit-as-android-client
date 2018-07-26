package com.mcit.admissionsystem.activities.course;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.activities.AbstractAsyncActivity;
import com.mcit.admissionsystem.entities.Course;
import com.mcit.admissionsystem.entities.Professor;
import com.mcit.admissionsystem.http.RestClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MaintainCourseActivity extends AbstractAsyncActivity {

    private TextView nameTextView;
    private Spinner professorSpinner;
    private EditText startDatePicker;
    private EditText endDatePicker;

    private String operation;
    private Course course;

    private SimpleDateFormat sdf;
    private android.text.format.DateFormat df;

    private List<Professor> professors = new ArrayList<>();
    ArrayAdapter<Professor> professorAdapter;
    private Professor selectedProfessor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintain_course);

        sdf = new SimpleDateFormat("MM/dd/yyyy");
        df = new android.text.format.DateFormat();

        nameTextView = findViewById(R.id.name);
        professorSpinner = findViewById(R.id.professor);
        startDatePicker = findViewById(R.id.startdate);
        endDatePicker = findViewById(R.id.enddate);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        operation = extras.getString("operation");

        if (operation.compareTo("EDIT")==0) {
            course = new Gson().fromJson(extras.getString("course"), Course.class);
            nameTextView.setText(course.getName());
            selectedProfessor = course.getProfessor();
            startDatePicker.setText(df.format("MM/dd/yyy", course.getStartDate()));
            endDatePicker.setText(df.format("MM/dd/yyy", course.getEndDate()));
        }

        professorAdapter = new ArrayAdapter<>(MaintainCourseActivity.this, android.R.layout.simple_spinner_dropdown_item, professors);
        professorAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        professorSpinner.setAdapter(professorAdapter);

        new LoadProfessorTask().execute();

    }

    public void save(View view) {

        if (operation.compareTo("ADD")==0) {
            course = new Course();

            Professor professor = new Professor();
            course.setProfessor(professor);
        }

        course.setName(nameTextView.getText().toString());
        course.setProfessor((Professor) professorSpinner.getSelectedItem());
        try {
            course.setStartDate(sdf.parse(startDatePicker.getText().toString()));
        } catch (ParseException pe) {

        }

        try {
            course.setEndDate(sdf.parse(endDatePicker.getText().toString()));
        } catch (ParseException pe) {

        }

        new MaintainCourseTask(course, operation).execute();

    }

    public void cancel(View view) {
        Intent intent = new Intent(MaintainCourseActivity.this, ListCourseActivity.class);
        startActivity(intent);
    }

    private class LoadProfessorTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient<Course> restClient = RestClient.getInstance();
            try {
                String ret = restClient.get("api/professor/");
                professors =  Arrays.asList(new Gson().fromJson(ret, Professor[].class));

                if(selectedProfessor != null)
                    for(int i=0; i < professors.size(); i++)
                        if (professors.get(i).getId() == selectedProfessor.getId())
                            try {
                                professorSpinner.setSelection(i);
                            } catch (Exception e) {
                                //ignore it for now.
                            }

                try {
                    professorAdapter.addAll(professors);
                } catch (Exception e) {
                    //ignore for now
                }

                return "success";
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());
                return "Error listing professor";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (!result.contains("success"))
                Toast.makeText(MaintainCourseActivity.this, result, duration).show();
        }
    }

    private class MaintainCourseTask extends AsyncTask<Void, Void, String> {

        private Course course;
        private String operation;

        public MaintainCourseTask(Course course, String operation) {
            this.course = course;
            this.operation = operation;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient<Course> restClient = RestClient.getInstance();
            try {
                String op = "new";
                if (operation.compareTo("EDIT")==0) op = "edit";
                String ret = restClient.post("api/course/" + op ,  course);
                return "success";
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());

                if (operation.compareTo("EDIT")==0)
                    return "Error editing course";
                else
                    return "Error adding course";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success")) {
                MaintainCourseActivity.this.cancel(null);
            } else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(MaintainCourseActivity.this, result, duration).show();
        }
    }

}
