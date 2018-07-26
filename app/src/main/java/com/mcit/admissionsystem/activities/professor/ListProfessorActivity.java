package com.mcit.admissionsystem.activities.professor;

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
import com.mcit.admissionsystem.activities.coursestudent.ListCourseStudentsActivity;
import com.mcit.admissionsystem.entities.Professor;
import com.mcit.admissionsystem.http.RestClient;

import java.util.Arrays;
import java.util.List;

public class ListProfessorActivity extends AbstractAsyncActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_professor);

        new LoadProfessorsTask().execute();
    }

    public void listProfessorReturn(View view) {
        Intent intent = new Intent(ListProfessorActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    public void addProfessor(View v) {
        Intent intent = new Intent(ListProfessorActivity.this, MaintainProfessorActivity.class);
        intent.putExtra("operation", "ADD");
        startActivity(intent);
    }

    private class LoadProfessorsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.get("api/professor" );
                return ret;
            } catch (Exception e) {
                return "Error Loading professors";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();

            if (!result.contains("Error")) {
                Log.w("DEBUG", result);

                List<Professor> professors = null;
                try {
                    professors = Arrays.asList(new Gson().fromJson(result, Professor[].class));


                    TableLayout tableLayout = (TableLayout) findViewById(R.id.professorsTable);
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

                    if (professors != null) {

                        for (Professor professor : professors) {
                            TableRow tableRow = new TableRow(getApplicationContext());

                            TextView name = new TextView(getApplicationContext());
                            name.setText(professor.getFirstName() + " " + professor.getLastName());
                            tableRow.addView(name);
                            TextView email = new TextView(getApplicationContext());
                            email.setPadding(20,0,0,0);
                            email.setText(professor.getUser().getEmail());
                            tableRow.addView(email);
                            tableLayout.addView(tableRow);
                            Button edit = new Button(getApplicationContext());
                            edit.setBackgroundResource(R.drawable.ic_mode_edit_black_24dp);
                            edit.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            edit.setPadding(40,0,0,0);
                            edit.setOnClickListener(new onClickEdit(professor));
                            tableRow.addView(edit);
                            Button delete = new Button(getApplicationContext());
                            delete.setLayoutParams(new TableRow.LayoutParams(80, 100));
                            delete.setPadding(40,0,0,0);
                            delete.setBackgroundResource(R.drawable.ic_delete_black_24dp);
                            delete.setOnClickListener(new onClickDelete(professor.getId()));
                            tableRow.addView(delete);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error converting professors json to list of objects\n"
                            + e.getStackTrace());
                }

            } else
                Toast.makeText(ListProfessorActivity.this, result, Toast.LENGTH_SHORT ).show();
        }
    }

    private class DeleteProfessorTask extends AsyncTask<Void, Void, String> {

        private long id;

        public DeleteProfessorTask(long id) {
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
                String ret = restClient.delete("api/professor", id );
                return ret;
            } catch (Exception e) {
                for(StackTraceElement ee : e.getStackTrace())
                    Log.e("Error", ee.toString());
                return "Error deleting professor";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int duration = Toast.LENGTH_SHORT;
            dismissProgressDialog();
            if (result.contains("success"))
                new LoadProfessorsTask().execute();
            else
                duration = Toast.LENGTH_LONG;
            Toast.makeText(ListProfessorActivity.this, result, duration).show();
        }
    }

    private class onClickEdit implements View.OnClickListener {

        private Professor professor;

        public onClickEdit(Professor professor) {
            this.professor = professor;
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(ListProfessorActivity.this, MaintainProfessorActivity.class);
            intent.putExtra("professor", new Gson().toJson(professor));
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
            new DeleteProfessorTask(id).execute();
        }
    }
}
