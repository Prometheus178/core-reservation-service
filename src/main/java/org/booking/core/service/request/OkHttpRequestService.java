package org.booking.core.service.request;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OkHttpRequestService {
	private final OkHttpClient client = new OkHttpClient();

	public String getRequest(String url) throws IOException {
		Request request = new Request.Builder().url(url).get().build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				return response.body().string();
			} else {
				throw new IOException("Unexpected response: " + response.code());
			}
		}
	}
}
