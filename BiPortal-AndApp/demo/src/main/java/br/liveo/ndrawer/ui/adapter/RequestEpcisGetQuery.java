package br.liveo.ndrawer.ui.adapter;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class RequestEpcisGetQuery extends AsyncTask<String, Void, String>{
    static OkHttpClient client = new OkHttpClient();
    static String url = "http://125.131.73.191:8080/epcis/Poll/SimpleEventQuery";
    public static final MediaType mediaType = MediaType.parse("application/xml; charset=utf-8");

    @Override
    protected String doInBackground(String... params) {

        try {
            String url = params[0];
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i("result", result);
        return ;
    }
}