package org.wikipedia.dataclient.okhttp;

import android.support.annotation.NonNull;

import org.wikipedia.dataclient.okhttp.util.HttpUrlUtil;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * This interceptor strips away the `must-revalidate` directive from the Cache-Control header,
 * since this directive prevents OkHttp from returning cached responses.  This directive makes
 * sense for a web browser, which unconditionally wants the freshest content from the network,
 * but is not necessary for our app, which needs to be more permissive with allowing cached content.
 */
@SuppressWarnings("checkstyle:magicnumber")
class StripMustRevalidateResponseInterceptor implements Interceptor {
    @Override public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
        Response rsp = chain.proceed(chain.request());
        HttpUrl url = rsp.request().url();
        Response.Builder builder = rsp.newBuilder();

        if (HttpUrlUtil.isRestBase(url) || HttpUrlUtil.isMobileView(url)) {
            //Remove any Cache-Control directives from Server and override them with a max-stale directive
            //in order to cache all responses
            builder.removeHeader("Cache-Control");
            builder.addHeader("Cache-Control", "max-stale=" + 60 * 60 * 24 * 7);
        }
        // If we're saving the current response to the offline cache, then strip away the Vary header.
        if (OfflineCacheInterceptor.SAVE_HEADER_SAVE.equals(chain.request().header(OfflineCacheInterceptor.SAVE_HEADER))) {
            builder.removeHeader("Vary");
        }

        return builder.build();
    }

}
