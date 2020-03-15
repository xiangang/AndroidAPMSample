package com.xiangang.apm;

import android.content.Context;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;

/**
 * ================================================
 * Created by xiangang on 2020/3/15 20:36
 * <a href="mailto:xiangang12202@gmail.com">Contact me</a>
 * <a href="https://github.com/xiangang">Follow me</a>
 * ================================================
 */
public class PushGateWayImpl implements IPushGateWay{

    private Context context;

    //根据需要改成可配置的
    private static final String DEFAULT_PUSH_GATEWAY_SERVER_IP = "192.168.3.13:9091";//pushgateway的ip

    private static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = netI
                        .getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }

    public PushGateWayImpl(Context context) {
        this.context = context;
    }

    @Override
    public String getInstanceKey() {
        return "instance";
    }

    @Override
    public String getInstanceValue() {
        return getIpAddressString();
    }

    @Override
    public String getJobName() {
        return "AndroidJob";
    }


    @Override
    public void push() {
        try{
            //CollectorRegistry
            CollectorRegistry registry = new CollectorRegistry();
            //Gauge Of MemoryUsage
            Gauge gaugeMemoryUsage = Gauge.build("MemoryUsage", "Android Performance Monitors").create();
            gaugeMemoryUsage.set(CollectorUtil.getMemoryUsed(context));
            gaugeMemoryUsage.register(registry);
            //Push To Gateway
            PushGateway pg = new PushGateway(DEFAULT_PUSH_GATEWAY_SERVER_IP);
            Map<String, String> groupingKey = new HashMap<>();
            groupingKey.put(getInstanceKey(), getInstanceValue());
            pg.pushAdd(registry, getJobName(), groupingKey);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
