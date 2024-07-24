package org.unipi.mpsp2343.smartalert;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.chromium.net.UploadDataProviders;
import org.chromium.net.UrlRequest;

import org.chromium.net.CronetEngine;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipi.mpsp2343.smartalert.dto.User;

import java.util.concurrent.Executor;

//Provides an interface with the authentication/authorization system to the activities and services that need them
public class Authentication {
    private static final long REQUEST_TIMEOUT_MS = 5000;
    private static final String AUTH_URL = "http://192.168.1.2:8080/api/v1/public/auth"; //The url to the auth controller
    private static final String LOGIN_ENDPOINT = "login"; //login endpoint
    private static final String SIGNUP_ENDPOINT = "signup"; //signup endpoint
    private final CronetEngine cronetEngine;
    private final Executor executor;
    private final Handler mainHandler;
    private final Handler timeoutHandler;
    private final LocalStorage localStorage;
    private User user = null;

    public Authentication(CronetEngine cronetEngine, Executor executor, LocalStorage localStorage) {
        this.cronetEngine = cronetEngine;
        this.executor = executor;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        this.localStorage = localStorage;
        user = this.localStorage.getUser();
    }

    //Logs in the user, based on te given username and password
    public Task<String> login(String email, String password) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                AUTH_URL + "/" + LOGIN_ENDPOINT,
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
                            //Parses the returned data into a User object
                            //Data are saved in the user field for retrieval by any component that might need them
                            user = new Gson().fromJson(getResponse(), new TypeToken<User>(){}.getType());
                            //Saves the user data to local storage
                            localStorage.saveUser(user);
                            mainHandler.post(() -> taskCompletionSource.setResult(""));
                        }
                        else {
                            timeoutHandler.removeCallbacksAndMessages(null);
                            mainHandler.post(() -> taskCompletionSource.setException(new Exception(getResponse())));
                        }
                    }

                    @Override
                    public void onCanceled(UrlRequest request, UrlResponseInfo info){
                        timeoutHandler.removeCallbacksAndMessages(null);
                        mainHandler.post(() -> taskCompletionSource.setException(new Exception("error_connection")));
                    }
                },
                executor
        );

        requestBuilder.setHttpMethod("POST");
        requestBuilder.addHeader("Content-Type", "application/json");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            mainHandler.post(() -> taskCompletionSource.setException(new Exception("error_unexpected_error")));
            return taskCompletionSource.getTask();
        }
        requestBuilder.setUploadDataProvider(
                UploadDataProviders.create(jsonBody.toString().getBytes()),
                executor
        );
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Signs up a new user. Uses the provides email and password as the credentials of the new user.
    public Task<String> signup(String email, String password) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                AUTH_URL + "/" + SIGNUP_ENDPOINT,
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
                            mainHandler.post(() -> taskCompletionSource.setResult(""));
                        }
                        else {
                            mainHandler.post(() -> taskCompletionSource.setException(new Exception(getResponse())));
                        }
                    }

                    @Override
                    public void onCanceled(UrlRequest request, UrlResponseInfo info){
                        timeoutHandler.removeCallbacksAndMessages(null);
                        mainHandler.post(() -> taskCompletionSource.setException(new Exception("error_connection")));
                    }
                },
                executor
        );

        requestBuilder.setHttpMethod("POST");
        requestBuilder.addHeader("Content-Type", "application/json");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            mainHandler.post(() -> taskCompletionSource.setException(new Exception("error_unexpected_error")));
            return taskCompletionSource.getTask();
        }
        requestBuilder.setUploadDataProvider(
                UploadDataProviders.create(jsonBody.toString().getBytes()),
                executor
        );
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Signs out the user
    //It just deletes the user's data from local storage and sets the user object to null
    public void signOut() {
        user = null;
        localStorage.deleteUser();
    }

    //Retrieves the object containing the user's data
    public User getUser() {
        return user;
    }
}
