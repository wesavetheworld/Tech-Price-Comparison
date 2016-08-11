package com.norbury.james.techpricecomparison;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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
    private final String ebuyerBaseUrl = "http://www.ebuyer.com/search?q=";
    // TODO Create custom ArrayAdapter
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Search button clicked. Proceed to make search url and download page
     *
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
            String ebuyerSearchUrl = buildSearchUrl(ebuyerBaseUrl, terms);
            new DownloadWebpageTask().execute(amazonSearchUrl, ebuyerSearchUrl);
        } else {
            // Display error toast
            Toast.makeText(this, "No network connection.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Takes a string array containing search terms and returns the url
     * to make an HTTP reqest
     *
     * @param baseUrl Containing the url up to the search query
     * @param terms   Words to build query with
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

    private class DownloadWebpageTask extends AsyncTask<String, Void, String[]> {
        /**
         * Called when DownloadWebpageTask.execute() is called
         *
         * @param urls Variable argument strings of URLs
         * @return String holding the downloaded page HTML
         */
        @Override
        protected String[] doInBackground(String... urls) {
            // params come from the execute() call: params[0] is the url
            String[] results = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                try {
                    results[i] = downloadUrl(urls[i]);
                } catch (IOException e) {
                    results[i] = "Unable to retrieve web page. URL may be invalid";
                }
            }
            return results;
        }

        /**
         * Make HTTP connection and get stream of information
         *
         * @param myUrl URL to connect to as string
         * @return Web page HTML as string
         * @throws IOException
         */
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

        /**
         * Converts input stream from HttpURLConnection to string
         *
         * @param stream Stream from connection to URL
         * @return String containing content of stream
         * @throws IOException Exception reading from stream and converting to string
         */
        private String readInputStream(InputStream stream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }

        /**
         * Executes after task finishes populating ListView with results
         *
         * @param results String holding web page
         */
        @Override
        protected void onPostExecute(String[] results) {
            // Create adapter with results
            adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, results);
            ListView listView = (ListView) findViewById(R.id.products_list_view);
            // Populate ListView
            listView.setAdapter(adapter);
        }
    }
}
