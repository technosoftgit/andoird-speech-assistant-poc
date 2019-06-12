package com.technosoft.rylee.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://us-central1-riley-daf1e.cloudfunctions.net";

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
