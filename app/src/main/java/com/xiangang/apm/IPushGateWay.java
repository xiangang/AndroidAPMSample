package com.xiangang.apm;

/**
 * ================================================
 * Created by xiangang on 2020/3/15 20:35
 * <a href="mailto:xiangang12202@gmail.com">Contact me</a>
 * <a href="https://github.com/xiangang">Follow me</a>
 * ================================================
 */
public interface IPushGateWay {
    String getInstanceKey();
    String getInstanceValue();
    String getJobName();
    void push();
}
