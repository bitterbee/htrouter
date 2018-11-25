package com.netease.hearttouch.router;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyl06 on 13/03/2018.
 */
public class HTRouterParams {
    public String url;
    public List<String> downgradeUrls = new ArrayList<>();
    public boolean isFinish;
    public int entryAnim;
    public int exitAnim;
    public boolean forResult;
    public int requestCode;
}
