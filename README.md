# htrouter

```
    路由框架，帮助 Android App 工程进行组件化的框架
```

##### [![Join the chat at https://gitter.im/alibaba/ARouter](https://badges.gitter.im/alibaba/ARouter.svg)](https://gitter.im/alibaba/ARouter?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)

#### 最新版本

模块|htrouter|htrouterdispatch|htrouterdispatch-process|
---|---|---|---
最新版本|1.0.0|1.0.0|1.0.0

## 一、功能介绍
1. **支持直接解析标准URL进行跳转，并自动注入参数到目标页面中**
2. **支持动态拦截器、注解拦截器、动态拦截器**
3. **支持多模块工程使用**
4. **支持静态方法或者单例对象方法注解调用**
5. 支持降级URL设置，支持老版本
6. 支持标准URL进行正则匹配
7. 提供参数的解析工具方法
8. 路由表按组分类、多级管理，按需初始化
9. 提供登录注解字段
10. 支持注解动画配置

## 二、应用场景
1. 从外部推送、H5等外部URL映射到内部页面，及参数传递和解析
2. 跨模块页面跳转，模块间解耦
3. 拦截跳转过程，处理登录、参数转换、埋点等逻辑
4. 通过外部URL唤起调用静态方法或单例方法，跳转相关SDK页面，如图片选择器页面

## 三、功能使用

#### 1. SDK 引入

```gradle
repositories {
    maven { url 'https://raw.githubusercontent.com/bitterbee/mvn-repo/master/' }
}

apt {
    arguments {
        routerPkg '${applicationId}.router'
    }
}

dependencies {
	compile "com.netease.heartouch.router:htrouter:1.0.0"
    apt "com.netease.heartouch.router:htrouterdispatch-process:1.0.0"
}
```

`${applicationId}` 替换为当前模块工程的包名

#### 2. 初始化配置

在 Application.onCreate 中执行初始化逻辑

```java
// 初始化路由表
HTRouterCall.init();
//处理每次跳转监听 用户打点统计等
HTRouterCall.addGlobalInterceptors(new GlobalRouterInterceptor());

//注册绑定默认的降级页面
String customUrlKey = "customUrlKey"; //HTWebActivity接收参数key
HTRouterManager.registerWebActivity(WebActivity.class, customUrlKey);
//开启Debug模式，输出相应日志
HTRouterManager.setDebugMode(isDebug());
```

#### 2. 添加页面注解

``` java
// 在支持路由的页面上添加注解(必选)
// url 参数必选，支持一个 Activity 配置多个路由 URL
// entryAnim 和 exitAnim 配置入场动画和出场动画
// needLogin 配置当前页面是否需要登录状态
@HTRouter(url = {"http://www.you.163.com/activity/detail/{id}.shtml", "http://m.you.163.com/activity/detail/{id}.shtml"}, entryAnim = R.anim.enter, exitAnim = R.anim.exit)
public class ProductDetailActivity extends AppCompatActivity {
	...
}
```

#### 3. 添加方法注解

``` java
@HTMethodRouter(url = {"http://www.you.163.com/jumpB"})
public static void jumpSDK(Context context, String str, int i) {
	...
}
```

#### 4. 添加注解拦截器

```java
@HTInterceptAnno(url = {"http://www.you.163.com/activity/detail/{id}.shtml", "http://m.you.163.com/activity/detail/{id}.shtml"})
public class ProductDetailInterceptor implements IRouterInterceptor {

    @Override
    public void intercept(IRouterCall call) {
        HTRouterParams routerParams = call.getParams();
        
        // 如果需要终止跳转，调用 call.cancel();
        if (...) {
            call.cancel();
            return;
        }

		// 执行相关逻辑之后，继续跳转，调用 call.proceed();
		...
        call.proceed();
    }
}
```

#### 5. 执行路由跳转

页面路由：

```java
HTRouterCall.newBuilder("http://www.you.163.com/activity/detail/101.shtml") // 指定跳转目标
    .context(MainActivity.this)
    // 指定动态拦截器，优先级 > 注解拦截器 > 动态拦截器
    .interceptors(new IRouterInterceptor() {
        @Override
        public void intercept(final IRouterCall call) {
            Log.i("TEST", call.toString());
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("alert")
                    .setMessage("是否继续")
                    .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            call.proceed();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            call.cancel();
                        }
                    }).create();
            dialog.show();
        }
    })
	// 指定降级 url，外部唤起 app （如推送）支持老版本场景
    .downgradeUrls("http://m.you.163.com/activity/detail/{id}.shtml", "http://m.you.163.com/product/{id}.html")
    .build()
    .start();
```

方法路由：

```java
HTRouterCall.call(MainActivity.this, "http://www.you.163.com/jumpB?a=hanmeimei&b=10");

// 最终调用 jumpSDK(MainActivity.this, hanmeimei, 10);
```

