package com.delvedapps.core.webservice;

import android.util.Log;

import com.delvedapps.core.parser.WebDataParser;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.NetworkMessage;
import com.delvedapps.core.util.contstant.Constants;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class WebService {
    private final String TAG = WebService.class.getSimpleName();

    // Point these at your own HTTPS server; README.md shows the JSON each one returns.
    private final static String URL_APP_DATA = "https://example.com/fakegps/AppData.json";
    private final static String URL_APP_COMMANDS = "https://example.com/fakegps/Commands.json";

    public final static int GET = 2;
    public final static int GET_APP_DATA = 10;
    public final static int GET_APP_COMMANDS = 11;
    public final static int APP_MUST_UPDATE = 777;


    private int retryCount = 1;

    public void webEvent(final int[] type, final String[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int status;
                try {
                    final OkHttpClient client = new OkHttpClient();
                    client.setConnectTimeout(Constants.WEB_SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
                    client.setReadTimeout(Constants.WEB_SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);

                    final Request.Builder builder = new Request.Builder();
                    builder.url(buildUrl(type[1], data));

                    if (type[0] == GET) {
                        builder.addHeader(Constants.CONTENT_TYPE, Constants.MIME_TYPE_JSON);
                    }

                    Response response = client.newCall(builder.build()).execute();
                    status = response.code();
                    String content = response.body().string();

                    Log.d(TAG, "Response Url : " + response.request().url());
                    Log.d(TAG, "Response Code : " + String.valueOf(status));
                    Log.d(TAG, "Response Content : " + content);

                    if (response.isSuccessful()) {
                        new WebDataParser().parseRawWebObject(content, type[1]);
                    }

                } catch (Exception e) {
                    status = ((e instanceof IOException) ? Constants.IO_ERROR_CODE : Constants.JSON_ERROR_CODE);
                    e.printStackTrace();
                }

                if (status == Constants.IO_ERROR_CODE && retryCount <= Constants.WEB_RETRY_MAX) {
                    retryCount++;
                    webEvent(type, data);
                } else {
                    try {
                        retryCount = 1;
                        Listener.send(new NetworkMessage<>(type[1], getInternalResponse(status)));
                    } catch (Exception e) {
                        Log.d(TAG, "app was killed");
                    }
                }
            }
        }
        ).start();
    }

    private static String getInternalResponse(int status) {
        if (status >= 200 && status < 300)
            return Constants.VALID_WEB_EVENT;
        else if (status == Constants.IO_ERROR_CODE)
            return Constants.IO_ERROR;
        else if (status == Constants.JSON_ERROR_CODE)
            return Constants.APP_ERROR + String.valueOf(Constants.JSON_ERROR_CODE);
        else if (status >= 500 && status < 600)
            return Constants.SERVER_ERROR + String.valueOf(status);
        else if (status >= 400 && status < 500)
            return Constants.CLIENT_ERROR + String.valueOf(status);
        else
            return Constants.UNKNOWN_ERROR + String.valueOf(status);
    }

    private static String buildUrl(int type, String[] data) {
        switch (type) {
            case GET_APP_DATA:
                return URL_APP_DATA;
            case GET_APP_COMMANDS:
                return URL_APP_COMMANDS;
            default:
                return null;
        }
    }

}