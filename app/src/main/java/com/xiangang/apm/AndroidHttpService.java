package com.xiangang.apm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AndroidHttpService extends Service {

    private static final String TAG = "AndroidHttpService";

    private ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(1);
    public AndroidHttpService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Pull方案的NanoHTTPD实现，在设备内置一个HTTPServer供外部访问
        try {
            //注册prometheus的采集器
            new MemoryUsageCollector(getApplicationContext()).register();
            //启动AndroidHttpServer
            new AndroidHttpServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Push方案的PushGateWay实现，使用scheduleWithFixedDelay定时上传数据到PushGateWay的接口
        final PushGateWayImpl pushGateWayImp = new PushGateWayImpl(getApplicationContext());
        mScheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "❤❤❤❤❤❤❤❤❤❤❤❤❤❤");
                pushGateWayImp.push();
            }
        }, 0, 10, TimeUnit.SECONDS);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
