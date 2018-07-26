package com.mcit.admissionsystem.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mcit.admissionsystem.R;
import com.mcit.admissionsystem.http.RestClient;

import org.springframework.web.client.HttpClientErrorException;

public class LoginActivity extends AbstractAsyncActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void click(View view) {
        LoginTask lt = new LoginTask();
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        lt.setUserName(username.getText().toString());
        lt.setPassword(password.getText().toString());
        lt.execute();
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {

        private String userName;
        private String password;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            RestClient restClient = RestClient.getInstance();
            try {
                String ret = restClient.postAndSetSession("login?username=" +userName+ "&password=" + password, null);
                return "User successfully logged in";
            } catch (HttpClientErrorException e) {
                return "Invalid username / password";
            } catch (Exception e) {
                return "Error - try again later";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();
            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT ).show();

            if (result.contains("success")) {
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        }
    }
}
