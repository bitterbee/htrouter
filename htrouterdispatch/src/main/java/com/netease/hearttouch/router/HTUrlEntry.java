package com.netease.hearttouch.router;

import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class HTUrlEntry {
    /** URL参数值对应的正则表达式 */
    private static final String PARAM_VALUE = "([a-zA-Z0-9_#'!+%~,\\-\\.\\$]*)";
    /** URL参数键对应的正则表达式 */
    private static final String PARAM = "([a-zA-Z][a-zA-Z0-9_-]*)";
    /** 填充字段的正则，用于将{id}转换为实际匹配的正则表达式 */
    private static final String PARAM_REGEX = "%7B(" + PARAM + ")%7D";
    /** 正向匹配正则 */
    private final Pattern regex;
    /** 逆向匹配正则 */
    private final Pattern regexReverse;


    /** 本页面可以匹配的URL */
    protected String url;
    /** 本页面url中对应的scheme */
    private String scheme;
    /** 本页面url中对应的host */
    private String host;

    public HTUrlEntry(String url) {
        this.url = url;
        this.scheme = scheme(url);
        this.host = host(url);

        //替换掉URL中填充的占位信息，例如{id}
        this.regex = Pattern.compile(hostAndPath(url).replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
        this.regexReverse = Pattern.compile(hostAndPath(url).replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
    }

    protected String scheme(String url) {
        if (url != null) {
            int position = url.indexOf("://");
            if (position != -1) {
                return url.substring(0, position);
            }
        }
        return "";
    }

    protected String host(String url) {
        int postion = url.indexOf("://");
        //去掉scheme
        if (postion != -1) {
            url = url.substring(postion + "://".length());
        }
        //去掉参数
        postion = url.indexOf("?");
        if(postion != -1) {
            url = url.substring(0, postion);
        }
        String[] urls = url.split("/");
        return urls.length > 0 ? URLEncoder.encode(urls[0]) : "";
    }

    /**
     * 进行正向匹配，判断传入的URL是否能跳转到当前页面
     *
     * @param inputUrl 需要进行判断的URL
     * @return 如果能跳转返回true，如果不能则返回false
     */
    public boolean matches(String inputUrl) {
        return inputUrl != null &&
                scheme.equals(scheme(inputUrl)) &&
                host.equals(host(inputUrl)) &&
                regex.matcher(hostAndPath(inputUrl)).find();
    }

    /**
     * 进行反向匹配，判断传入的URL是否能跳转到当前页面，预留接口
     * 目的是忽略scheme进行匹配
     *
     * @param inputUrl 需要进行判断的URL
     * @return 如果能跳转返回true，如果不能则返回false
     */
    public boolean reverseMatches(String inputUrl) {
        return inputUrl != null && regexReverse.matcher(hostAndPath(inputUrl)).find();
    }

    private String hostAndPath(String url) {
        int postion = url.indexOf("://");
        //去掉scheme
        if (postion != -1) {
            url = url.substring(postion + "://".length());
        }
        //去掉参数
        postion = url.indexOf("?");
        if(postion != -1) {
            url = url.substring(0, postion);
        }
        String[] urls = url.split("/");
        //每一段都encode一下
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urls.length; i++) {
            sb.append(URLEncoder.encode(urls[i]));
            if (i != urls.length - 1){
                sb.append('/');
            }
        }
        return sb.toString();
    }
}
