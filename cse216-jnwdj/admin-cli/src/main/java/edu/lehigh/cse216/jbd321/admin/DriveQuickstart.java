package edu.lehigh.cse216.jbd321.admin;

import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "The Buzz";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying these
     * scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "quickstart-1573150860939-e68cb72c73cc.json";

    /**
     * Creates an authorized Credential object.
     * 
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // public static void main(String... args) throws IOException,
    // GeneralSecurityException {
    public static void listDoc() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        /*
         * final NetHttpTransport HTTP_TRANSPORT =
         * GoogleNetHttpTransport.newTrustedTransport(); Drive service = new
         * Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
         * .setApplicationName(APPLICATION_NAME).build();
         */

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        // String currentD =
        // System.getProperty("quickstart-1573150860939-e68cb72c73cc.json");
        // System.out.println("Current Dic " + currentD);
        InputStream in = Database.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleCredential cr = GoogleCredential.fromStream(
                (Database.class.getClassLoader().getResourceAsStream("quickstart-1573150860939-e68cb72c73cc.json")))
                .createScoped(SCOPES);
        GoogleCredential.Builder builder = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY).setServiceAccountScopes(SCOPES)
                .setServiceAccountId(cr.getServiceAccountId())
                .setServiceAccountPrivateKey(cr.getServiceAccountPrivateKey())
                .setServiceAccountPrivateKeyId(cr.getServiceAccountPrivateKeyId())
                .setTokenServerEncodedUrl(cr.getTokenServerEncodedUrl())
                .setServiceAccountId("radiant-spire-42063@quickstart-1573150860939.iam.gserviceaccount.com");
        // .setServiceAccountUser("cse216redjaguars@gmail.com");

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, builder.build())
                .setApplicationName(APPLICATION_NAME).build();

        // Print the names and IDs for up to 30 files.
        FileList result = service.files().list().setPageSize(30).setFields("nextPageToken, files(id, name)").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

    /**
     * Permanently delete a file, skipping the trash.
     *
     * @param service Drive API service instance.
     * @param fileId  ID of the file to delete.
     */
    public static void deleteFile(String fileId) throws IOException, GeneralSecurityException {
        // final NetHttpTransport HTTP_TRANSPORT =
        // GoogleNetHttpTransport.newTrustedTransport();
        // Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        // getCredentials(HTTP_TRANSPORT))
        // .setApplicationName(APPLICATION_NAME).build();

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = Database.class.getClassLoader()
                .getResourceAsStream("quickstart-1573150860939-e68cb72c73cc.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: ");
        }
        GoogleCredential cr = GoogleCredential.fromStream(
                (Database.class.getClassLoader().getResourceAsStream("quickstart-1573150860939-e68cb72c73cc.json")))
                .createScoped(SCOPES);
        GoogleCredential.Builder builder = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY).setServiceAccountScopes(SCOPES)
                .setServiceAccountId(cr.getServiceAccountId())
                .setServiceAccountPrivateKey(cr.getServiceAccountPrivateKey())
                .setServiceAccountPrivateKeyId(cr.getServiceAccountPrivateKeyId())
                .setTokenServerEncodedUrl(cr.getTokenServerEncodedUrl())
                .setServiceAccountId("radiant-spire-42063@quickstart-1573150860939.iam.gserviceaccount.com");
        // .setServiceAccountUser("cse216redjaguars@gmail.com");

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, builder.build())
                .setApplicationName(APPLICATION_NAME).build();
        try {
            service.files().delete(fileId).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }
}