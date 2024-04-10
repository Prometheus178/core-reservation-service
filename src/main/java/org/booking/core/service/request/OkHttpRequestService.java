package org.booking.core.service.request;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log
public class OkHttpRequestService {

	public static final String HTTP_BOOKING_CORE_APP_8080 = "http://booking-core-app:8080";
	private final OkHttpClient client = new OkHttpClient();
	private final Gson gson = new Gson();

	public <T> T getRequest(String url, Class<T> tClass) {
		Request request = new Request.Builder().url(url).get().build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				ResponseBody body = response.body();
				if (body != null) {
					return gson.fromJson(body.string(), tClass);
				}
			} else {
				log.warning("Unexpected response: " + response.code());
				return null;
			}
		} catch (IOException e) {
			log.warning(e.getMessage());
		}
		return null;
	}
}
