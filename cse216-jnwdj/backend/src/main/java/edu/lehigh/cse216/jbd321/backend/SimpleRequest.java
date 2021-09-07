package edu.lehigh.cse216.jbd321.backend;

/**
 * SimpleRequest provides a format for clients to present title and message
 * strings to the server.
 * 
 * NB: since this will be created from JSON, all fields must be public, and we
 * do not need a constructor.
 */
public class SimpleRequest {
    /**
     * The message being provided by the client.
     */
    public String mMessage;

    /**
     * The like message being provided by the client.
     */
    public int votes;

    /**
     * The user id of message being provided by the client.
     */
    public long mUid;

    /**
     * The username being provided by the client.
     */
    public String uUsername;

    /**
     * The location of user being provided by the client.
     */
    public String uLocation;

    /**
     * The password of user being provided by the client.
     */
    public String uPassword;

    /**
     * The email being provided by the client
     */
    public String uEmail;

    /**
     * The comment message
     */
    public String cComment;

    /**
     * The uid for the comment table
     */
    public long cUid;

    /**
     * The mid for the comment table
     */
    public int cMid;

    /**
     * The uid for votes table
     */
    public long vUid;

    /**
     * The mid for votes table
     */
    public int vMid;

    /**
     * Base64 encoded img or pdf file
     */
    public String dFile;

    public String dFileName;

    /**
     * The session id
     */
    public String session;

    public String id_token;
}
