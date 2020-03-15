package com.xiangang.apm;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.prometheus.client.Collector;

/**
 * ================================================
 * Created by xiangang on 2020/3/15 18:35
 * <a href="mailto:xiangang12202@gmail.com">Contact me</a>
 * <a href="https://github.com/xiangang">Follow me</a>
 * ================================================
 */
public class MemoryUsageCollector extends Collector implements ICollector {

    private Context context;

    public MemoryUsageCollector(Context context) {
        this.context = context;
    }

    @Override
    public String getMetricName() {
        return "MemoryUsage";
    }

    @Override
    public String getHelp() {
        return "Android Performance Monitors";
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfsList = new ArrayList<>();
        String metricName = getMetricName();
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(metricName, Arrays.asList(metricName), Arrays.asList(metricName), CollectorUtil.getMemoryUsed(context));
        MetricFamilySamples mfs = new MetricFamilySamples(metricName, Type.GAUGE, getHelp(), Arrays.asList(sample));
        mfsList.add(mfs);
        return mfsList;
    }
}
