package com.xiangang.apm;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import fi.iki.elonen.NanoHTTPD;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

/**
 * ================================================
 * Created by xiangang on 2020/3/15 17:45
 * <a href="mailto:xiangang12202@gmail.com">Contact me</a>
 * <a href="https://github.com/xiangang">Follow me</a>
 * ================================================
 */
public class AndroidHttpServer extends NanoHTTPD {

    private static final String TAG = "AndroidHttpServer";

    private static final int DEFAULT_PORT = 8088;

    //Prometheus用于注册Collector
    private CollectorRegistry registry;

    //ByteArrayOutputStream
    private final LocalByteArray response = new LocalByteArray();
    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        protected ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream(1 << 20);
        }
    }

    public AndroidHttpServer() {
        this(DEFAULT_PORT);
    } 
    
    public AndroidHttpServer(int port) {
        super(port);
        registry = CollectorRegistry.defaultRegistry;
    }

    public AndroidHttpServer(String hostname, int port) {
        super(hostname, port);
        registry = CollectorRegistry.defaultRegistry;
    }

    @Override
    public Response serve(IHTTPSession session) {
        //获取浏览器输入的Uri
        String uri = session.getUri();
        //获取session的Method
        Method method = session.getMethod();
        Log.i(TAG, "method = " + method + " uri= " + uri);
        //这里需要判断下Uri是否符合要求，比如浏览器输入http://localhost:8088/metrics符合，其他都不合符。
        if(uri.startsWith("/metrics")){
            //本地输出流
            ByteArrayOutputStream response = this.response.get();
            if(response == null){
                return newFixedLengthResponse("response is null ");
            }
            //每次使用前要reset
            response.reset();
            //创建一个Writer
            OutputStreamWriter osw = new OutputStreamWriter(response);
            try {
                TextFormat.write004(osw, registry.filteredMetricFamilySamples(parseQuery(uri)));
                osw.flush();
                osw.close();
                response.flush();
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseMetrics(session, response.toByteArray());
        }
        return response404(uri);
    }

    /**
     * 是否压缩
     * @param session IHTTPSession
     * @return boolean
     */
    protected static boolean shouldUseCompression(IHTTPSession session) {
        String encodingHeaders = session.getHeaders().get("Accept-Encoding");
        if (encodingHeaders == null) return false;

        String[] encodings = encodingHeaders.split(",");
        for (String encoding : encodings) {
            if (encoding.trim().toLowerCase().equals("gzip")) {
                return true;
            }
        }
        return false;
    }


    /**
     * 解析uri
     * @param query String
     * @return Set<String>
     * @throws IOException
     */
    protected static Set<String> parseQuery(String query) throws IOException {
        Set<String> names = new HashSet<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
    }

    /**
     * 访问/metrics,返回对应的Response
     * @param session IHTTPSession
     * @param bytes byte[]
     * @return Response
     */
    private Response responseMetrics(IHTTPSession session,byte[] bytes) {
        //调用newFixedLengthResponse,生成一个Response
        Response response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,new ByteArrayInputStream(bytes), bytes.length);
        //Header添加Content-Type:"text/plain; version=0.0.4; charset=utf-8"
        response.addHeader("Content-Type", TextFormat.CONTENT_TYPE_004);
        if (shouldUseCompression(session)) {
            //Header添加Content-Encoding:"gzip"
            response.addHeader("Content-Encoding", "gzip");
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(out);
                gzip.write(bytes);
                gzip.close();
                response.setData(new ByteArrayInputStream(out.toByteArray()));
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            response.addHeader("Content-Length",  String.valueOf(bytes.length));
        }
        return response;
    }

    /**
     * 访问无效页面，返回404
     * @param url 没有定义的url
     * @return Response
     */
    private Response response404(String url) {
        //构造一个简单的Html 404页面
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Sorry,Can't Found Uri:" );
        builder.append(url );
        builder.append(" !");
        builder.append("</body></html>\n");
        //调用newFixedLengthResponse返回一个固定长度的Response
        return newFixedLengthResponse(builder.toString());
    }
}
