package com.example.selenium.common.util.google;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SheetsServiceUtil {
//https://www.baeldung.com/google-sheets-java-client
    // OAuth 2.0 연동시 지정한 OAuth 2.0 클라이언트 이름
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final int SHEET_TO_COPY = 0;

    public static Sheets getSheetService() throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }





    public static String create(String sheetNm) throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();

        Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(sheetNm));

        spreadsheet = service.spreadsheets().create(spreadsheet).execute();

        return spreadsheet.getSpreadsheetId();
    }


}
