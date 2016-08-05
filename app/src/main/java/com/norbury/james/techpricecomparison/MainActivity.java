package com.norbury.james.techpricecomparison;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final String amazonBaseUrl =
            "https://www.amazon.co.uk/s/ref=nb_sb_noss_2?url=search-alias%3Daps&field-keywords=";
    private final String ebuyerBaseUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Search button clicked. Proceed to make search url and download page
     * @param view View passed in by activity on button click
     */
    public void searchButtonClicked(View view) {
        // Check if network connectivity is available
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Proceed with connection
            EditText editText = (EditText) findViewById(R.id.search_edit_text);
            String searchString = editText.getText().toString();
            String[] terms = searchString.split(" ");
            String amazonSearchUrl = buildSearchUrl(amazonBaseUrl, terms);
        } else {
            // Display error toast
            Toast.makeText(this, "No network connection.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Takes a string array containing search terms and returns the url
     * to make an HTTP reqest
     * @param baseUrl Containing the url up to the search query
     * @param terms Words to build query with
     * @return Complete URL with query
     */
    private String buildSearchUrl(String baseUrl, String[] terms) {
        StringBuilder builder = new StringBuilder(baseUrl);
        for (String term : terms) {
            builder.append(term).append("+");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params come from the execute() call: params[0] is the url
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid";
            }
        }

        private String downloadUrl(String myUrl) throws IOException {
            InputStream inputStream = null;
            try {
                // Setup HttpConnection settings
                URL url = new URL(myUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                // Starts the query
                connection.connect();
                // If response is 200 continue else throw
                int response = connection.getResponseCode();
                if (response == 200) {
                    inputStream = connection.getInputStream();
                } else {
                    throw new Exception("Bad response");
                }
                // Convert stream to string
                return readInputStream(inputStream);
            } catch (Exception e) {
                // TODO Suitable error handling for bad response code
                e.printStackTrace();
                return "Error downloading pages.";
            } finally {
                // Close streams if still open
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }

        private String readInputStream(InputStream stream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Update list adapter
        }
    }
}
