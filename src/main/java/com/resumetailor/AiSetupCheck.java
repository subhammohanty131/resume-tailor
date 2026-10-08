package com.resumetailor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AiSetupCheck {

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println(
                    "GEMINI_API_KEY is missing from this run configuration."
            );
            return;
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        ObjectMapper mapper = new ObjectMapper();

        String configuredModel = System.getenv("GEMINI_MODEL");
        if (configuredModel == null || configuredModel.isBlank()) {
            configuredModel = "gemini-2.5-flash";
        }

        System.out.println("Configured model: " + configuredModel);
        System.out.println();
        System.out.println("Models supporting generateContent:");

        try {
            String pageToken = "";
            int count = 0;

            do {
                String endpoint =
                        "https://generativelanguage.googleapis.com"
                                + "/v1beta/models?pageSize=1000";

                if (!pageToken.isBlank()) {
                    endpoint += "&pageToken=" + URLEncoder.encode(
                            pageToken, StandardCharsets.UTF_8
                    );
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(30))
                        .header("x-goog-api-key", apiKey.trim())
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(
                                StandardCharsets.UTF_8
                        )
                );

                if (response.statusCode() != 200) {
                    System.out.println(
                            "Model-list request failed: HTTP "
                                    + response.statusCode()
                    );
                    return;
                }

                JsonNode root = mapper.readTree(response.body());

                for (JsonNode model : root.path("models")) {
                    for (JsonNode method :
                            model.path("supportedGenerationMethods")) {

                        if ("generateContent".equals(method.asText())) {
                            String name = model.path("name").asText();

                            if (name.startsWith("models/")) {
                                name = name.substring("models/".length());
                            }

                            System.out.println("- " + name);
                            count++;
                            break;
                        }
                    }
                }

                pageToken = root.path("nextPageToken").asText("");

            } while (!pageToken.isBlank());

            if (count == 0) {
                System.out.println(
                        "No generateContent models were returned."
                );
            }

            System.out.println();
            System.out.println("No resume content was sent.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Model-list request interrupted.");
        } catch (IOException e) {
            System.out.println(
                    "Unable to retrieve models. Check your connection."
            );
        }
    }
}