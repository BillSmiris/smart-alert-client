package org.unipi.mpsp2343.smartalert;

import android.os.Looper;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;

//Abstract class for the callback functions of an http request in the app.
//Overrides most  class functions that must work the same way in the request.
//The developer has to only override the onSucceeded and onCancelled callbacks
//to include implementation proper for handling the result of each request.
abstract class UrlRequestCallback extends UrlRequest.Callback {
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(102400);
    private final StringBuilder responseBuilder = new StringBuilder();

    @Override
    public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) {
        request.followRedirect();
    }

    @Override
    public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {
        request.read(byteBuffer);
    }

    //Fills a buffer with the response from the server
    @Override
    public void onReadCompleted(UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {
        byteBuffer.flip();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        responseBuilder.append(new String(bytes, StandardCharsets.UTF_8));
        byteBuffer.clear();
        request.read(byteBuffer);
        byteBuffer.clear();
    }

    @Override
    public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {
        this.onCanceled(request, info);
    }

    public String getResponse() {
        return responseBuilder.toString();
    }
}
