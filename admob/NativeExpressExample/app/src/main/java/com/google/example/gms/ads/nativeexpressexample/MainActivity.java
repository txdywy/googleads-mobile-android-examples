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

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Build;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;



import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.button;


/**
 * A simple activity showing the use of a .
 */
public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "EXAMPLE";
    private static int index = 0;
    private static String[] btText= {"HACKING CPU", "HACKING DEVICE", "HACKING WIFI", "HACKING IP"};
    private static int[] cs = {Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.RED};
    private InterstitialAd mInterstitialAd;
    private long AdInterTs;
    private int counter;
    private int batLevel;
    private CPUManager cpuManager = new CPUManager(1);

    private AdView mAdView;

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            batLevel = intent.getIntExtra("level", 0);
            //	level加%就是当前电量了
        }
    };


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

    private void refresh(final int index) {
        this.counter++;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    final String cpuRate = "CPU busy rate: " + readCPUusage() * 100 + "%\n";
                    final String memRate = "Free Memory: " + getFreeMemorySize() + "\n";
                    final String[] versions = getVersion();
                    final String[] cpuInfos = getCpuInfo();
                    final long[] romMems = getRomMemroy();
                    final long iMem = getTotalInternalMemorySize();
                    long[] sdCard = getSDCardMemory();
                    String[] wifiInfo = getOtherInfo();
                    String startTime = getTimes();


                    Log.d("hahaha", "Versions: " + Arrays.toString(versions));
                    Log.d("hahaha", "CPUs: " + Arrays.toString(cpuInfos));
                    Log.d("hahaha", "ROMs: " + Arrays.toString(romMems));
                    Log.d("hahaha", "iMem: " + iMem);
                    Log.d("hahaha", "sdCard: " + Arrays.toString(sdCard));
                    Log.d("hahaha", "battery: " + batLevel + "%");
                    Log.d("hahaha", "wifi: " + Arrays.toString(wifiInfo));
                    Log.d("hahaha", "boot: " + startTime);
                    Log.d("hahaha", "cpu cores: " + cpuManager.getMaxCpuFreq() + "Ghz, "
                                                  + cpuManager.getMinCpuFreq() + "Ghz, "
                                                  + cpuManager.getCurCpuFreq() + "Ghz, "
                                                  + cpuManager.getCpuName() + ", "
                                                  + cpuManager.getNumCores() + " cores,"
                                                  + cpuManager.getUsage() + "%, "
                    );
                    Log.d("hahaha", "SD new: " + getSDAvailableSize() + "/" + getSDTotalSize());
                    Log.d("hahaha", "ROM new: " + getRomAvailableSize() + "/" + getRomTotalSize());
                    //Log.d("hahaha", "mac new: " + getMacAddress());


                    String tmp = "";
                    if (index == 0) {
                        tmp = getIpInfo();
                    }
                    else if (index == 1){
                        tmp = "CPU : " + cpuManager.getCpuName() + "\n" +
                              "CPU Cores: " + cpuManager.getNumCores() + "\n" +
                                cpuRate +
                              "Current CPU Frequency: " + cpuManager.getCurCpuFreq() + "GHz\n" +
                              "Max CPU Frequency: "+ cpuManager.getMaxCpuFreq() + "GHz\n" +
                              "Min CPU Frequency: "+ cpuManager.getMinCpuFreq() + "GHz\n";
                    }
                    else if (index == 2){
                        tmp = "Android Version: " + versions[1] + "\n" +
                                "Device Type: " + versions[2] + "\n" +
                                "Memory: " + getFreeMemorySize() + "\n" +
                                "Storage: " + getRomAvailableSize() + "/" + getRomTotalSize() + "\n" +
                                "Battery: " + batLevel + "%\n" +
                                "Running time: " + startTime;


                    }
                    else if (index == 3){
                        tmp = "WIFI IP: " + wifiInfo[3] + "\n" +
                                "WIFI NAME: " + wifiInfo[2] + "\n" +
                                "MAC Address: " + wifiInfo[0] ;
                    }
                    else{
                        tmp = cpuRate + memRate + getIpInfo();
                    }
                    final String textResult = tmp;




                    //getTotalMemory();

                    //Log.d("hahaha", a);
                    final TextView textViewToChange = (TextView) findViewById(R.id.news);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("hahaha", "code: ");
                            textViewToChange.setText(textResult);
                            Log.d("hahaha", cpuRate);
                        }
                    });

                } catch (IOException e) {
                    Log.d("hahaha", "Just a nap...");
                }
            }
        };
        Toast.makeText(this, "Hacking! Scroll up for more!",
                Toast.LENGTH_LONG).show();
        thread.start();
        showInterAd();
    }


    private String getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    private String getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(MainActivity.this, blockSize * availableBlocks);
    }

    private String getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(MainActivity.this, blockSize * availableBlocks);
    }

    private String getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
    }

    public String[] getOtherInfo(){
        String[] other={"null","null","null","null","null","null","null"};
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo.getMacAddress()!=null){
            other[0]=wifiInfo.getMacAddress();
        } else {
            other[0] = "Fail";
        }
        other[1] = getTimes();
        other[2] = wifiInfo.getSSID();
        other[3] = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        other[4] = wifiInfo.getBSSID();
        //other[5] = wifiInfo.getFrequency() + "Hz";
        other[5] = wifiInfo.getLinkSpeed() + "";
        other[6] = wifiInfo.getNetworkId() +"";
        return other;
    }

    private String getTimes() {
        long ut = SystemClock.elapsedRealtime() / 1000;
        if (ut == 0) {
            ut = 1;
        }
        int m = (int) ((ut / 60) % 60);
        int h = (int) ((ut / 3600));
        return h + "h:" + m + "m";
    }


    public long[] getSDCardMemory() {
        long[] sdCardInfo=new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();
            long availBlocks = sf.getAvailableBlocks();

            sdCardInfo[0] = bSize * bCount;//总大小
            sdCardInfo[1] = bSize * availBlocks;//可用大小
        }
        return sdCardInfo;
    }


    private long[] getRomMemroy() {
        long[] romInfo = new long[2];
        //Total rom memory
        romInfo[0] = getTotalInternalMemorySize();

        //Available rom memory
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        romInfo[1] = blockSize * availableBlocks;
        getVersion();
        return romInfo;
    }

    private long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }


    private String[] getCpuInfo() {
        String str1 = "/proc/cpuinfo";
        String str2="";
        String[] cpuInfo={"",""};
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return cpuInfo;
    }

    private float readCPUusage() {

        try {

            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");

            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);

            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])

                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {

                Thread.sleep(360);

            } catch (Exception e) {

            }


            reader.seek(0);

            load = reader.readLine();

            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);

            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) + Long.parseLong(toks[6])

                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {

            ex.printStackTrace();

        }

        return 0;
    }

    private String[] getVersion(){
        String[] version={"null","null","null","null"};
        String str1 = "/proc/version";
        String str2;
        String[] arrayOfString;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            version[0]=arrayOfString[2];//KernelVersion
            localBufferedReader.close();
        } catch (IOException e) {
        }
        version[1] = Build.VERSION.RELEASE;// firmware version
        version[2]=Build.MODEL;//model
        version[3]=Build.DISPLAY;//system version
        return version;
    }

    private String getFreeMemorySize() {
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(outInfo);
        long avaliMem = outInfo.availMem;
        long totalMem = outInfo.totalMem;

        return (avaliMem / 1024 / 1024) + "MB/" + (totalMem / 1024 / 1024) + "MB";
    }

    private void getTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2="";
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            while ((str2 = localBufferedReader.readLine()) != null) {
                Log.d("hahaha", "---" + str2);
            }
        } catch (IOException e) {
        }
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

        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        final TextView textViewToChange = (TextView) findViewById(R.id.news);
        textViewToChange.setMovementMethod(new ScrollingMovementMethod());


        refresh(index);

        final Button button = (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                int rnd = new Random().nextInt(cs.length);
                index = (index + 1) % btText.length;
                refresh(index);
                button.setText(btText[index]);
                button.setBackgroundColor(cs[rnd]);
            }
        });


    }

    protected String getIpInfo() throws IOException{
        Request request = new Request.Builder()
                .url("http://ip-api.com/json")
                .build();
        Response response = httpClient.newCall(request).execute();
        String jsonData = response.body().string();
        Log.i("hahaha", "1" + jsonData);
        try {
            JSONObject jObject = new JSONObject(jsonData);
            Log.i("hahaha", "2.5" +jObject);
            String result = "";
            if (jObject != null) {

                result += "Your IP: " + jObject.getString("query") + "\n";
                result += "City: " + jObject.getString("city") + "\n";
                result += "Region: " + jObject.getString("regionName") + "\n";
                result += "Region Code: " + jObject.getString("region") + "\n";
                result += "Country: " + jObject.getString("country") + "\n";
                result += "ISP: " + jObject.getString("isp") + "\n";
                result += "Lat: " + jObject.getString("lat") + "\n";
                result += "Lon: " + jObject.getString("lon") + "\n";
                result += "Organization: " + jObject.getString("org") + "\n";
                result += "Timezone: " + jObject.getString("timezone") + "\n";
                result += "Zip: " + jObject.getString("zip") + "\n";
                return result;
            }
        } catch (JSONException je) {
            Log.i("hahaha", "2" + je.getLocalizedMessage());

        }
        return "err...";
    }
}
