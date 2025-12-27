package com.alcw.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private static final String APPLICATION_NAME = "ALC Contact Form";
    private static final com.google.api.client.json.JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/spreadsheets");

    @Value("${google.sheets.id}")
    private String spreadsheetId;

    @Value("${google.sheets.range}")
    private String range;

    @Value("${google.credentials}")
    private String base64Credentials;

    @SneakyThrows
    @Override
    public void writeToSheet(String name, String email, String subject, String message, String fileUrl) {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Decode credentials
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes))
                .createScoped(SCOPES);

        Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Prepare data
        List<Object> rowData = List.of(
                new Date().toString(),
                name,
                email,
                subject,
                message,
                fileUrl != null ? fileUrl : "N/A"
        );

        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(rowData));

        // Write to sheet
        service.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}