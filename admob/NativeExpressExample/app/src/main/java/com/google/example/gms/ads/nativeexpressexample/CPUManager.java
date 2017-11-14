package com.google.example.gms.ads.nativeexpressexample;

/**
 * Created by yiwei on 11/10/2017.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import android.util.Log;

public class CPUManager {

    // CPU index
    public int CPU_index = 0;
    // sampling time in calculate usage
    public int sample_fre = 10;

    public CPUManager(int index) {
        this.CPU_index = index;
    }

    /**
     * Wether the CPU is online
     * @return  true if it is online
     */
    public boolean isOnline() {
        BufferedReader reader;
        String result = "";
        ProcessBuilder cmd;
        InputStream in = null;
        try {
            String[] args = { "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu" + CPU_index + "/online" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            in = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String temp = "";
            while ((result = reader.readLine()) != null) {
                temp = temp + result;
            }
            if (temp.equals("1")) {
                return true;
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * get the cpu's max frequency, return cpu0 max frequency by defalut
     * "/system/bin/cat"
     * "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
     */

    public int getMaxCpuFreq() {
        String result = "";
        String[] args = {
                "/system/bin/cat",
                "/sys/devices/system/cpu/cpu" + CPU_index
                        + "/cpufreq/cpuinfo_max_freq" };
        if (!isOnline()) {
            args[1] = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
        }
        ProcessBuilder cmd;
        try {

            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "0";
        }

        int max_fre = fixInt(result.trim()) / 1024;
        return max_fre;
    }

    int fixInt(String myString) {
        return myString.isEmpty()?0:Integer.parseInt(myString);
    }

    /**
     *  get the cpu's min frequency,return cpu0 min frequency by defalut
     */
    public int getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        String[] args = {
                "/system/bin/cat",
                "/sys/devices/system/cpu/cpu" + CPU_index
                        + "/cpufreq/cpuinfo_min_freq" };
        if (!isOnline()) {
            args[1] = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
        }
        try {
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "-1";
        }
        int min_fre = fixInt(result.trim()) / 1024;
        return min_fre;
    }

    /**
     * get the cpu's current frequency
     */
    public int getCurCpuFreq() {
        String result = "";
        int cur_fre = 0;
        if (!isOnline()) {
            result = "0";
            return 0;
        }

        try {
            FileReader fr = new FileReader("/sys/devices/system/cpu/cpu"
                    + CPU_index + "/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        cur_fre = Integer.parseInt(result) / 1024;
        return cur_fre;
    }

    /**
     * get the cpu name
     */
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get the device's cpu num
     */
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            Log.e("cxq", "CPU Count: Failed.");
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * get the cpu usage using specified samplling time
     */
    public double getUsage() {
        double usage = 0;
        long total_start, total_end;
        long idel_start, idel_end;

        try {

            Map<String, Long> start_data = getCPUData();
            idel_start = start_data.get("idle");
            total_start = start_data.get("total_time");

            Thread.sleep(sample_fre);

            Map<String, Long> end_data = getCPUData();
            idel_end = end_data.get("idle");
            total_end = end_data.get("total_time");

            double idel = idel_end - idel_start;
            double total = total_end - total_start;
            if (total == 0) {
                return 0;
            }
            usage = (total - idel) / total;
            usage = Math.abs(usage * 100);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DecimalFormat df = new java.text.DecimalFormat("#.00");
        usage = Double.parseDouble(df.format(usage).replaceAll(",","."));
        return usage;
    }

    /**
     * take a data snapshot avoid reading the old data, especially using for usage calculation
     * @return Map it contains two values,("total_time",total_time) and ("idle",idle);
     */
    public Map<String, Long> getCPUData() {
        Map<String, Long> data = new HashMap<String, Long>();
        ArrayList<Long> list = new ArrayList<Long>();
        ArrayList<Long> list_defalut = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            list_defalut.add(0L);
        }
        String cpuCompare = "cpu" + CPU_index;
        if (CPU_index == -1) {
            cpuCompare = "cpu";
        }
        String load = "";
        String[] temp = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);

            while ((load = reader.readLine()) != null) {
                if (load.contains(cpuCompare)) {
                    break;
                } else {
                    load = "";
                }
            }
            reader.close();
            if (load == null || load.equals("")) {
                data.put("total_time", 0L);
                data.put("idle", 0L);
                return data;
            }
            temp = load.split(" ");
            for (int i = 1; i < temp.length; i++) {
                if (!temp[i].trim().equals("")) {
                    list.add(Long.parseLong(temp[i]));
                }
            }

        } catch (IOException ex) {
            Log.e("CPU", "IOException" + ex.toString());
            data.put("total_time", 0L);
            data.put("idle", 0L);
            return data;
        }
        long total_time = 0;
        for (int i = 0; i < list.size(); i++) {
            total_time += list.get(i);
        }
        data.put("total_time", total_time);
        data.put("idle", list.get(3));
        return data;
    }

}

