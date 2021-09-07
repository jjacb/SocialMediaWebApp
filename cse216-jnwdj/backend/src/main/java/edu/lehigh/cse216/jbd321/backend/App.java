package edu.lehigh.cse216.jbd321.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import com.google.gson.*;
import java.util.Map;
import spark.Spark;
import java.util.ArrayList;
import java.util.HashMap;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;


import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.lang.InterruptedException;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

// Import Google's JSON library
//import com.google.api.client.googleapis.auth.oauth2;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Collection;
import java.util.Collections;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        switch (uploader.getUploadState()) {
        case INITIATION_STARTED:
            System.out.println("Initiation Started");
            break;
        case INITIATION_COMPLETE:
            System.out.println("Initiation Completed");
            break;
        case MEDIA_IN_PROGRESS:
            System.out.println("Upload in progress");
            System.out.println("Upload percentage: " + uploader.getProgress());
            break;
        case MEDIA_COMPLETE:
            System.out.println("Upload Completed!");
            break;
        case NOT_STARTED:
            System.out.println("Upload Not Started!");
            break;
        }
    }
}
class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {
    @Override
    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
        switch (downloader.getDownloadState()) {
            case MEDIA_IN_PROGRESS:
                System.out.println("Download is in progress: " + downloader.getProgress());
                break;
            case MEDIA_COMPLETE:
                System.out.println("Download is Complete!");
                break;
        }
    }
}
public class App {


    private static Drive drive;
    private static FileDataStoreFactory dataStoreFactory;
    private static String filePath = new java.io.File("").getAbsolutePath();
    private static String DIR_FOR_DOWNLOADS = filePath + "/temp";
    private static final String APPLICATION_NAME = "jbd321";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/app");
    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     * 
     * @envar The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     * 
     * @returns The best answer we could come up with for a value for envar
     */

    static int getIntFromEnv(String envar, int defaultVal) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get(envar) != null) {
            return Integer.parseInt(processBuilder.environment().get(envar));
        }
        return defaultVal;
    }

    private static File uploadFile(boolean useDirectUpload, java.io.File upload_file) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setTitle(upload_file.getName());
    
        FileContent mediaContent = new FileContent("image/jpeg", upload_file);
    
        Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(useDirectUpload);
        uploader.setProgressListener(new FileUploadProgressListener());
        return insert.execute();
    }
    private static void downloadFile(boolean useDirectDownload, File uploadedFile)
        throws IOException {
        // create parent directory (if necessary)
        java.io.File parentDir = new java.io.File(DIR_FOR_DOWNLOADS);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Unable to create parent directory");
        }
        OutputStream out = new FileOutputStream(new java.io.File(parentDir, uploadedFile.getTitle()));

        MediaHttpDownloader downloader =
            new MediaHttpDownloader(httpTransport, drive.getRequestFactory().getInitializer());
        downloader.setDirectDownloadEnabled(useDirectDownload);
        downloader.setProgressListener(new FileDownloadProgressListener());
        downloader.download(new GenericUrl(uploadedFile.getDownloadUrl()), out);
    }

    public static void main(String[] args) {

        String appName = "thebuzz";
        String clientSecret = "yRr5fLi7WJW9RD1YiT9DVlGY";
        String CLIENT_ID = "151775305975-4lh2ge5urppr6q4rrv4alsj4qvecldci.apps.googleusercontent.com";

        Credential credential = null;
        try{
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId("radiant-spire-42063@quickstart-1573150860939.iam.gserviceaccount.com")
                .setServiceAccountPrivateKeyFromP12File(new java.io.File(filePath + "/target/classes/quickstart-1573150860939-25e1e016bfd4.p12"))
                .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
                .build();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        String oauth2Endpoint = "https://accounts.google.com/o/oauth2/v2/auth";

        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        // get the Postgres configuration from the environment
        Map<String, String> env = System.getenv();

        String db_url = env.get("DATABASE_URL");
        // Get a fully-configured connection to the database, or exit
        // immediately
        Database db = Database.getDatabase(db_url);

        if (db == null)
            return;

        // gson provides us with a way to turn JSON into objects, and objects
        // into JSON.
        //
        // NB: it must be final, so that it can be accessed from our lambdas
        //
        // NB: Gson is thread-safe. See
        // https://stackoverflow.com/questions/10380835/is-it-ok-to-use-gson-instance-as-a-static-field-in-a-model-bean-reuse
        final Gson gson = new Gson();


        List<InetSocketAddress> servers = AddrUtil.getAddresses("mc5.dev.ec2.memcachier.com:11211".replace(",", " "));

        AuthInfo authInfo = AuthInfo.plain("BDD34E", "9ACB1C3137183E16D0014FF6183A2268");
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(servers);
        // Configure SASL auth for each server
        for(InetSocketAddress server : servers) {
            builder.addAuthInfo(server, authInfo);
        }

        // Use binary protocol
        builder.setCommandFactory(new BinaryCommandFactory());
        // Connection timeout in milliseconds (default: )
        builder.setConnectTimeout(1000);
        // Reconnect to servers (default: true)
        builder.setEnableHealSession(true);
        // Delay until reconnect attempt in milliseconds (default: 2000)
        builder.setHealSessionInterval(2000);

        MemcachedClient mcInit = null;
        try {
            mcInit = builder.build();
            try {
              mcInit.set("foo", 0, "bar");
              String val = mcInit.get("foo");
              System.out.println(val);
            } catch (TimeoutException te) {
              System.err.println("Timeout during set or get: " +
                                 te.getMessage());
            } catch (InterruptedException ie) {
              System.err.println("Interrupt during set or get: " +
                                 ie.getMessage());
            } catch (MemcachedException me) {
              System.err.println("Memcached error during get or set: " +
                                 me.getMessage());
            }
            } catch (IOException ioe) {
              System.err.println("Couldn't create a connection to MemCachier: " +
                                 ioe.getMessage());
          }
        final MemcachedClient mc = mcInit;



        // Set up the location for serving static files
        // Set up the location for serving static files. If the STATIC_LOCATION
        // environment variable is set, we will serve from it. Otherwise, serve
        // from "/web"
        String static_location_override = System.getenv("STATIC_LOCATION");
        if (static_location_override == null) {
            Spark.staticFileLocation("/web");
        } else {
            Spark.staticFiles.externalLocation(static_location_override);
        }

        // Set up a route for serving the main page
        Spark.get("/", (req, res) -> {
            res.redirect("/login.html");
            return "";
        });
        // GET route that returns all message titles and Ids. All we do is get
        // the data, embed it in a StructuredResponse, turn it into JSON, and
        // return it. If there's no data, we return "[]", so there's no need
        // for error handling.
        Spark.post("/getmessages", (request, response) -> {
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }
            ArrayList<Database.RowData> msgs = db.selectAll();
            for(int i = 0; i < msgs.size(); i++){
                int mId = msgs.get(i).mMid;
                String file = "";
                String fileName = "";
                String[] files = db.dSelectOne(mId);
                if(files != null && !files[0].equals("")){
                    fileName = files[0];
                    File testFile = new File();
                    testFile.setTitle(files[0]);
                    testFile.setDownloadUrl(files[1]);
                    downloadFile(false, testFile);

                    java.io.File downloaded = new java.io.File(DIR_FOR_DOWNLOADS + "/" + fileName);
                    file = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(downloaded));
                    msgs.get(i).dFile = file;
                    msgs.get(i).dFileName = fileName;
                }
            }
            return gson.toJson(new StructuredResponse("ok", null, msgs));
        });

        // GET route that returns everything for a single row in the DataStore.
        // The ":id" suffix in the first parameter to get() becomes
        // request.params("id"), so that we can get the requested row ID. If
        // ":id" isn't a number, Spark will reply with a status 500 Internal
        // Server Error. Otherwise, we have an integer, and the only possible
        // error is that it doesn't correspond to a row with data.
        Spark.post("/getmessages/:id", (request, response) -> {
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            Database.RowData data = db.selectOne(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // POST route for adding a new element to the DataStore. This will read
        // JSON from the body of the request, turn it into a SimpleRequest
        // object, extract the title and message, insert them, and return the
        // ID of the newly created row.
        Spark.post("/messages", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            // describes the error.
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            // NB: createEntry checks for null title and message
        
            db.insertRow(req.mMessage, req.mUid);
            ArrayList<Database.RowData> findMid = db.selectAll();
            int newId = 0;
            for(int i = 0; i < findMid.size(); i++){
                if(newId < findMid.get(i).mMid){
                    newId = findMid.get(i).mMid;
                }
            }

            if(!req.dFile.equals("") && req.dFile != null && req.dFileName != null && !req.dFileName.equals("")){
                byte[] fileBytes = Base64.getDecoder().decode(req.dFile);
                java.io.File upload = new java.io.File(filePath + "/temp/" + req.dFileName);
                FileUtils.writeByteArrayToFile(upload, fileBytes);
                File uploadedFile = uploadFile(false, upload);
                
                db.insertDoc(
                req.mUid, 
                newId, 
                uploadedFile.getDownloadUrl(), 
                uploadedFile.getFileSize(), 
                uploadedFile.getTitle(), 
                uploadedFile.getOwnerNames().get(0), 
                uploadedFile.getId(), 
                uploadedFile.getModifiedDate().toString()
                );
            }

            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // PUT route for updating a row in the DataStore. This is almost
        // exactly the same as POST
        Spark.put("/messages/:id", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            int result = -1;
            
            long uid = mc.get(req.session);

            if(db.selectOne(idx).mUid == uid)
                db.updateOne(idx, req.mMessage);
            else
                return gson.toJson(new StructuredResponse("error" + idx, "message: " + idx + " does not belong to you", null));

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, db.selectOne(idx)));
            }
        });

        // DELETE route for removing a row from the DataStore
        Spark.delete("/messages/:id", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            // NB: we won't concern ourselves too much with the quality of the
            // message sent on a successful delete

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            long uid = mc.get(req.session);

            int result = -1;
            if(db.selectOne(idx).mUid == uid)
                result = db.deleteRow(idx);
            else
                return gson.toJson(new StructuredResponse("error" + idx, "message: " + idx + " does not belong to you", null));


            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("deleted row" + idx, null, null));
            }
        });

        // GET route that returns all USER INFORMATION.
        Spark.post("/getusers", (request, response) -> {
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            return gson.toJson(new StructuredResponse("ok", null, db.selectUAll()));
        });

        // GET route that returns everything for a single row in the user database.
        Spark.post("/getusers/:id", (request, response) -> {
            long idx = Long.parseLong(request.params("id"));

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            Database.RowUData data = null;
            if(idx < 1000){
                data = db.selectUOne(idx);
            }
            else{
                data = mc.get("" + idx);
            }

            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // POST route for adding a new element to the user database.
        Spark.post("/users", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            // describes the error.
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            // NB: createEntry checks for null username and location
            int newId = db.insertURow(req.uUsername, req.uLocation, req.uEmail, req.uPassword);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // Put route for updating user's password
        Spark.put("/users/:id/password", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            long idx = Long.parseLong(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            int result = db.updateUpassword(idx, req.uPassword);
            if (result == -1) {
                return gson
                        .toJson(new StructuredResponse("error", "unable to update user " + idx + "'s password", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, db.selectUOne(idx)));
            }
        });

        // DELETE route for removing a row from the user
        Spark.delete("/users/:id", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            long idx = Long.parseLong(request.params("id"));

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            // NB: we won't concern ourselves too much with the quality of the
            // message sent on a successful delete
            int result = db.deleteURow(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        // GET route that returns all comment data
        Spark.post("/getcomments", (request, response) -> {
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            return gson.toJson(new StructuredResponse("ok", null, db.selectCAll()));
        });

        // POST route for adding a new element to the comment database.
        Spark.post("/comments", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            // describes the error.
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            // NB: createEntry checks for null username and location
            int newId = db.insertCRow(req.cComment, req.cUid, req.cMid);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // GET route that returns everything for a single row in the comment database.
        Spark.post("/getcomments/:id", (request, response) -> {
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            int idx = Integer.parseInt(request.params("id"));
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            Database.RowCData data = db.selectCOne(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // DELETE route for removing a row from the comment table
        Spark.delete("/comments/:id", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            // NB: we won't concern ourselves too much with the quality of the
            // message sent on a successful delete
            int result = db.deleteCRow(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        // PUT route for updating a row in the DataStore. This is almost
        // exactly the same as POST
        Spark.put("/comments/:id", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            int result = db.updateCComment(idx, req.cComment);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, db.selectOne(idx)));
            }
        });

        // GET route that returns comments for a specific message
        Spark.post("/getmessages/:id/comments", (request, response) -> {
            int idx = Integer.parseInt(request.params("id"));

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            ArrayList<Database.RowCData> data = db.selectCommentsMid(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // Routes for Votes Table

        // GET route that returns all VOTES INFORMATION.
        Spark.post("/getvotes", (request, response) -> {
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            return gson.toJson(new StructuredResponse("ok", null, db.selectVAll()));
        });

        // POST route for adding a new element to the comment database.
        Spark.post("/votes", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            // describes the error.
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            boolean hasVoted = false;
            hasVoted = db.checkVote(req.vMid, req.vUid);

            if (hasVoted) {
                db.deleteVRow(req.vMid, req.vUid);
            }

            // NB: createEntry checks for null username and location
            int newId = db.insertVRow(req.vMid, req.vUid, req.votes);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // GET route that returns number of votes for a specific message
        Spark.post("/getmessages/:id/votes", (request, response) -> {
            int idx = Integer.parseInt(request.params("id"));

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            int data = db.sumVotes(idx);
            if (data == -99999999) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // Put route for updating user's votes
        Spark.put("/messages/:id/votes", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            int result = db.updateMVotes(idx, req.votes);
            if (result == -1) {
                return gson
                        .toJson(new StructuredResponse("error", "unable to update message " + idx + "'s votes", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, db.selectUOne(idx)));
            }
        });

        // get request
        Spark.post("/userLogin", (request, response) -> {
            // do we get the access token from the request?
            // i theorize theat
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
                    // Specify the CLIENT_ID of the app that accesses the backend:
                    .setAudience(Collections.singletonList(CLIENT_ID)).build();

            // (Receive idTokenString by HTTPS POST)
            // *********************************************** */
            // GoogleIdToken idToken = verifier.verify(req.idTokenString);
            // is this the right way to recieve the id token?
            // i added a string id_token in simplerequest.java to post to that
            GoogleIdToken idToken = verifier.verify(req.id_token);

            if (idToken != null) {
                Payload payload = idToken.getPayload();
                if(!payload.getHostedDomain().equals("lehigh.edu")){
                    return gson.toJson(new StructuredResponse("error", "non-lehigh user", null));
                }
                // Print user identifier
                String userId = payload.getSubject();
                System.out.println("User ID: " + userId);
                // Get profile information from payload
                String email = payload.getEmail();
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                // Use or store profile information
                // ...

                String uid = (String) payload.get("sub");
                uid = uid.substring(7);
                long uidNum = Long.parseLong(uid);

                //check if user is blocked
                Database.RowUData row = db.selectUOne(uidNum);
                if(row.uBlock == true){
                    return gson.toJson(new StructuredResponse("error", "This user is blocked", null));
                }

                db.insertUGoogleRow(uidNum);
                Database.RowUData data = new Database.RowUData(uidNum, name, locale, "", "", email, false);
                mc.set("" + uidNum, 3600, data);

                String sessionId = sessionGenerator(10);
                mc.set(sessionId, 3600, uidNum);
                return gson.toJson(new StructuredResponse("ok", "" + uidNum, sessionId));
            } else {
                return gson.toJson(new StructuredResponse("error", "invalid login credentials", null));
            }

        });

        Spark.post("/getusers/:id/comments", (request, response) -> {
            long cid = Long.parseLong(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");
            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }
            ArrayList<String> data = db.selectAllCom(cid);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", cid + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        Spark.post("/getusers/:id/messages", (request, response)  ->{
            // oauth
            long uid = Long.parseLong(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");
            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }
            ArrayList<String> data = db.selectAllFromUser(uid);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", uid + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }

        });

        Spark.post("/messages/:id/flag", (request, response)  ->{
            int idx = Integer.parseInt(request.params("id"));
            
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            response.status(200);
            response.type("application/json");

            if (mc.get(req.session) == null || mc.get(req.session).equals("")){
                return gson.toJson(new StructuredResponse("error", "Missing or expired Session", null));
            }

            db.flagMessage(idx);

            return gson.toJson(new StructuredResponse("ok", null, null));
        });

    }

    // Helper Funtions to Verify Login Credentials
    private static boolean validatePassword(String originalPassword, String storedPassword)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        int diff = hash.length ^ testHash.length;
        for (int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private static String sessionGenerator(int count) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
