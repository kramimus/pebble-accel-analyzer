package com.whitneyindustries.acceldump.util;

import static org.mockito.Mockito.*;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicStatusLine;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class WrappedMockHttpClient {
    @Mock
    private HttpClient mockedClient;

    @Mock
    private HttpResponse response;

    public WrappedMockHttpClient() {
        MockitoAnnotations.initMocks(this);
    }

    public HttpClient getHttpClient() {
        return mockedClient;
    }

    public void setStatusCode(int code) throws Exception {
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("", 0, 0), code, ""));
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(response);

    }
}
