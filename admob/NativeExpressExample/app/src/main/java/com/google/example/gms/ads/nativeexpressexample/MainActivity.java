/*
 * Copyright (C) 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.example.gms.ads.nativeexpressexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple activity showing the use of a {@link NativeExpressAdView}.
 */
public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "EXAMPLE";
    private InterstitialAd mInterstitialAd;
    private long AdInterTs;
    private int counter;

    NativeExpressAdView mAdView;
    VideoController mVideoController;
    OkHttpClient httpClient = new OkHttpClient();

    private void requestNewInterstitial() {
        //Log.d("hahaha", AdRequest.DEVICE_ID_EMULATOR);
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void showInterAd() {
        long ts = System.currentTimeMillis();
        long elapsed = ts - this.AdInterTs;
        if (elapsed > 3 * 60 * 1000 || this.counter % 10 == 1){
            this.AdInterTs = ts;
        }
        else{
            Log.d("hahaha", "time too short " + elapsed/1000);
            return;
        }
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            Log.d("hahaha", "ad 111 ready");
        }
        else{
            Log.d("hahaha", "ad 222 not ready");
        }
    }

    private void refresh() {
        this.counter++;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Random r = new Random();
                    int index = r.nextInt(30);
                    final String a = get_news(index);
                    Log.d("hahaha", a);
                    final TextView textViewToChange = (TextView) findViewById(R.id.news);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("hahaha", "code: ");
                            textViewToChange.setText(a);
                        }
                    });

                } catch (IOException e) {
                    Log.d("hahaha", "Just a nap...");
                }
            }
        };
        Toast.makeText(this, "1+ news fetching!",
                Toast.LENGTH_LONG).show();
        thread.start();
        showInterAd();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.AdInterTs = System.currentTimeMillis();
        this.counter = 0;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8217481143192443/7924005419");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Log.d("hahaha", "ad closed");
            }
        });
        requestNewInterstitial();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Locate the NativeExpressAdView.
        mAdView = (NativeExpressAdView) findViewById(R.id.adView);

        // Set its video options.
        mAdView.setVideoOptions(new VideoOptions.Builder()
                .setStartMuted(true)
                .build());

        // The VideoController can be used to get lifecycle events and info about an ad's video
        // asset. One will always be returned by getVideoController, even if the ad has no video
        // asset.
        mVideoController = mAdView.getVideoController();
        mVideoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            @Override
            public void onVideoEnd() {
                Log.d(LOG_TAG, "Video playback is finished.");
                super.onVideoEnd();
            }
        });

        // Set an AdListener for the AdView, so the Activity can take action when an ad has finished
        // loading.
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mVideoController.hasVideoContent()) {
                    Log.d(LOG_TAG, "Received an ad that contains a video asset.");
                } else {
                    Log.d(LOG_TAG, "Received an ad that does not contain a video asset.");
                }
            }
        });

        mAdView.loadAd(new AdRequest.Builder().build());





        refresh();

        Button button = (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                refresh();
            }
        });


    }

    protected String get_news(int index) throws IOException{
        Request request = new Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/topstories.json")
                .build();
        Response response = httpClient.newCall(request).execute();
        String jsonData = response.body().string();
        Log.i("hahaha", "1" + jsonData);
        ArrayList<String> listdata = new ArrayList<String>();
        try {
            JSONArray jArray = new JSONArray(jsonData);
            Log.i("hahaha", "2.5" +jArray);
            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    listdata.add(jArray.getString(i));
                }
            }
        } catch (JSONException je) {
            Log.i("hahaha", "2" + je.getLocalizedMessage());
        }
        Log.i("hahaha", "3" + listdata);
        final String id = listdata.get(index);


        request = new Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/item/"+id+".json")
                .build();
        response = httpClient.newCall(request).execute();
        jsonData = response.body().string();
        Log.i("hahaha", "1" + jsonData);
        try {
            JSONObject jObject = new JSONObject(jsonData);
            Log.i("hahaha", "2.5" +jObject);
            if (jObject != null) {
                String a = jObject.getString("title");
                return a;
            }
        } catch (JSONException je) {
            Log.i("hahaha", "2" + je.getLocalizedMessage());

        }
        return "err...";
    }
}
