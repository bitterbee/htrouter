package com.netease.hearttouch.router;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class HTRouterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            HTLogUtil.d("receive URL:" + intent.getData().toString());
            String url = intent.getData().toString();
            HTRouterManager.startActivity(HTRouterActivity.this, url, intent, true);
        } else {
            finish();
            HTLogUtil.d("page error,needs URL format ");
            Toast.makeText(HTRouterActivity.this, "page error", Toast.LENGTH_SHORT).show();
        }
    }
}
