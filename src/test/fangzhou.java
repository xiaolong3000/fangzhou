package test;
import dao.Dao;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class fangzhou {//1-7 90 money 120 exp 150
    Random r = new Random();
    static int time = 120;//second
    static int lun = 45;//cishu

    public void runCMD(String context) {
        try {
            Runtime.getRuntime().exec(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        this.runCMD("adb connect 127.0.0.1:7555");
        System.out.println("connect");
        this.sleep(60);
    }



    public void sleep(int second) {
        try {
            Thread.sleep(1000 * second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void button_start_one() {
        this.runCMD("adb shell input tap " + (1250 - r.nextInt(3)) + " " + (625 + r.nextInt(3)) + " ");
        this.sleep(3);
        System.out.println("button one  " + new Date());
    }

    public void button_start_two() {
        this.runCMD("adb shell input tap " + (1250 - r.nextInt(3)) + " " + (750 - r.nextInt(3)) + " ");
        this.sleep(3);
        System.out.println("button two  " + new Date());
    }
    public void button_start_three(){
        this.runCMD("adb shell input tap 320 25");
        this.sleep(3);
        System.out.println("button three "+new Date());
    }

    public void shutdown() {
        this.runCMD("shutdown /s /t 60");
    }

    public void xunhuan(int time) {
        button_start_two();
        button_start_one();
        button_start_two();
        button_start_one();
        this.sleep(time);
        button_start_three();
        this.sleep(5);
    }


}
