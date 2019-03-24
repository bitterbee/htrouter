package com.netease.hearttouch.example;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.netease.hearttouch.router.HTRouter;
import com.netease.hearttouch.router.HTRouterCall;
import com.netease.hearttouch.router.IRouterCall;
import com.netease.hearttouch.router.intercept.IRouterInterceptor;

@HTRouter(url = {"http://www.you.163.com/", "http://m.you.163.com"}, entryAnim = R.anim.enter, exitAnim = R.anim.exit, needLogin = false)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPageRouter0(View v) {
        HTRouterCall.newBuilder("http://www.you.163.com/activity/detail/101.shtml")
                .context(MainActivity.this)
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
                .downgradeUrls("http://m.you.163.com/activity/detail/{id}.shtml", "http://m.you.163.com/product/{id}.html")
                .build()
                .start();
    }

    public void onMethodRouter0(View v) {
        HTRouterCall.call(MainActivity.this, "http://www.you.163.com/jumpA?a=lilei&b=10");
    }

    public void onMethodRouter1(View v) {
        HTRouterCall.call(MainActivity.this, "http://www.you.163.com/jumpB?a=hanmeimei&b=10");
    }

    public void onMethodRouter2(View v) {
        HTRouterCall.call(MainActivity.this, "http://www.you.163.com/jumpC");
    }
}
