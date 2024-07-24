package org.unipi.mpsp2343.smartalert;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.chromium.net.CronetEngine;
import org.chromium.net.UploadDataProviders;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipi.mpsp2343.smartalert.dto.EventListItem;
import org.unipi.mpsp2343.smartalert.dto.GetEventResponseDto;
import org.unipi.mpsp2343.smartalert.dto.PostDisasterEventDto;
import org.unipi.mpsp2343.smartalert.dto.SendAlertDto;
import org.unipi.mpsp2343.smartalert.dto.User;

import java.util.List;
import java.util.concurrent.Executor;

//Provides access to the remote database to any activities and services that need it
public class DbProvider {
    private static final long REQUEST_TIMEOUT_MS = 5000; //Request timeout
    private static final String API_URL = "http://192.168.1.2:8080/api/v1/"; //URL  of thg api
    private static final String USER_EVENT_ENDPOINT = "user/event"; //Endpoint for the users
    private static final String ADMIN_EVENT_ENDPOINT = "admin/event"; //Endpoint for the employees
    private final CronetEngine cronetEngine; //HTTP engine to make the requests
    private final Executor executor; //Executor to offload the requests
    private final Handler mainHandler; //Handler to post task results to the UI thread
    private final Handler timeoutHandler; //Handler to notify the listener of each request of a timeout
    private final Authentication auth;

    public DbProvider(CronetEngine cronetEngine, Executor executor, Authentication auth) {
        this.cronetEngine = cronetEngine;
        this.executor = executor;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        this.auth = auth;
    }

    //Posts the event the user wants to report
    public Task<String> postEvent(PostDisasterEventDto dto) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        //Sets the endpoint that will be hit
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                API_URL + USER_EVENT_ENDPOINT,
                new UrlRequestCallback() {
                    //No result is returned by the server, so the UI is just notified for the success of the request
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
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

        //Sets the request method to POST
        requestBuilder.setHttpMethod("POST");
        requestBuilder.addHeader("Content-Type", "application/json");
        User user = auth.getUser();
        //Adds the user's auth token to the request header
        if(user != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + user.getAuthToken());
        }
        String requestBody;
        try {
            requestBody =  new Gson().toJson(dto);
        } catch (Exception e) {
            mainHandler.post(() -> taskCompletionSource.setException(new Exception("error_unexpected_error")));
            return taskCompletionSource.getTask();
        }
        requestBuilder.setUploadDataProvider(
                UploadDataProviders.create(requestBody.getBytes()),
                executor
        );
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        //Cancels the request of timeout
        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Retrieves a lightweight list of the OPEN events
    public Task<List<EventListItem>> getEventList(){
        TaskCompletionSource<List<EventListItem>> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                API_URL + ADMIN_EVENT_ENDPOINT + "/list",
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        //On success, it parses the response as a list of EventListItem objects
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
                            List<EventListItem> results = new Gson().fromJson(getResponse(), new TypeToken<List<EventListItem>>(){}.getType());
                            mainHandler.post(() -> taskCompletionSource.setResult(results));
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

        requestBuilder.setHttpMethod("GET");
        requestBuilder.addHeader("Content-Type", "application/json");
        User user = auth.getUser();
        if(user != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + user.getAuthToken());
        }
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Retrieves the details of a single event, based on event id. The event id is passed
    //as a parameter to the function
    public Task<GetEventResponseDto> getEvent(String eventId){
        TaskCompletionSource<GetEventResponseDto> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                API_URL + ADMIN_EVENT_ENDPOINT + "/" + eventId, //Adds the event id to the url
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
                            //On success it parses the response into GetEventResponseDto object
                            GetEventResponseDto result = new Gson().fromJson(getResponse(), new TypeToken<GetEventResponseDto>(){}.getType());
                            mainHandler.post(() -> taskCompletionSource.setResult(result));
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

        requestBuilder.setHttpMethod("GET");
        requestBuilder.addHeader("Content-Type", "application/json");
        User user = auth.getUser();
        if(user != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + user.getAuthToken());
        }
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Rejects an event
    public Task<String> rejectEvent(String eventId){
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                //Specifies the rejection endpoint  and adds the event id to the url
                API_URL + ADMIN_EVENT_ENDPOINT + "/reject/" + eventId,
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
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

        requestBuilder.setHttpMethod("PUT");
        requestBuilder.addHeader("Content-Type", "application/json");
        User user = auth.getUser();
        if(user != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + user.getAuthToken());
        }
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Confirms and event
    public Task<String> confirmEvent(String eventId){
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                API_URL + ADMIN_EVENT_ENDPOINT + "/confirm/" + eventId,
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
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

        requestBuilder.setHttpMethod("PUT");
        requestBuilder.addHeader("Content-Type", "application/json");
        User user = auth.getUser();
        if(user != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + user.getAuthToken());
        }
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

    //Checks the server for new disaster alerts for the current user. Takes the user's location as parameters
    public Task<List<SendAlertDto>> checkForAlerts(double lat, double lon){
        TaskCompletionSource<List<SendAlertDto>> taskCompletionSource = new TaskCompletionSource<>();
        //<editor-fold name="Setup request">
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                //Specifies the alerts endpoint and adds the user's location as query parameters
                API_URL + USER_EVENT_ENDPOINT + "/alerts?lat=" + lat + "&lon=" + lon,
                new UrlRequestCallback() {
                    @Override
                    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        if(info.getHttpStatusCode() == 200) {
                            //Parses the retrieved alerts as a list of SendAlertDto objects
                            List<SendAlertDto> results = new Gson().fromJson(getResponse(), new TypeToken<List<SendAlertDto>>(){}.getType());
                            mainHandler.post(() -> taskCompletionSource.setResult(results));
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

        requestBuilder.setHttpMethod("GET");
        requestBuilder.addHeader("Content-Type", "application/json");
        User user = auth.getUser();
        if(user != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + user.getAuthToken());
        }
        //</editor-fold>

        UrlRequest request = requestBuilder.build();
        request.start();

        timeoutHandler.postDelayed(() -> {
            request.cancel();
        }, REQUEST_TIMEOUT_MS);

        return taskCompletionSource.getTask();
    }

}
