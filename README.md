# 基于Prometheus+Grafana+Matrix构建的Android性能监控方案实践《二》

<a name="khaey"></a>
# 上篇回顾


上一篇[《**基于Prometheus+Grafana+Matrix构建的Android性能监控方案实践《一》**》](https://www.yuque.com/u115611/apm/mpn20z)主要讲了在Windows平台下安装和使用Prometheus+Grafana。

为了便于快速上手，我们采用Windows来做演示。但是实际用于生成环境建议还是使用docker来安装部署。不了解docker的同学需要单独学习，网上教程很多，选择合适的食用即可。

**本片文章的重点在《****数据采集之Pull And Push****》****，可跳过其它小节直接阅读。**

<a name="eeQnV"></a>
# 简单了解APM


APM这里指的是Application Performance Management，即应用性能管理。

<a name="y9iUj"></a>
## 应用运维遇到挑战


在云时代，分布式微服务架构下应用日益丰富，用户数量爆发式增长，纷杂的应用异常问题接踵而来。传统运维模式下，多套运维系统上的各项指标无法关联分析， 运维人员需要根据运维经验逐一排查应用异常，分析定位问题效率低，维护成本高且稳定性差。<br />海量业务下应用运维面临以下两个方面的挑战：<br />![](https://cdn.nlark.com/yuque/0/2020/png/248229/1584257677475-420f518b-267d-4d77-8115-1777146b9d57.png#align=left&display=inline&height=353&originHeight=353&originWidth=610&size=0&status=done&style=none&width=610)

- 大型分布式应用关系错综复杂，分析定位应用问题困难，应用运维面临如何保障应用正常、快速完成问题定位、迅速找到性能瓶颈的挑战。
- 应用体验差导致用户流失。运维人员不能实时感知并追踪体验差的业务，未能及时诊断应用异常，严重影响用户体验。

<br />
<a name="jbF4E"></a>
## 什么是APM


（Application Performance Management，简称APM）是实时监控并管理云应用性能和故障的云服务，提供专业的分布式应用性能分析能力，可以帮助运维人员快速解决应用在分布式架构下的问题定位和性能瓶颈等难题，为用户体验保驾护航。

APM作为云应用诊断服务，拥有强大的分析工具，通过[拓扑图](https://support.huaweicloud.com/usermanual-apm/apm_02_0007.html)、[调用链](https://support.huaweicloud.com/usermanual-apm/apm_02_0010.html)、[事务分析](https://support.huaweicloud.com/usermanual-apm/apm_02_0009.html)可视化地展现应用状态、调用过程、用户对应用的各种操作，快速定位问题和改善性能瓶颈。<br />**<br />**图1 **APM架构图<br />![](https://cdn.nlark.com/yuque/0/2020/png/248229/1584257888558-fc058f1f-a52b-4b19-975b-1e51e8e55924.png#align=left&display=inline&height=917&originHeight=917&originWidth=1087&size=0&status=done&style=none&width=1087)

1. 访问APM：通过IAM（统一身份认证）的委托、AK/SK鉴权可以访问APM。
1. 数据采集：APM可以通过非侵入方式采集[Java探针](https://support.huaweicloud.com/qs-apm/apm_00_0004.html)、[PHP探针、](https://support.huaweicloud.com/qs-apm/apm_00_0009.html)[Istio网格](https://support.huaweicloud.com/qs-apm/apm_00_0008.html)等提供的应用数据、基础资源数据、用户体验数据等多项指标。
1. 业务实现：APM支持[全链路拓扑](https://support.huaweicloud.com/usermanual-apm/apm_02_0007.html)、[调用链追踪](https://support.huaweicloud.com/usermanual-apm/apm_02_0010.html)、[事务分析](https://support.huaweicloud.com/usermanual-apm/apm_02_0009.html)、[端侧分析](https://support.huaweicloud.com/usermanual-apm/apm_02_0029.html)功能。
1. 业务拓展：
  - AOM（应用运维管理）实时监控应用运维指标，APM通过拓扑、调用链等快速诊断应用性能异常。
  - 通过APM找到性能瓶颈后，CPTS（云性能测试服务）关联分析生成性能报表。
  - 通过智能算法学习历史指标数据，APM多维度关联分析异常指标，提取业务正常与异常时上下文数据特征，通过聚类分析找到问题根因。

本小节摘抄自华为云官方文档介绍《 [应用性能管理 APM](https://support.huaweicloud.com/productdesc-apm/apm_06_0006.html)》，简单看看就好。国内外商业的 APM 有 Compuware、iMaster、博睿Bonree、听云、New Relic、云智慧、OneAPM、AppDyn、Amics等，可自行搜索了解。

<a name="82gaQ"></a>
# 快速上手Matrix 

<a name="GTJbg"></a>
## 介绍


Matrix 是一款微信研发并日常使用的应用性能接入框架，支持iOS, macOS和Android。 Matrix 通过接入各种性能监控方案，对性能监控项的异常数据进行采集和分析，输出相应的问题分析、定位与优化建议，从而帮助开发者开发出更高质量的应用。

实际上类似的Matrix应用性能接入框架还有很多，如360开源的ArgusAPM移动性能监控平台（[https://github.com/Qihoo360/ArgusAPM](https://github.com/Qihoo360/ArgusAPM)），滴滴开源的专门为移动应用设计的易用、轻量级且可扩展的质量优化框架Booster （[https://github.com/didi/booster](https://github.com/didi/booster)），更多方案可以自行搜索了解。

<a name="G1vDq"></a>
## [Matrix for Android]()


本小节主要介绍[Matrix for Android的快速上手。其他平台可自己参考官方文档。]()

Matrix-android 当前监控范围包括：应用安装包大小，帧率变化，启动耗时，卡顿，慢方法，SQLite 操作优化，文件读写，内存泄漏等等。

- APK Checker: 针对 APK 安装包的分析检测工具，根据一系列设定好的规则，检测 APK 是否存在特定的问题，并输出较为详细的检测结果报告，用于分析排查问题以及版本追踪
- Resource Canary: 基于 WeakReference 的特性和 [Square Haha](https://github.com/square/haha) 库开发的 Activity 泄漏和 Bitmap 重复创建检测工具
- Trace Canary: 监控界面流畅性、启动耗时、页面切换耗时、慢函数及卡顿等问题
- SQLite Lint: 按官方最佳实践自动化检测 SQLite 语句的使用质量
- IO Canary: 检测文件 IO 问题，包括：文件 IO 监控和 Closeable Leak 监控
<a name="FEsFV"></a>
### 
<a name="6hP5y"></a>
### Matrix 特性
与常规的 APM 工具相比，Matrix 拥有以下特点：
<a name="EdDVQ"></a>
#### APK Checker

- 具有更好的可用性：JAR 包方式提供，更方便应用到持续集成系统中，从而追踪和对比每个 APK 版本之间的变化
- 更多的检查分析功能：除具备 APKAnalyzer 的功能外，还支持统计 APK 中包含的 R 类、检查是否有多个动态库静态链接了 STL 、搜索 APK 中包含的无用资源，以及支持自定义检查规则等
- 输出的检查结果更加详实：支持可视化的 HTML 格式，便于分析处理的 JSON ，自定义输出等等
<a name="pUZOe"></a>
#### Resource Canary

- 分离了检测和分析部分，便于在不打断自动化测试的前提下持续输出分析后的检测结果
- 对检测部分生成的 Hprof 文件进行了裁剪，移除了大部分无用数据，降低了传输 Hprof 文件的开销
- 增加了重复 Bitmap 对象检测，方便通过减少冗余 Bitmap 数量，降低内存消耗
<a name="aBybh"></a>
#### Trace Canary

- 编译期动态修改字节码, 高性能记录执行耗时与调用堆栈
- 准确的定位到发生卡顿的函数，提供执行堆栈、执行耗时、执行次数等信息，帮助快速解决卡顿问题
- 自动涵盖卡顿、启动耗时、页面切换、慢函数检测等多个流畅性指标
<a name="1MJM3"></a>
#### SQLite Lint

- 接入简单，代码无侵入
- 数据量无关，开发、测试阶段即可发现SQLite性能隐患
- 检测算法基于最佳实践，高标准把控SQLite质量*
- 底层是 C++ 实现，支持多平台扩展
<a name="MByrr"></a>
#### IO Canary

- 接入简单，代码无侵入
- 性能、泄漏全面监控，对 IO 质量心中有数
- 兼容到 Android P
<a name="SsnZo"></a>
### 
<a name="2SfcW"></a>
### 使用方法
1.在你项目根目录下的 gradle.properties 中配置要依赖的 Matrix 版本号，如：

```
MATRIX_VERSION=0.6.5
```

注意：如果使用0.5.1，会出现[新版本0.5.1集成以后，一启动就crash](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FTencent%2Fmatrix%2Fissues%2F191)

2.在你项目根目录下的 build.gradle 文件添加 Matrix 依赖，如：

```
dependencies {
      classpath ("com.tencent.matrix:matrix-gradle-plugin:${MATRIX_VERSION}") { changing = true }
  }
```

因为changing = true，表示会自动检查更新。一般项目中没必要设置，所以可以简化成如下代码：

3.接着，在 app/build.gradle 文件中添加 Matrix 各模块的依赖，如：

```
 dependencies {
    implementation group: "com.tencent.matrix", name: "matrix-android-lib", version: MATRIX_VERSION, changing: true
    implementation group: "com.tencent.matrix", name: "matrix-android-commons", version: MATRIX_VERSION, changing: true
    implementation group: "com.tencent.matrix", name: "matrix-trace-canary", version: MATRIX_VERSION, changing: true
    implementation group: "com.tencent.matrix", name: "matrix-resource-canary-android", version: MATRIX_VERSION, changing: true
    implementation group: "com.tencent.matrix", name: "matrix-resource-canary-common", version: MATRIX_VERSION, changing: true
    implementation group: "com.tencent.matrix", name: "matrix-io-canary", version: MATRIX_VERSION, changing: true
    implementation group: "com.tencent.matrix", name: "matrix-sqlite-lint-android-sdk", version: MATRIX_VERSION, changing: true
  }

  apply plugin: 'com.tencent.matrix-plugin'
  matrix {
    trace {
        enable = true	//if you don't want to use trace canary, set false
        baseMethodMapFile = "${project.buildDir}/matrix_output/Debug.methodmap"
        blackListFile = "${project.projectDir}/matrixTrace/blackMethodList.txt"
    }
  }
```

注意：apply plugin必须要写在app的build.gradle，否则会提示Matrix Plugin, Android Application plugin required。<br />
enable：如果不需要启用matrix的trace canary，则可以设为false<br />
baseMethodMapFile：trace canary对于慢函数的分析，需要通过method_mapping文件解析堆栈，mapping文件在上传安装包的时候需要一起上传.


4.实现 PluginListener，接收 Matrix 处理后的数据, 如：

```
  public class TestPluginListener extends DefaultPluginListener {
    public static final String TAG = "Matrix.TestPluginListener";
    public TestPluginListener(Context context) {
        super(context);
        
    }

    @Override
    public void onReportIssue(Issue issue) {
        super.onReportIssue(issue);
        MatrixLog.e(TAG, issue.toString());
        
        //add your code to process data
    }
}
```

注意：1.官方demo是弹出新的activity来显示日志。如果有额外需求，比如记录到文件，均在此类中处理。<br />
注意：2.此处可能需要额外的文件，可以从[官方demo](https://github.com/Tencent/matrix/tree/master/samples/sample-android)中获取。可能用到的文件有IssuesMap、IssueFilter、ParseIssueUtil、IssuesListActivity 以及activity需要的xml文件。

5.实现动态配置接口， 可修改 Matrix 内部参数. 在 sample-android 中 我们有个简单的动态接口实例DynamicConfigImplDemo.java, 其中参数对应的 key 位于文件 MatrixEnum中， 摘抄部分示例如下：

```
 public class DynamicConfigImplDemo implements IDynamicConfig {
    public DynamicConfigImplDemo() {}

    public boolean isFPSEnable() { return true;}
    public boolean isTraceEnable() { return true; }
    public boolean isMatrixEnable() { return true; }
    public boolean isDumpHprof() {  return false;}

    @Override
    public String get(String key, String defStr) {
        //hook to change default values
    }

    @Override
    public int get(String key, int defInt) {
         //hook to change default values
    }

    @Override
    public long get(String key, long defLong) {
        //hook to change default values
    }

    @Override
    public boolean get(String key, boolean defBool) {
        //hook to change default values
    }

    @Override
    public float get(String key, float defFloat) {
        //hook to change default values
    }
}
```


可以完全复制[DynamicConfigImplDemo](https://github.com/Tencent/matrix/blob/b54b09ae06cc225c1cc9aedc8be39f3db4a2a340/samples/sample-android/app/src/main/java/sample/tencent/matrix/config/DynamicConfigImplDemo.java)<br />注意：可能会缺少[MatrixEnum](https://github.com/Tencent/matrix/blob/b54b09ae06cc225c1cc9aedc8be39f3db4a2a340/samples/sample-android/app/src/main/java/sample/tencent/matrix/config/MatrixEnum.java)文件

6.选择程序启动的位置对 Matrix 进行初始化，如在 Application 的继承类中， Init 核心逻辑如下：

```
  Matrix.Builder builder = new Matrix.Builder(application); // build matrix
  builder.patchListener(new TestPluginListener(this)); // add general pluginListener
  DynamicConfigImplDemo dynamicConfig = new DynamicConfigImplDemo(); // dynamic config
  
  // init plugin 
  IOCanaryPlugin ioCanaryPlugin = new IOCanaryPlugin(new IOConfig.Builder()
                    .dynamicConfig(dynamicConfig)
                    .build());
  //add to matrix               
  builder.plugin(ioCanaryPlugin);
  
  //init matrix
  Matrix.init(builder.build());

  // start plugin 
  ioCanaryPlugin.start();
```

至此，Matrix就已成功集成到你的项目中，并且开始收集和分析性能相关异常数据，如仍有疑问，请查看 [示例](https://github.com/Tencent/Matrix/tree/dev/samples/sample-android/).<br />PS： Matrix 分析后的输出字段的含义请查看 [Matrix 输出内容的含义解析](https://github.com/Tencent/matrix/wiki/Matrix-Android--data-format)
<a name="aPXpB"></a>
### 
<a name="fCj9T"></a>
#### APK Checker
APK Check 以独立的 jar 包提供 ([matrix-apk-canary-0.6.5.jar](https://jcenter.bintray.com/com/tencent/matrix/matrix-apk-canary/0.6.5/matrix-apk-canary-0.6.5.jar)），你可以运行：

```
java -jar matrix-apk-canary-0.6.5.jar
```


查看 Usages 来使用它。

```
Usages: 
    --config CONFIG-FILE-PATH
or
    [--input INPUT-DIR-PATH] [--apk APK-FILE-PATH] [--unzip APK-UNZIP-PATH] [--mappingTxt MAPPING-FILE-PATH] [--resMappingTxt RESGUARD-MAPPING-FILE-PATH] [--output OUTPUT-PATH] [--format OUTPUT-FORMAT] [--formatJar OUTPUT-FORMAT-JAR] [--formatConfig OUTPUT-FORMAT-CONFIG (json-array format)] [Options]
    
Options:
-manifest
     Read package info from the AndroidManifest.xml.
-fileSize [--min DOWN-LIMIT-SIZE (KB)] [--order ORDER-BY ('asc'|'desc')] [--suffix FILTER-SUFFIX-LIST (split by ',')]
     Show files whose size exceed limit size in order.
-countMethod [--group GROUP-BY ('class'|'package')]
     Count methods in dex file, output results group by class name or package name.
-checkResProguard
     Check if the resguard was applied.
-findNonAlphaPng [--min DOWN-LIMIT-SIZE (KB)]
     Find out the non-alpha png-format files whose size exceed limit size in desc order.
-checkMultiLibrary
     Check if there are more than one library dir in the 'lib'.
-uncompressedFile [--suffix FILTER-SUFFIX-LIST (split by ',')]
     Show uncompressed file types.
-countR
     Count the R class.
-duplicatedFile
     Find out the duplicated resource files in desc order.
-checkMultiSTL  --toolnm TOOL-NM-PATH
     Check if there are more than one shared library statically linked the STL.
-unusedResources --rTxt R-TXT-FILE-PATH [--ignoreResources IGNORE-RESOURCES-LIST (split by ',')]
     Find out the unused resources.
-unusedAssets [--ignoreAssets IGNORE-ASSETS-LIST (split by ',')]
     Find out the unused assets file.
-unstrippedSo  --toolnm TOOL-NM-PATH
     Find out the unstripped shared library file.
```

详细说明见 [Matrix-APKChecker](https://github.com/Tencent/matrix/wiki/Matrix-Android-ApkChecker) 。
<a name="8tfrY"></a>
# 
<a name="BYWTB"></a>
# 数据采集之Pull And Push

前面的小节都是科普和做准备工作。数据采集成功以后，就要把数据上传到Prometheus，主要有两种方式：Pull 和 Push。

Pull理解为数据拉取，这是一种被动的获取方式。基于C/S架构，我们在设备上内置一个Server用于提供数据获取的接口，Prometheus作为Client制定访问Server接口的定时任务用户获取采集的数据，最后存入时序数据仓库。

Push则刚好相反，Prometheus配置好一个PushGateWay作为Server，设备作为Client访问Server提供的接口定时上传采集到的数据。最后Prometheus从PushGateWay获取数据存入时序数据仓库。

<a name="Zpzhu"></a>
## HttpServer
HttpServer用于Pull方案。基于Android我们使用NanoHttpd来搭建服务器。

NanoHTTPD是一个免费、轻量级的(只有一个Java文件) HTTP服务器,可以很好地嵌入到Java程序中。支持 GET, POST, PUT, HEAD 和 DELETE 请求，支持文件上传，占用内存很小（[https://github.com/NanoHttpd/nanohttpd](https://github.com/NanoHttpd/nanohttpd)）。

<a name="adApp"></a>
### NanoHTTPD使用
使用方法很简单，参照Github上的文档就好了。这里还是简单说下Android上的使用，Gradle添加依赖：

```
implementation 'org.nanohttpd:nanohttpd:2.3.1'
```

一个简单完整的Server类如下：

```
 public class App extends NanoHTTPD {
    
        public App() throws IOException {
            super(8080);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        }
    
        public static void main(String[] args) {
            try {
                new App();
            } catch (IOException ioe) {
                System.err.println("Couldn't start server:\n" + ioe);
            }
        }
    
        @Override
        public Response serve(IHTTPSession session) {
            String msg = "<html><body><h1>Hello server</h1>\n";
            Map<String, String> parms = session.getParms();
            if (parms.get("username") == null) {
                msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
            } else {
                msg += "<p>Hello, " + parms.get("username") + "!</p>";
            }
            return newFixedLengthResponse(msg + "</body></html>\n");
        }
    }
```

运行App，浏览器打开[http://localhost:8080/ ](http://localhost:8080/)即可看到效果。

这是官方文档的简单示例，我们基于Android稍微改造下，通过Service来启动HttpServer。

创建一个服务类AndroidHttpServer 继承 NanoHTTPD，完整的AndroidHttpServer 如下：
```
public class AndroidHttpServer extends NanoHTTPD {

    private static final String TAG = "AndroidHttpServer";
		
    //定义一个默认的端口号
    private static final int DEFAULT_PORT = 8088;

    //Prometheus用于获取数据
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


```

创建AndroidHttpService来启动AndroidHttpServer。

```
public class AndroidHttpService extends Service {
    public AndroidHttpService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //注册prometheus的采集器
            new MemoryUsageCollector(getApplicationContext()).register();
            //启动AndroidHttpServer
            new AndroidHttpServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}】
```

浏览器访问：[http://设备IP:8088/metrics](http://192.168.0.2:8088/metrics)，返回类似数据，即代表成功。

```
# HELP MemoryUsage Android Performance Monitors
# TYPE MemoryUsage gauge
MemoryUsage{MemoryUsage="MemoryUsage",} 64.57366943359375
```
<br />
<a name="dSGyo"></a>
## PushGateWay


PushGateWay用于Push方案，需要Prometheus先配置PushGateWay。

[Pushgateway](https://github.com/prometheus/pushgateway) 是 Prometheus 生态中一个重要工具，使用它的原因主要是：

- Prometheus 采用 pull 模式，可能由于不在一个子网或者防火墙原因，导致 Prometheus 无法直接拉取各个 target 数据。
- 在监控业务数据的时候，需要将不同数据汇总, 由 Prometheus 统一收集。

由于以上原因，不得不使用 pushgateway，但在使用之前，有必要了解一下它的一些弊端：

- 将多个节点数据汇总到 pushgateway, 如果 pushgateway 挂了，受影响比多个 target 大。
- Prometheus 拉取状态 `up` 只针对 pushgateway, 无法做到对每个节点有效。
- Pushgateway 可以持久化推送给它的所有监控数据。

因此，即使你的监控已经下线，Prometheus 还会拉取到旧的监控数据，需要手动清理 pushgateway 不要的数据。

<a name="l1Vv3"></a>
### Pushgateway 安装和使用
中文教程：[ttps://songjiayang.gitbooks.io/prometheus/content/pushgateway/how.html](https://songjiayang.gitbooks.io/prometheus/content/pushgateway/how.html)<br />github:[https://github.com/prometheus/pushgateway](https://github.com/prometheus/pushgateway)

prometheus.yml中Pushgateway配置如下
```
  - job_name: 'pushgateway'
    honor_labels: true
    static_configs:
      - targets: ['填入IP:9091']
        labels:
          instance: pushgateway
```

配置成功即可通过Push的方式，往Pushgateway上传数据。

<a name="GuCER"></a>
### SDK的使用
Gradle添加以下依赖
```
 implementation 'io.prometheus:simpleclient_pushgateway:0.8.0'
```

定义IPushGateWay接口，PushGateWayImpl实现这个接口。<br />IPushGateWay类
```
public interface IPushGateWay {
    String getInstanceKey();
    String getInstanceValue();
    String getJobName();
    void push();
}
```


PushGateWayImpl类
```
public class PushGateWayImpl implements IPushGateWay{

    private Context context;

    //根据需要改成可配置的
    private static final String DEFAULT_PUSH_GATEWAY_SERVER_IP = "IP:9091";//pushgateway的ip

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
```

AndroidHttpService完整代码

```
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
```


在Service里通过ScheduledExecutorService创建一个定时任务，定时上传监控数据到PushGateWay。浏览器访问：PushGateWay的IP:9091,如出现自己设备IP的instance，即代表数据上传成功，之后就可以通过Prometheus直接查询相应的数据指标，并配置到Grafana面板里。
<a name="e13J5"></a>
# 
<a name="EoxEA"></a>
# 小结
Matrix可用于收集Android设备的相关性能指标，是一个数据采集工具。根据实际需求可以替换或自己编写相应的采集工具，获取到数据后通过Pull或者Push的方式与Prometheus对接，最终在Grafana面板上看到实时采集的数据，达到监控和数据可视化的目的。

当然还可以接入报警通知等业务，进一步的了解和使用可以参考：

[Prometheus 实战](https://songjiayang.gitbooks.io/prometheus/content/concepts/metric-types.html)<br />[Prometheus 教程](https://github.com/yunlzheng/prometheus-book)

非常感谢作者大大们！

<a name="tmOtz"></a>
# 参考资料
本文的撰写参考了以下资料，同样非常感谢！点击即可跳转到原文。

[什么是应用性能管理](https://support.huaweicloud.com/productdesc-apm/apm_06_0006.html)<br />[应用性能管理(APM, Application Performance Management)](https://www.cnblogs.com/polaris16/p/8886319.html)<br />[Android开发：移动端APM性能监控](https://www.jianshu.com/p/905081fb873b)<br />[Android APM 系列一（原理篇）](https://www.imooc.com/article/33354?block_id=tuijian_wz)<br />[Android 微信APM工具 Matrix使用](https://www.jianshu.com/p/0ff8646871f9)


