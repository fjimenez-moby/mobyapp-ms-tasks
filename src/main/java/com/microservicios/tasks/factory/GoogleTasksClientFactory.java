package com.microservicios.tasks.factory;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.microservicios.tasks.exception.GoogleApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleTasksClientFactory {

    private static final String APPLICATION_NAME = "MobyApp Tasks Microservice";

    public Tasks createClient(String accessToken) {
        try {
            log.debug("Creating Google Tasks client with provided access token");

            AccessToken token = new AccessToken(accessToken, null);
            GoogleCredentials credentials = GoogleCredentials.create(token);

            return new Tasks.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create Google Tasks client: {}", e.getMessage(), e);
            throw new GoogleApiException("Failed to create Google Tasks client", e);
        }
    }
}
