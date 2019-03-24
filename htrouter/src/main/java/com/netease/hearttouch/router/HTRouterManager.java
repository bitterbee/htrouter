package com.netease.hearttouch.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.netease.hearttouch.router.intercept.HTInterceptorEntry;
import com.netease.hearttouch.router.method.HTMethodRouterEntry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zyl06 on 13/03/2018.
 */
public class HTRouterManager {
    public static final String HT_URL_PARAMS_KEY = "ht_url_params_map";

    private static Class mWebActivityClass;

    private static String mWebExtraKey = "HTUrl";

    public static final String HT_TARGET_URL_KEY = "HTROUTER_TARGET_URL_KEY";

    public static final int NATIVE = 1;

    public static final int H5 = 2;

    private static final Map<String, List<HTRouterEntry>> PAGE_ROUTERS = new HashMap<>();
    private static final Map<String, List<IRouterGroup>> PAGE_GROUPS = new HashMap<>();
    static final List<HTMethodRouterEntry> METHOD_ENTRIES = new LinkedList<>();
    private static final Map<String, HTRouterEntry> mEntryCache = new HashMap<>();
    private static final Map<String, HTMethodRouterEntry> mMethodEntryCache = new HashMap<>();

    static void init(Map<String, IRouterGroup> pageGroups,
                     List<HTMethodRouterEntry> methodEntries,
                     List<HTInterceptorEntry> annoInterceptors) {

        if (pageGroups != null) {
            for (String name : pageGroups.keySet()) {
                List<IRouterGroup> groups = PAGE_GROUPS.get(name);
                if (groups == null) {
                    groups = new LinkedList<>();
                    PAGE_GROUPS.put(name, groups);
                }
                groups.add(pageGroups.get(name));
            }
        }
        if (methodEntries != null) {
            METHOD_ENTRIES.addAll(methodEntries);
        }
        if (annoInterceptors != null) {
            HTRouterCall.initAnnoInterceptors(annoInterceptors);
        }
    }

    /**
     * 设置当前无法处理的URL的降级Webview类，将会通过这个webview进行处理
     *
     * @params webActivityClass 降级处理的webview的类型
     */
    public static void registerWebActivity(Class webActivityClass) {
        registerWebActivity(webActivityClass, mWebExtraKey);
    }

    /**
     * 设置当前无法处理的URL的降级Webview类，将会通过这个webview进行处理
     *
     * @params webActivityClass 降级处理的webview的类型
     * @params webExtraKey 自定义的传递给webview的参数的键
     */
    public static void registerWebActivity(Class webActivityClass, String webExtraKey) {
        if (webActivityClass == null || TextUtils.isEmpty(webExtraKey)) {
            return;
        }
        mWebActivityClass = webActivityClass;
        mWebExtraKey = webExtraKey;
    }

    public static String getSchemaHostAndPath(String url) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException();
        }
        Uri uri = Uri.parse(url);
        return uri.getScheme() + "://" + uri.getHost() + uri.getPath();
    }

    public static String getHostAndPath(String url) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException();
        }
        Uri uri = Uri.parse(url);
        return uri.getHost() + uri.getPath();
    }

    public static HashMap<String, String> getParams(String url) {
        HashMap<String, String> params = new LinkedHashMap<>();

        Uri uri = Uri.parse(url);
        String query = uri.getEncodedQuery();
        if (query != null) {
            String[] entries = query.split("&");
            for (String entry : entries) {
                String[] keys = entry.split("=");
                if (keys != null && keys.length >= 2) {
                    params.put(keys[0], keys[1]);
                }
            }
        }
        return params;
    }

    public static String getHttpSchemaHostAndPath(String url) {
        Uri uri = Uri.parse(url);
        return url.replaceFirst(uri.getScheme(), "http");
    }

    private static int getAnimIdMethod(Class activity, boolean isExitAnim) {
        HTRouter anno = activity.getClass().getAnnotation(HTRouter.class);
        if (anno != null) {
            return isExitAnim ? anno.exitAnim() : anno.entryAnim();
        }
        return 0;
    }

    /**
     * 通过URL找到可以跳转的页面的信息
     *
     * @param url 需要进行匹配的URL
     * @return 返回匹配成功后的实体类，如果找不到会返回null
     */
    public static HTRouterEntry findRouterEntryByUrl(String url) {
        HTRouterEntry result = mEntryCache.get(url);
        if (result != null) {
            return result;
        }

        String group = RouterGroupHelper.getGroup(url);
        List<HTRouterEntry> entries = PAGE_ROUTERS.get(group);
        if (entries == null) {
            List<IRouterGroup> groups = PAGE_GROUPS.get(group);
            if (groups == null) {
                return null;
            }

            entries = new LinkedList<>();
            PAGE_ROUTERS.put(group, entries);
            for (IRouterGroup rg : groups) {
                entries.addAll(rg.pageRouters());
            }
        }

        for (HTRouterEntry entry : entries) {
            if (entry.matches(url)) {
                mEntryCache.put(url, entry);
                return entry;
            }
        }
        return null;
    }

    /**
     * 根据传入的 url 能否找到相关的跳转页面信息@param url 需要进行匹配的URL
     *
     * @return 能找到相关的跳转页面信息，返回true，否则返回false
     */
    public static boolean isUrlRegistered(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return findRouterEntryByUrl(url) != null;
    }

    /**
     * 通过URL启动一个页面
     *
     * @param activity     当前需要进行跳转的activity
     * @param url          跳转的目标URL
     * @param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展
     * @param isFinish     跳转后是否需要关闭当前页面
     * @param entryAnim    自定义的入场动画
     * @param exitAnim     自定义的出场动画
     */
    /*package*/
    static void startActivity(Activity activity,
                              String url,
                              Intent sourceIntent,
                              boolean isFinish,
                              int entryAnim,
                              int exitAnim) {
        Intent intent = null;
        HTRouterEntry entry = findRouterEntryByUrl(url);
        if (entry != null) {
            intent = processIntent(activity, sourceIntent, url, entry.getActivity());
            activity.startActivity(intent);
            if (exitAnim == 0 && entryAnim == 0) {
                exitAnim = getAnimIdMethod(activity.getClass(), true);
                entryAnim = getAnimIdMethod(entry.getActivity(), false);
            }
            if (isFinish) {
                activity.finish();
            }
            if (entryAnim != 0 || exitAnim != 0) {
                activity.overridePendingTransition(entryAnim, exitAnim);
            }
        } else if (mWebActivityClass != null) {
            intent = processIntent(activity, sourceIntent, url, mWebActivityClass);
            intent.putExtra(mWebExtraKey, getHttpSchemaHostAndPath(url));
            activity.startActivity(intent);
            if (exitAnim == 0 && entryAnim == 0) {
                exitAnim = getAnimIdMethod(activity.getClass(), true);
                entryAnim = getAnimIdMethod(mWebActivityClass, false);
            }
            if (isFinish) {
                activity.finish();
            }
            if (entryAnim != 0 || exitAnim != 0) {
                activity.overridePendingTransition(entryAnim, exitAnim);
            }
        }
    }

    /**
     * 通过URL启动一个页面
     *
     * @param activity     当前需要进行跳转的activity
     * @param url          跳转的目标URL
     * @param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展
     * @param isFinish     跳转后是否需要关闭当前页面
     */
    /*package*/
    static void startActivity(Activity activity, String url, Intent sourceIntent, boolean isFinish) {
        startActivity(activity, url, sourceIntent, isFinish, 0, 0);
    }

    /**
     * 通过URL启动一个页面
     *
     * @param context      当前需要进行跳转的 context
     *                     如果 context 是 Activity，则使用 startActivity(Activity activity, String url, Intent sourceIntent, boolean isFinish)进行跳转
     *                     否则这个函数进行处理，但参数 isFinish 不会生效，另外启动页面的场景切换动画会使用主题默认动画
     * @param url          跳转的目标URL
     * @param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展
     * @param isFinish     跳转后是否需要关闭当前页面
     * @param entryAnim    自定义的入场动画
     * @param exitAnim     自定义的出场动画
     */
    /*package*/
    static void startActivity(Context context,
                              String url,
                              Intent sourceIntent, boolean isFinish, int entryAnim, int exitAnim) {
        if (context instanceof Activity) {
            startActivity((Activity) context, url, sourceIntent,
                    isFinish, entryAnim, exitAnim);
            return;
        }
        Intent intent = null;
        HTRouterEntry entry = findRouterEntryByUrl(url);
        if (entry != null) {
            intent = processIntent(context, sourceIntent, url, entry.getActivity());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (mWebActivityClass != null) {
            intent = processIntent(context, sourceIntent, url, mWebActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(mWebExtraKey, getHttpSchemaHostAndPath(url));
            context.startActivity(intent);
        }
    }

    /**
     * 通过URL启动一个页面,同时可以获得result回调
     *
     * @param activity     当前需要进行跳转的activity
     * @param url          跳转的目标URL
     * @param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展
     * @param isFinish     跳转后是否需要关闭当前页面
     * @param entryAnim    自定义的入场动画
     * @param exitAnim     自定义的出场动画
     */
    /*package*/
    static void startActivityForResult(Activity activity,
                                       String url,
                                       Intent sourceIntent,
                                       boolean isFinish,
                                       int requestCode,
                                       int entryAnim,
                                       int exitAnim) {
        Intent intent = null;
        HTRouterEntry entry = findRouterEntryByUrl(url);
        if (entry != null) {
            intent = processIntent(activity, sourceIntent, url, entry.getActivity());
            activity.startActivityForResult(intent, requestCode);
            if (exitAnim == 0 && entryAnim == 0) {
                exitAnim = getAnimIdMethod(activity.getClass(), true);
                entryAnim = getAnimIdMethod(entry.getActivity(), false);
            }
            if (isFinish) {
                activity.finish();
            }
            if (entryAnim != 0 || exitAnim != 0) {
                activity.overridePendingTransition(entryAnim, exitAnim);
            }
        } else if (mWebActivityClass != null) {
            intent = processIntent(activity, sourceIntent, url, mWebActivityClass);
            intent.putExtra(mWebExtraKey, getHttpSchemaHostAndPath(url));
            activity.startActivityForResult(intent, requestCode);
            if (exitAnim == 0 && entryAnim == 0) {
                exitAnim = getAnimIdMethod(activity.getClass(), true);
                entryAnim = getAnimIdMethod(mWebActivityClass, false);
            }
            if (isFinish) {
                activity.finish();
            }
            if (entryAnim != 0 || exitAnim != 0) {
                activity.overridePendingTransition(entryAnim, exitAnim);
            }
        }
    }

    /**
     * 通过URL启动一个页面,同时可以获得result回调，回调在fragment中
     *
     * @param fragment     当前需要进行跳转的fragment
     * @param url          跳转的目标URL
     * @param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展
     * @param isFinish     跳转后是否需要关闭当前页面
     * @param entryAnim    自定义的入场动画
     * @param exitAnim     自定义的出场动画
     */
    /*package*/
    static void startActivityForResult(Fragment fragment,
                                       String url,
                                       Intent sourceIntent,
                                       boolean isFinish,
                                       int requestCode,
                                       int entryAnim,
                                       int exitAnim) {
        if (fragment.getActivity() == null) {
            return;
        }

        Intent intent = null;
        HTRouterEntry entry = findRouterEntryByUrl(url);
        if (entry != null) {
            intent = processIntent(fragment.getActivity(), sourceIntent, url, entry.getActivity());
            fragment.startActivityForResult(intent, requestCode);
            if (exitAnim == 0 && entryAnim == 0) {
                exitAnim = getAnimIdMethod(fragment.getActivity().getClass(), true);
                entryAnim = getAnimIdMethod(entry.getActivity(), false);
            }
            if (isFinish) {
                fragment.getActivity().finish();
            }
            if (entryAnim != 0 || exitAnim != 0) {
                fragment.getActivity().overridePendingTransition(entryAnim, exitAnim);
            }
        } else if (mWebActivityClass != null) {
            intent = processIntent(fragment.getActivity(), sourceIntent, url, mWebActivityClass);
            intent.putExtra(mWebExtraKey, getHttpSchemaHostAndPath(url));
            fragment.startActivityForResult(intent, requestCode);
            if (exitAnim == 0 && entryAnim == 0) {
                exitAnim = getAnimIdMethod(fragment.getActivity().getClass(), true);
                entryAnim = getAnimIdMethod(mWebActivityClass, false);
            }
            if (isFinish) {
                fragment.getActivity().finish();
            }
            if (entryAnim != 0 || exitAnim != 0) {
                fragment.getActivity().overridePendingTransition(entryAnim, exitAnim);
            }
        }
    }

    static Object callMethod(@Nullable Context context, String url)
            throws ParamConvertor.ParamConvertException, NullPointerException {
        HTMethodRouterEntry entry = findMethodRouterEntryByUrl(url);
        Method method = null;
        if (entry != null && entry.isValid()) {
            method = entry.getMethod();
        }
        if (method == null) {
            HTLogUtil.d("entry is invalid. url = " + url);
            throw new NullPointerException("\"entry is invalid. url = \" + url");
        }

        List paramObjs = new ArrayList();
        Uri uri = Uri.parse(url);
        char c = 'a';
        for (Class clazz : entry.paramTypes) {
            if (Context.class.isAssignableFrom(clazz)) {
                paramObjs.add(context);
            } else {
                String value = uri.getQueryParameter(Character.toString(c));
                paramObjs.add(ParamConvertor.toObj(value, clazz));
                c += 1;
            }
        }

        if ((method.getModifiers() & Modifier.STATIC) != 0) {
            return RefInvoker.invoke(null, method, paramObjs.toArray());
        } else {
            Object instance = RefInvoker.invokeStaticMethod(entry.className, "getInstance", null, null);
            if (instance != null) {
                return RefInvoker.invoke(instance, method, paramObjs.toArray());
            }
        }

        return null;
    }


    public static HTMethodRouterEntry findMethodRouterEntryByUrl(String url) {
        HTMethodRouterEntry result = mMethodEntryCache.get(url);
        if (result == null) {
            for (HTMethodRouterEntry entry : METHOD_ENTRIES) {
                if (entry.matches(url)) {
                    mMethodEntryCache.put(url, entry);
                    result = entry;
                    break;
                }
            }
        }

        return result;
    }

    public static void setDebugMode(boolean debug) {
        HTLogUtil.setDebugMode(debug);
    }

    private static Intent processIntent(Context context, Intent sourceIntent, String url, Class activityClass) {
        Intent intent = null;
        if (sourceIntent != null) {
            intent = (Intent) sourceIntent.clone();
            intent.setClass(context, activityClass);
        } else {
            intent = new Intent(context, activityClass);
        }
        intent.putExtra(HT_TARGET_URL_KEY, Uri.parse(url));

        HashMap<String, String> paramsMap = getParams(url);
        if (paramsMap != null) {
            intent.putExtra(HT_URL_PARAMS_KEY, paramsMap);
        }
        return intent;
    }
}
