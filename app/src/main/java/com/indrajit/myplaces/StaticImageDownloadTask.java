package com.indrajit.myplaces;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class StaticImageDownloadTask extends android.os.AsyncTask<Void,Void,Bitmap>{

    private AsyncResponse response;
    private URL url;

    interface AsyncResponse {
        void onTaskEnd(Bitmap bitmap);
    }

    StaticImageDownloadTask(AsyncResponse response, URL url) {
        this.response = response;
        this.url = url;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {

        try {

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();

            return BitmapFactory.decodeStream(inputStream);

        } catch (Exception ignored) {

        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        response.onTaskEnd(bitmap);
    }
}
