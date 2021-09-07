package edu.lehigh.cse216.jbd321.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.net.URISyntaxException;
import java.net.URI;

import java.util.ArrayList;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;

public class Database {
    /**
     * The connection to the database. When there is no connection, it should be
     * null. Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * A prepared statement for getting all data in the database
     */
    private PreparedStatement mSelectAll;

    /**
     * A prepared statement for getting one row from the database
     */
    private PreparedStatement mSelectOne;

    /**
     * A prepared statement for deleting a row from the database
     */
    private PreparedStatement mDeleteOne;

    /**
     * A prepared statement for inserting into the database
     */
    private PreparedStatement mInsertOne;

    /**
     * A prepared statement for updating a single row in the database
     */
    private PreparedStatement mUpdateOne;

    /**
     * A prepared statement for creating the table in our database
     */
    private PreparedStatement mCreateTable;

    /**
     * A prepared statement for dropping the table in our database
     */
    private PreparedStatement mDropTable;

    /**
     * A prepared statement for creating the user database table
     */
    private PreparedStatement mCreateUserTable;
    private PreparedStatement mCreateMsgTable;
    private PreparedStatement mCreateCommentTable;
    private PreparedStatement mCreateVotesTable;
    private PreparedStatement mDropUserTable;
    private PreparedStatement mDropMsgTable;
    private PreparedStatement mDropCommentTable;
    private PreparedStatement mDropVotesTable;

    private PreparedStatement mSelectVote;
    private PreparedStatement mUpdateVote;

    private PreparedStatement uSelectAll;
    private PreparedStatement uSelectOne;
    private PreparedStatement uDeleteOne;
    private PreparedStatement uInsertOne;
    private PreparedStatement uInsertGoogleOne;
    private PreparedStatement uUpdatePassword;
    private PreparedStatement uSelectPasswordByUname;
    private PreparedStatement uSelectUidByUname;

    private PreparedStatement cSelectAll;
    private PreparedStatement cSelectOne;
    private PreparedStatement cDeleteOne;
    private PreparedStatement cDeleteMessageComments;
    private PreparedStatement cInsertOne;
    private PreparedStatement cUpdateComment;
    private PreparedStatement cSelectMidComments;

    private PreparedStatement vSelectAll;
    private PreparedStatement vInsertOne;
    private PreparedStatement vSelectVotes;
    private PreparedStatement vDeleteOne;
    private PreparedStatement vDeleteMessageVotes;
    private PreparedStatement vSearch;

    private PreparedStatement uAllMsg;
    private PreparedStatement cSelectUidComments;

    private PreparedStatement dInsertOne;
    private PreparedStatement dSelectOne;
    private PreparedStatement dDeleteOne;
    
    private PreparedStatement mFlagMessage;
    private PreparedStatement uBlockUser;
    private PreparedStatement mSelectFlag;

    /**
     * RowData is like a struct in C: we use it to hold data, and we allow direct
     * access to its fields. In the context of this Database, RowData represents the
     * data we'd see in a row.
     * 
     * We make RowData a static class of Database because we don't really want to
     * encourage users to think of RowData as being anything other than an abstract
     * representation of a row of the database. RowData and the Database are tightly
     * coupled: if one changes, the other should too.
     */
    public static class RowData {
        /**
         * The ID of this row of the database
         */
        int mMid;
        /**
         * The subject stored in this row
         */
        // String mSubject;

        /**
         * The message stored in this row
         */
        String mMessage;

        long mUid;
        String dFile;
        String dFileName;
        /**
         * Construct a RowData object by providing values for its fields
         */
        public RowData(int mid, String message, long uid, String file, String fileName) {
            mMid = mid;
            mMessage = message;
            mUid = uid;
            dFile = file;
            dFileName = fileName;
        }
    }

    public static class RowUData implements java.io.Serializable{
        // The userid stored in this row
        long uUid;

        // The userid stored in this row
        String uUsername;

        // The location stored in this row
        String uLocation;

        // The user password stored in this row
        String uPassword;

        // The salt for the passwords
        String uSalt;

        // The email stored in this row
        String uEmail;

        boolean uBlock;
        /**
         * Construct a RowUData object by providing values for its fields
         */
        public RowUData(long uid, String username, String location, String password, String salt, String email, boolean block) {
            uUid = uid;
            uUsername = username;
            uLocation = location;
            uPassword = password;
            uSalt = salt;
            uEmail = email;
            uBlock = block;
        }
    }

    public static class RowCData {
        // The commentid stored in this row
        int cCid;

        // The comment message stored in this row;
        String cComment;

        // The userid of the comment
        long cUid;

        // The messageid of the comment
        int cMid;

        /**
         * Construct a RowCData object by providing values for its fields
         */
        public RowCData(int cid, String comment, long uid, int mid) {
            cCid = cid;
            cComment = comment;
            cUid = uid;
            cMid = mid;
        }
    }

    public static class RowVData {
        // The mid in this row
        int vMid;

        // The uid in this row
        long vUid;

        // # of votes
        int votes;

        public RowVData(int mid, long uid, int numVotes) {
            vMid = mid;
            vUid = uid;
            votes = numVotes;
        }
    }

    /**
     * The Database constructor is private: we only create Database objects through
     * the getDatabase() method.
     */
    private Database() {
    }

    /**
     * Get a fully-configured connection to the database
     * 
     * @param ip   The IP address of the database server
     * @param port The port on the database server to which connection requests
     *             should be sent
     * @param user The user ID to use when connecting
     * @param pass The password to use when connecting
     * 
     * @return A Database object, or null if we cannot connect properly
     */
    static Database getDatabase(String db_url) {
        // Create an un-configured Database object
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        try {
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI(db_url);

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                    + "?sslmode=require";
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Unable to find postgresql driver");
            return null;
        } catch (URISyntaxException s) {
            System.out.println("URI Syntax Error");
            return null;
        }

        // Attempt to create all of our prepared statements. If any of these
        // fail, the whole getDatabase() call should fail
        try {
            // NB: we can easily get ourselves in trouble here by typing the
            // SQL incorrectly. We really should have things like "tblData"
            // as constants, and then build the strings for the statements
            // from those constants.

            // Note: no "IF NOT EXISTS" or "IF EXISTS" checks on table
            // creation/deletion, so multiple executions will cause an exception

            // Create and drop user & message tables
            db.mCreateUserTable = db.mConnection.prepareStatement("CREATE TABLE userData (uid SERIAL PRIMARY KEY,"
                    + " username VARCHAR(20) NOT NULL, location VARCHAR(50), email VARCHAR(30) NOT NULL, password VARCHAR(200) NOT NULL, salt VARCHAR(30) NOT NULL)");
            db.mDropUserTable = db.mConnection.prepareStatement("DROP TABLE userData");
            db.mCreateMsgTable = db.mConnection
                    .prepareStatement("CREATE TABLE messageData (mid SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL,"
                            + " uid SERIAL, FOREIGN KEY (uid) REFERENCES userData (uid))");
            db.mDropMsgTable = db.mConnection.prepareStatement("DROP TABLE messageData");

            db.mCreateCommentTable = db.mConnection.prepareStatement(
                    "CREATE TABLE commentsData (cid SERIAL PRIMARY KEY, commentMsg VARCHAR(500) NOT NULL,"
                            + " uid SERIAL, FOREIGN KEY (uid) REFERENCES userData (uid), mid SERIAL, FOREIGN KEY (mid) REFERENCES messageData (mid))");
            db.mDropCommentTable = db.mConnection.prepareStatement("DROP TABLE commentsData");

            db.mCreateVotesTable = db.mConnection.prepareStatement(
                    "CREATE TABLE votesData(mid int REFERENCES userData (mid), uid int REFERENCES messageData (uid), votes int, PRIMARY KEY(uid, mid))");
            db.mDropVotesTable = db.mConnection.prepareStatement("DROP TABLE votesData");

            // Standard CRUD operations
            db.mDeleteOne = db.mConnection.prepareStatement("DELETE FROM messageData WHERE mid = ?");
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO messageData VALUES (default, ?, ?, FALSE)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT mid, message, uid FROM messageData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from messageData WHERE mid=?");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE messageData SET message = ? WHERE mid = ?");
            db.mUpdateVote = db.mConnection.prepareStatement("UPDATE votesData SET votes = ? WHERE mid = ?");

            db.uDeleteOne = db.mConnection.prepareStatement("DELETE FROM userData WHERE uid = ?");
            db.uInsertOne = db.mConnection.prepareStatement("INSERT INTO userData VALUES (default, ?, ?, ?, ?, ?, FALSE)");
            db.uInsertGoogleOne = db.mConnection.prepareStatement("INSERT INTO userData VALUES (?, ?, ?, ?, ?, ?, FALSE)");
            db.uSelectAll = db.mConnection.prepareStatement("SELECT * FROM userData");
            db.uSelectOne = db.mConnection.prepareStatement("SELECT * from userData WHERE uid=?");
            db.uUpdatePassword = db.mConnection
                    .prepareStatement("UPDATE userData SET password = ?, salt = ? WHERE uid = ?");
            db.uSelectPasswordByUname = db.mConnection
                    .prepareStatement("SELECT password from userData WHERE username = ?");
            db.uSelectUidByUname = db.mConnection.prepareStatement("SELECT uid FROM userData WHERE username = ?");

            db.cSelectAll = db.mConnection.prepareStatement("SELECT cid, commentMsg, uid, mid FROM commentsData");
            db.cSelectOne = db.mConnection.prepareStatement("SELECT * from commentsData WHERE cid=?");
            db.cInsertOne = db.mConnection.prepareStatement("INSERT INTO commentsData VALUES (default, ?, ?, ?)");
            db.cDeleteOne = db.mConnection.prepareStatement("DELETE FROM commentsData WHERE cid = ?");
            db.cDeleteMessageComments = db.mConnection.prepareStatement("DELETE FROM commentsData WHERE mid = ?");
            db.cUpdateComment = db.mConnection.prepareStatement("UPDATE commentsData SET comment = ? WHERE cid = ?");
            db.cSelectMidComments = db.mConnection
                    .prepareStatement("SELECT cid, commentMsg, uid, mid FROM commentsData WHERE mid = ?");

            db.vSelectAll = db.mConnection.prepareStatement("SELECT mid, uid, votes FROM votesData");
            db.vInsertOne = db.mConnection.prepareStatement("INSERT INTO votesData VALUES (?, ?, ?)");
            db.vSelectVotes = db.mConnection.prepareStatement("SELECT votes FROM votesData WHERE mid = ?");
            db.vDeleteOne = db.mConnection.prepareStatement("DELETE FROM votesData WHERE mid = ? AND uid = ?");
            db.vDeleteMessageVotes = db.mConnection.prepareStatement("DELETE FROM votesData WHERE mid = ?");
            db.vSearch = db.mConnection.prepareStatement("SELECT * FROM votesData WHERE mid = ? AND uid = ?");

            db.uAllMsg = db.mConnection.prepareStatement("SELECT message FROM messageData WHERE uid = ?");
            db.cSelectUidComments = db.mConnection
                    .prepareStatement("SELECT commentmsg FROM commentsdata WHERE uid = ?");

            db.dInsertOne = db.mConnection.prepareStatement("INSERT INTO documentdata VALUES (default, ?, ?, ?, ?, ?, ?, ?, ?)");
            db.dSelectOne = db.mConnection.prepareStatement("SELECT dname, url, documentid FROM documentdata WHERE mid = ?");
            db.dDeleteOne = db.mConnection.prepareStatement("DELETE FROM documentdata WHERE mid = ?");

            db.mFlagMessage = db.mConnection.prepareStatement("UPDATE messageData SET flag = TRUE WHERE mid = ?");
            db.uBlockUser = db.mConnection.prepareStatement("UPDATE userData SET block = TRUE WHERE uid = ?");
            db.mSelectFlag = db.mConnection.prepareStatement("SELECT flag FROM messageData WHERE mid = ?");

        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }

    /**
     * Close the current connection to the database, if one exists.
     * 
     * NB: The connection will always be null after this call, even if an error
     * occurred during the closing operation.
     * 
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect() {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    /**
     * Insert a row into the database
     * 
     * @param subject The subject for this new row
     * @param message The message body for this new row
     * 
     * @return The number of rows that were inserted
     */
    int insertRow(String message, long uid) {
        int count = 0;
        try {
            mInsertOne.setString(1, message);
            mInsertOne.setLong(2, uid);
            mInsertOne.executeUpdate();
            count = 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    /**
     * Insert a File into the database

     */
    int insertDoc(long uId, int mId, String url, long size, String dname, String ownerName, String dId, String modify) {
        int count = 0;
        try {
            dInsertOne.setString(1, dname);
            dInsertOne.setString(2, ownerName);
            dInsertOne.setString(3, dId);
            dInsertOne.setString(4, modify);
            dInsertOne.setLong(5, size);
            dInsertOne.setString(6, url);
            dInsertOne.setLong(7, uId);
            dInsertOne.setInt(8, mId);
            count += dInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    String[] dSelectOne(int mId){
        String[] file = {"", ""};
        try{
            dSelectOne.setInt(1, mId);
            ResultSet rs = dSelectOne.executeQuery();
            if(rs.next()){
                file[0] = rs.getString("dname");
                file[1] = rs.getString("url");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Query the database for a list of all subjects and their IDs
     * 
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAll() {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowData(rs.getInt("mid"), rs.getString("message"), rs.getLong("uid"), "", ""));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowData selectOne(int mid) {
        RowData res = null;
        try {
            mSelectOne.setInt(1, mid);
            ResultSet rs = mSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowData(rs.getInt("mid"), rs.getString("message"), rs.getLong("uid"), "", "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * 
     * @return The number of rows that were deleted. -1 indicates an error.
     */
    int deleteRow(int mid) {
        int res = -1;
        try {
            cDeleteMessageComments.setInt(1, mid);
            cDeleteMessageComments.executeUpdate();

            vDeleteMessageVotes.setInt(1, mid);
            vDeleteMessageVotes.executeUpdate();

            dDeleteOne.setInt(1, mid);
            dDeleteOne.executeUpdate();

            mDeleteOne.setInt(1, mid);
            res = mDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the message for a row in the database
     * 
     * @param id      The id of the row to update
     * @param message The new message contents
     * 
     * @return The number of rows that were updated. -1 indicates an error.
     */
    int updateOne(int mid, String message) {
        int res = -1;
        try {
            mUpdateOne.setString(1, message);
            mUpdateOne.setInt(2, mid);
            res = mUpdateOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Create tblData. If it already exists, this will print an error
     */
    void createTable() {
        try {
            mCreateTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create user table
     */
    void createUserTable() {
        try {
            mCreateUserTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create message table
     */
    void createMsgTable() {
        try {
            mCreateMsgTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create comments table
     */
    void createCommentsTable() {
        try {
            mCreateCommentTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create votes table
     */
    void createVotesTable() {
        try {
            mCreateVotesTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove userdata and messagedata from the database. If it does not exist, this
     * will print an error.
     */
    void dropUserTable() {
        try {
            mDropUserTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropMsgTable() {
        try {
            mDropMsgTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropCommentTable() {
        try {
            mDropCommentTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropVotesTable() {
        try {
            mDropVotesTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropTable() {
        try {
            mDropTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Related function for user table */
    int insertURow(String username, String location, String email, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int count = 0;

        byte[] salt = getSalt();
        String finalSalt = salt.toString();
        String hashedPassword = generateStorngPasswordHash(password, salt);
        try {
            uInsertOne.setString(1, username);
            uInsertOne.setString(2, location);
            uInsertOne.setString(3, email);
            uInsertOne.setString(4, hashedPassword);
            uInsertOne.setString(5, finalSalt);
            count += uInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    ArrayList<RowUData> selectUAll() {
        ArrayList<RowUData> res = new ArrayList<RowUData>();
        try {
            ResultSet rs = uSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowUData(rs.getLong("uid"), rs.getString("username"), rs.getString("location"),
                        rs.getString("password"), rs.getString("salt"), rs.getString("email"), rs.getBoolean("block")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    int insertUGoogleRow(long uid)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int count = 0;

        ArrayList<RowUData> users = selectUAll();
        for(int i = 0; i < users.size(); i++){
            if (users.get(i).uUid == uid){
                return count;
            }
        }

        String placeholder = "PLACEHOLDER";

        try {
            uInsertGoogleOne.setLong(1, uid);
            uInsertGoogleOne.setString(2, placeholder);
            uInsertGoogleOne.setString(3, placeholder);
            uInsertGoogleOne.setString(4, placeholder);
            uInsertGoogleOne.setString(5, placeholder);
            uInsertGoogleOne.setString(6, placeholder);
            count += uInsertGoogleOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowUData selectUOne(long uid) {
        RowUData res = null;
        try {
            uSelectOne.setLong(1, uid);
            ResultSet rs = uSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowUData(rs.getLong("uid"), rs.getString("username"), rs.getString("location"),
                        rs.getString("password"), rs.getString("salt"), rs.getString("email"), rs.getBoolean("block"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }


    String selectUPassByUname(String username) {
        String res = null;
        try {
            uSelectPasswordByUname.setString(1, username);
            ResultSet rs = uSelectPasswordByUname.executeQuery();
            if (rs.next()) {
                res = rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    long selectUid(String username) {
        long res = -1;
        try {
            uSelectUidByUname.setString(1, username);
            ResultSet rs = uSelectUidByUname.executeQuery();
            if (rs.next()) {
                res = rs.getLong("uid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * @return The number of rows that were deleted. -1 indicates an error.
     */
    int deleteURow(long uid) {
        int res = -1;
        try {
            uDeleteOne.setLong(1, uid);
            res = uDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteVRow(int mid, long uid) {
        int res = -1;
        try {
            vDeleteOne.setInt(1, mid);
            vDeleteOne.setLong(2, uid);

            res = vDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    // Checks to see if a vote is already in the table
    boolean checkVote(int mid, long uid) {
        boolean hasVoted = false;
        try {
            vSearch.setInt(1, mid);
            vSearch.setLong(2, uid);

            ResultSet rs = vSearch.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasVoted;
    }

    int updateUpassword(long uid, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int res = -1;
        try {
            byte[] salt = getSalt();
            String finalSalt = salt.toString();
            String hashedPassword = generateStorngPasswordHash(password, salt);

            uUpdatePassword.setString(1, hashedPassword);
            uUpdatePassword.setString(2, finalSalt);
            uUpdatePassword.setLong(3, uid);
            res = uUpdatePassword.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateMVotes(int mid, int votes) {
        int res = -1;
        try {
            mUpdateVote.setInt(1, votes);
            mUpdateVote.setInt(2, mid);
            res = mUpdateVote.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Hash function for passwords
     */
    private static String generateStorngPasswordHash(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = password.toCharArray();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Salt function for hashing passwords
     */
    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * Helper Function for Hash function
     */
    private static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    // Relevant Comment Table Methods

    ArrayList<RowCData> selectCAll() {
        ArrayList<RowCData> res = new ArrayList<RowCData>();
        try {
            ResultSet rs = cSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowCData(rs.getInt("cid"), rs.getString("commentMsg"), rs.getLong("uid"), rs.getInt("mid")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    int insertCRow(String comment, long uid, int mid) {
        int count = 0;
        try {
            cInsertOne.setString(1, comment);
            cInsertOne.setLong(2, uid);
            cInsertOne.setInt(3, mid);
            count += cInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    RowCData selectCOne(int cid) {
        RowCData res = null;
        try {
            cSelectOne.setInt(1, cid);
            ResultSet rs = cSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowCData(rs.getInt("cid"), rs.getString("commentMsg"), rs.getLong("uid"), rs.getInt("mid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteCRow(int cid) {
        int res = -1;
        try {
            cDeleteOne.setInt(1, cid);
            res = cDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateCComment(int cid, String comment) {
        int res = -1;
        try {
            cUpdateComment.setString(1, comment);
            cUpdateComment.setInt(2, cid);
            res = cUpdateComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    ArrayList<RowCData> selectCommentsMid(int cMid) {
        ArrayList<RowCData> res = new ArrayList<RowCData>();
        try {
            cSelectMidComments.setInt(1, cMid);
            ResultSet rs = cSelectMidComments.executeQuery();
            while (rs.next()) {
                res.add(new RowCData(rs.getInt("cid"), rs.getString("commentMsg"), rs.getLong("uid"), rs.getInt("mid")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<RowVData> selectVAll() {
        ArrayList<RowVData> res = new ArrayList<RowVData>();
        try {
            ResultSet rs = vSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowVData(rs.getInt("mid"), rs.getLong("uid"), rs.getInt("votes")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    int insertVRow(int mid, long uid, int votes) {
        int count = 0;
        try {
            vInsertOne.setInt(1, mid);
            vInsertOne.setLong(2, uid);
            vInsertOne.setInt(3, votes);
            count += vInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    int sumVotes(int mid) {
        int sum = 0;
        try {
            vSelectVotes.setInt(1, mid);
            ResultSet rs = vSelectVotes.executeQuery();
            while (rs.next()) {
                sum = sum + rs.getInt("votes");
            }
            rs.close();
            return sum;
        } catch (SQLException e) {
            e.printStackTrace();
            return -99999999; // error return
        }
    }

    ArrayList<String> selectAllFromUser(long uid) {
        ArrayList<String> res = new ArrayList<String>();
        try {
            uAllMsg.setLong(1, uid);
            ResultSet rs = uAllMsg.executeQuery();
            while (rs.next()) {
                res.add(rs.getString("message"));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * this is used to select all comments from the user id jurecki
     */
    ArrayList<String> selectAllCom(long uid) {
        ArrayList<String> res = new ArrayList<String>();
        try {
            cSelectUidComments.setLong(1, uid);
            ResultSet rs = cSelectUidComments.executeQuery();
            while (rs.next()) {
                res.add(rs.getString("commentmsg"));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    void flagMessage(int mid){
        //get uid

        boolean res = false;
        try {
            mSelectFlag.setInt(1, mid);
            ResultSet rs = mSelectFlag.executeQuery();
            if (rs.next()) {
                res = rs.getBoolean("flag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long uid = selectOne(mid).mUid;
        try{
            if(!res){
                mFlagMessage.setInt(1, mid);
                mFlagMessage.executeUpdate();
            }
            else{
                deleteRow(mid);

                uBlockUser.setLong(1, uid);
                uBlockUser.executeUpdate();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}