package com.example.airquality.client;

import java.io.IOException;

public interface TqsHttpClient {
    String get(String url) throws IOException;
}
