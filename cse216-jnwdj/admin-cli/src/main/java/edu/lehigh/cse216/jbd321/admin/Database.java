package edu.lehigh.cse216.jbd321.admin;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

//import com.google.api.client.util.DateTime;

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
    private PreparedStatement mDropUserTable;
    private PreparedStatement mDropMsgTable;
    private PreparedStatement mSelectVote;
    private PreparedStatement mUpdateVote;
    private PreparedStatement mUpdateMsgComment;
    private PreparedStatement mInsertOneComment;

    private PreparedStatement mCreateVotesTable;
    private PreparedStatement mCreateCommentsTable;
    private PreparedStatement mDropVotesTable;
    private PreparedStatement mDropCommentsTable;

    private PreparedStatement mCreateDocTable;
    private PreparedStatement mDropDocTable;

    private PreparedStatement mCreatexTable;
    private PreparedStatement mDropxTable;
    private PreparedStatement xNewCol;

    private PreparedStatement mInsertOneVote;
    private PreparedStatement uSelectAll;
    private PreparedStatement uSelectOne;
    private PreparedStatement uDeleteOne;
    private PreparedStatement uInsertOne;
    private PreparedStatement uUpdateName;
    private PreparedStatement uUpdateLoc;
    private PreparedStatement uSelectName;
    private PreparedStatement uSelectLoc;

    private PreparedStatement uUpdateEmail;
    private PreparedStatement uUpdatePass;
    private PreparedStatement uSelectEmail;
    private PreparedStatement uSelectPass;
    private PreparedStatement dSelectAll;
    private PreparedStatement dSelectOne;
    private PreparedStatement dSelectName;
    private PreparedStatement dSelectMInfo;
    private PreparedStatement dSelectDName;
    private PreparedStatement dDeleteOutdate;
    private PreparedStatement dSelectDsize;

    private PreparedStatement uBlockCol;
    private PreparedStatement mFlagCol;

    private PreparedStatement uSelectBlock;
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
         * The message stored in this row
         */
        String mMessage;
        /**
         * The like message stored in row
         */
        int mVotes;
        Long mUid;

        /**
         * ********************************************************* ADD COMMENTMSG UP
         * HERE Construct a RowData object by providing values for its fields
         */
        public RowData(int mid, String message, int votes, Long uid) {
            mMid = mid;
            mMessage = message;
            mVotes = votes;
            mUid = uid;

        }
    }

    public static class RowUData {
        // The userid stored in this row
        Long uUid;
        // The userid stored in this row
        String uUsername;
        // The location stored in this row
        String uLocation;
        String uEmail;
        String uPassword;
        String uSalt;

        /**
         * Construct a RowUData object by providing values for its fields
         */
        public RowUData(Long uid, String username, String location, String email, String password, String salt) {
            uUid = uid;
            uUsername = username;
            uLocation = location;
            uEmail = email;
            uPassword = password;
            uSalt = salt;
        }
    }

    public static class RowCData {
        // comment id
        int ccid;
        // comment
        String ccomment;
        // userid, messageid
        Long cuid;
        int cmid;

        /**
         * Construct a RowUData object by providing values for its fields
         */
        public RowCData(int cid, String comment, Long uid, int mid) {
            ccid = cid;
            ccomment = comment;
            cuid = uid;
            cmid = mid;
        }
    }

    public static class RowDData {
        // comment id
        int ddid;
        // comment
        String name;
        String owner;
        String dDocId;
        String dModifty;
        String durl;

        long dDsize;
        Long duid;
        int dmid;

        /**
         * Construct a RowUData object by providing values for its fields
         */
        public RowDData(int did, String dname, String ownname, String docId, String dmodify, long dsize, String url,
                Long uid, int mid) {
            ddid = did;
            name = dname;
            duid = uid;
            dmid = mid;
            dDocId = docId;
            dModifty = dmodify;
            durl = url;
            dDsize = dsize;
            owner = ownname;
        }
    }

    public static class RowAData {
        // The userid stored in this row
        Long uUid;
        // The userid stored in this row
        String uUsername;

        public RowAData(Long uid, String username) {
            uUid = uid;
            uUsername = username;
        }
    }

    public static class RowMData {
        int mMid;
        String mMessage;

        public RowMData(int mid, String message) {
            mMid = mid;
            mMessage = message;
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

            // Create and drop user & message & vote tables
            db.mCreateUserTable = db.mConnection.prepareStatement("CREATE TABLE userData (uid BIGSERIAL PRIMARY KEY,"
                    + " username VARCHAR(20) NOT NULL, location VARCHAR(50), email VARCHAR(30) NOT NULL, password VARCHAR(30) NOT NULL, salt VARCHAR(30) not null)");

            db.mCreateMsgTable = db.mConnection
                    .prepareStatement("CREATE TABLE messageData (mid SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL,"
                            + " uid SERIAL, FOREIGN KEY (uid) REFERENCES userData (uid)) ");

            db.mDropUserTable = db.mConnection.prepareStatement("DROP TABLE userData");
            db.mDropMsgTable = db.mConnection.prepareStatement("DROP TABLE messageData");

            db.mCreateVotesTable = db.mConnection.prepareStatement(
                    "CREATE TABLE votesData (mid int REFERENCES messageData (mid), uid bigint REFERENCES userData (uid), votes int, PRIMARY KEY(mid, uid))");

            db.mCreateCommentsTable = db.mConnection.prepareStatement(
                    "CREATE TABLE commentsData (cid SERIAL PRIMARY KEY,  commentMsg VARCHAR(500) NOT NULL,"
                            + " uid SERIAL, FOREIGN KEY (uid) REFERENCES userData (uid), mid SERIAL, FOREIGN KEY (mid) REFERENCES messageData (mid)) ");

            db.mDropVotesTable = db.mConnection.prepareStatement("DROP TABLE votesData");
            db.mDropCommentsTable = db.mConnection.prepareStatement("DROP TABLE commentsData");

            // db.mCreateCommentsTable = db.mConnection.prepareStatement("CREATE TABLE
            // commentsData (cid SERIAL PRIMARY KEY, commentMsg VARCHAR(500) NOT NULL, uid
            // int REFERENCES userData , mid REFERENCES messageData) ");

            /* create document table for google drive activity */
            db.mCreateDocTable = db.mConnection.prepareStatement(
                    "CREATE TABLE documentData (did SERIAL PRIMARY KEY, dName VARCHAR(500) NOT NULL, ownName VARCHAR(500) NOT NULL, documentId VARCHAR(50) NOT NULL, modifyInfo VARCHAR(500) NOT NULL, quotaBytesUsed bigint, url VARCHAR(500),"
                            + " uid SERIAL, FOREIGN KEY (uid) REFERENCES userData (uid), mid SERIAL, FOREIGN KEY (mid) REFERENCES messageData (mid)) ");
            db.mDropDocTable = db.mConnection.prepareStatement("DROP TABLE documentData");

            /* create x table for google drive activity */
            db.mCreatexTable = db.mConnection.prepareStatement(
                    "CREATE TABLE xData (xid SERIAL PRIMARY KEY, xName VARCHAR(500) NOT NULL, mid SERIAL, FOREIGN KEY (mid) REFERENCES messageData (mid)) ");
            db.mDropxTable = db.mConnection.prepareStatement("DROP TABLE xData");
            db.xNewCol = db.mConnection.prepareStatement("ALTER TABLE xData ADD truth BOOLEAN");

            // Query for functions
            db.dSelectAll = db.mConnection.prepareStatement("SELECT * FROM documentData");
            db.dSelectOne = db.mConnection.prepareStatement("SELECT * from documentData WHERE documentId=?");
            db.dSelectName = db.mConnection.prepareStatement("SELECT ownname from documentData WHERE documentId=?");
            db.dSelectMInfo = db.mConnection.prepareStatement("SELECT modifyinfo from documentData WHERE documentId=?");
            db.dSelectDName = db.mConnection.prepareStatement("SELECT dname from documentData WHERE did=?");
            db.dDeleteOutdate = db.mConnection.prepareStatement(
                    "Delete from documentData where modifyinfo = (select MIN(modifyinfo) from documentData)");
            db.dSelectDsize = db.mConnection.prepareStatement("SELECT SUM(quotaBytesUsed) from documentData");

            db.uDeleteOne = db.mConnection.prepareStatement("DELETE FROM messageData WHERE mid = ?");
            // db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO messageData
            // VALUES (default, ?, ?, ?, ?, ?)");
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO messageData VALUES (default, ?, ?)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT * FROM messageData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from messageData WHERE mid=?");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE messageData SET message = ? WHERE mid = ?");
            // db.mSelectVote = db.mConnection.prepareStatement("SELECT votes FROM
            // messageData WHERE mid=?"); //remove votes from message table
            // db.mUpdateVote = db.mConnection.prepareStatement("UPDATE messageData SET
            // votes = ? WHERE mid=?");//remove votes from message table

            db.uDeleteOne = db.mConnection.prepareStatement("DELETE FROM userData WHERE uid = ?");
            db.uInsertOne = db.mConnection.prepareStatement("INSERT INTO userData VALUES (default, ?, ?, ?, ?, ?)");
            db.uSelectAll = db.mConnection.prepareStatement("SELECT * FROM userData");
            db.uSelectOne = db.mConnection.prepareStatement("SELECT * from userData WHERE uid=?");
            db.uUpdateName = db.mConnection.prepareStatement("UPDATE userData SET username = ? WHERE uid = ?");
            db.uUpdateLoc = db.mConnection.prepareStatement("UPDATE userData SET location = ? WHERE uid = ?");
            db.uSelectName = db.mConnection.prepareStatement("SELECT username from userData WHERE uid=?");
            db.uSelectLoc = db.mConnection.prepareStatement("SELECT location from userData WHERE uid=?");

            db.uUpdateEmail = db.mConnection.prepareStatement("UPDATE userData SET email = ? WHERE uid = ?");
            db.uUpdatePass = db.mConnection.prepareStatement("UPDATE userData SET password = ? WHERE uid = ?");
            db.uSelectEmail = db.mConnection.prepareStatement("SELECT email from userData WHERE uid = ?");
            db.uSelectPass = db.mConnection.prepareStatement("SELECT password from userData WHERE uid = ?");

            // CRUD for new tables, votesTable & commentsTable
            db.uDeleteOne = db.mConnection.prepareStatement("DELETE FROM votesData WHERE mid = ?");
            // db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO votesData VALUES
            // (?, ?, 0)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT * FROM votesData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from votesData WHERE mid = ?");
            // db.mUpdateOne = db.mConnection.prepareStatement("UPDATE votesData SET message
            // = ? WHERE vid = ?");
            db.mSelectVote = db.mConnection.prepareStatement("SELECT votes FROM votesData WHERE mid =?");
            db.mUpdateVote = db.mConnection.prepareStatement("UPDATE votesData SET votes = ? WHERE mid=? and uid =?");

            db.uDeleteOne = db.mConnection.prepareStatement("DELETE FROM commentsData WHERE cid = ?");
            // db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO commentsData
            // VALUES (default, ?, ?, ?)");
            db.mInsertOneComment = db.mConnection
                    .prepareStatement("INSERT INTO commentsData VALUES (default, ?, ?, ?)");
            db.mInsertOneVote = db.mConnection.prepareStatement("INSERT INTO votesData VALUES (?, ?, ?)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT * FROM commentsData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from commentsData WHERE cid=?");
            db.mUpdateMsgComment = db.mConnection
                    .prepareStatement("UPDATE commentsData SET commentMsg = ? WHERE cid = ?");
            // db.mSelectVote = db.mConnection.prepareStatement("SELECT votes FROM
            // commentsData WHERE mid=?");
            // db.mUpdateVote = db.mConnection.prepareStatement("UPDATE commentsData SET
            // votes = ? WHERE cid=?");

            db.uBlockCol = db.mConnection.prepareStatement("ALTER TABLE userData ADD block BOOLEAN");
            db.mFlagCol = db.mConnection.prepareStatement("ALTER TABLE messageData ADD flag BOOLEAN");

            db.mSelectFlag = db.mConnection.prepareStatement("SELECT mid, message FROM messageData WHERE flag = TRUE");
            db.uSelectBlock = db.mConnection.prepareStatement("SELECT uid, username FROM userData WHERE block = TRUE");

        } catch (

        SQLException e) {
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
     * create document table for google drive
     */
    void createDocTable() {
        try {
            mCreateDocTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create user table
     */
    void createVotesTable() {
        try {
            mCreateVotesTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create user table
     */
    void createCommentsTable() {
        try {
            mCreateCommentsTable.execute();
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
     * Insert a row into the database
     * 
     * @param subject The subject for this new row
     * @param message The message body for this new row
     * 
     * @return The number of rows that were inserted
     */
    int insertMRow(String message, Long uid) {
        int count = 0;
        try {
            mInsertOne.setString(1, message);
            mInsertOne.setLong(2, uid);
            count += mInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // here*****************************
    int insertComRow(String message, Long uid, int mid) {
        // int insertComRow(int cid, String message, int uid, int mid) {
        int count = 0;
        try {
            // mInsertOneComment.setInt(1, cid);
            mInsertOneComment.setString(1, message);
            mInsertOneComment.setLong(2, uid);
            mInsertOneComment.setInt(3, mid);
            count += mInsertOneComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /** Related function for user table */
    int insertURow(String username, String location, String email, String password, String salt) {
        // add int, userID
        int count = 0;
        try {
            // uInsertOne.setString(1, userID);
            //
            uInsertOne.setString(1, username);
            uInsertOne.setString(2, location);
            uInsertOne.setString(3, email);
            uInsertOne.setString(4, password);
            uInsertOne.setString(5, salt);
            count += uInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    int insertVoteRow(int mid, Long uid, int votes) {
        int count = 0;
        try {
            mInsertOneVote.setInt(1, mid);
            mInsertOneVote.setLong(2, uid);
            mInsertOneVote.setInt(3, votes);
            count += mInsertOneVote.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
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
     * Update the like message
     */
    int updateVote(int mid, Long uid, int votes) {
        int res = -1;
        try {
            mUpdateVote.setInt(1, mid);
            mUpdateVote.setLong(2, uid);
            mUpdateVote.setInt(3, votes);
            res = mUpdateVote.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * update the email
     */
    int updateEmail(String email, Long uid) {
        int res = -1;
        try {
            uUpdateEmail.setString(1, email);
            uUpdateEmail.setLong(2, uid);
            res = uUpdateEmail.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * update the password
     */
    int updatePass(String password, Long uid) {
        int res = -1;
        try {
            uUpdatePass.setString(1, password);
            uUpdatePass.setLong(2, uid);
            res = uUpdatePass.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Remove userdata and messagedata from the database. If it does not exist, this
     * will print an error. *******************************************
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

    void dropVotesTable() {
        try {
            mDropVotesTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropCommentsTable() {
        try {
            mDropCommentsTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Drop document table */
    void dropDocTable() {
        try {
            mDropDocTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * @return The number of rows that were deleted. -1 indicates an error.
     */
    int deleteURow(Long uid) {
        int res = -1;
        try {
            uDeleteOne.setLong(1, uid);
            res = uDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    // delete msg data
    int deleteMRow(int mid) {
        int res = -1;
        try {
            mDeleteOne.setInt(1, mid);
            res = mDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteCRow(int cid) {
        int res = -1;
        try {
            mDeleteOne.setInt(1, cid);
            res = mDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Check the message status by its mid
     */
    int selectVote(int mid) {
        int res = 0;
        try {
            mSelectVote.setInt(1, mid);
            ResultSet rs = mSelectVote.executeQuery();
            if (rs.next()) {
                res = rs.getInt("votes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
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
                res.add(new RowData(rs.getInt("mid"), rs.getString("message"), rs.getInt("votes"), rs.getLong("uid")));
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
                res = new RowData(rs.getInt("mid"), rs.getString("message"), rs.getInt("votes"), rs.getLong("uid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Check the message status by its mid
     */
    Long selectEmail(Long uid) {
        Long res = (long) 0;
        try {
            uSelectEmail.setLong(1, uid);
            ResultSet rs = uSelectEmail.executeQuery();
            if (rs.next()) {
                res = rs.getLong("uid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Check the message status by its mid
     */
    int selectPassword(int uid) {
        int res = 0;
        try {
            uSelectPass.setInt(1, uid);
            ResultSet rs = uSelectPass.executeQuery();
            if (rs.next()) {
                res = rs.getInt("uid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    ArrayList<RowUData> selectUAll() {
        ArrayList<RowUData> res = new ArrayList<RowUData>();
        try {
            ResultSet rs = uSelectAll.executeQuery();
            while (rs.next()) {
                // uid, name, loc, ema, pass, sal
                // res = new RowUData(rs.getInt("uid"), rs.getString("username"),
                // rs.getString("location"), rs.getString("email"), rs.getString("password"),
                // rs.getString("salt"));
                res.add(new RowUData(rs.getLong("uid"), rs.getString("username"), rs.getString("location"),
                        rs.getString("email"), rs.getString("password"), rs.getString("salt")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<RowCData> selectAllComment() {
        ArrayList<RowCData> res = new ArrayList<RowCData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()) {
                // cid, commentMsg, uid, mid
                // res = new RowCData(rs.getInt("cid"), rs.getString("commentMsg"),
                // rs.getInt("uid"), rs.getInt("mid"));
                res.add(new RowCData(rs.getInt("cid"), rs.getString("commentMsg"), rs.getLong("uid"),
                        rs.getInt("mid")));
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
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowUData selectUOne(Long uid) {
        RowUData res = null;
        try {
            uSelectOne.setLong(1, uid);
            ResultSet rs = uSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowUData(rs.getLong("uid"), rs.getString("username"), rs.getString("location"),
                        rs.getString("email"), rs.getString("password"), rs.getString("salt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String selectUName(Long uid) {
        String res = null;
        try {
            uSelectName.setLong(1, uid);
            ResultSet rs = uSelectName.executeQuery();
            if (rs.next()) {
                res = rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String selectULoc(Long uid) {
        String res = null;
        try {
            uSelectLoc.setLong(1, uid);
            ResultSet rs = uSelectLoc.executeQuery();
            if (rs.next()) {
                res = rs.getString("location");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the username for a row in the database
     * 
     * @param uid      The user id of the row to update
     * @param username The new username contents
     * 
     * @return The number of rows that were updated. -1 indicates an error.
     */
    int updateUname(Long uid, String username) {
        int res = -1;
        try {
            uUpdateName.setString(1, username);
            uUpdateName.setLong(2, uid);
            res = uUpdateName.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateUloc(Long uid, String location) {
        int res = -1;
        try {
            uUpdateLoc.setString(1, location);
            uUpdateLoc.setLong(2, uid);
            res = uUpdateLoc.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    // update email, password, and comments
    int updateMMsg(String msg, int cid) {
        int res = -1;
        try {
            mUpdateMsgComment.setString(1, msg);
            mUpdateMsgComment.setInt(2, cid);
            res = uUpdateLoc.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateUEmail(String email, Long uid) {
        int res = -1;
        try {
            uUpdateEmail.setString(1, email);
            uUpdateEmail.setLong(2, uid);
            res = uUpdateLoc.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateUPass(String password, Long uid) {
        int res = -1;
        try {
            uUpdatePass.setString(1, password);
            uUpdatePass.setLong(2, uid);
            res = uUpdateLoc.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /* get all info in document database */
    ArrayList<RowDData> selectDAll() {
        ArrayList<RowDData> res = new ArrayList<RowDData>();
        try {
            ResultSet rs = dSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowDData(rs.getInt("did"), rs.getString("dname"), rs.getString("ownname"),
                        rs.getString("documentId"), rs.getString("modifyinfo"), rs.getLong("quotaBytesUsed"),
                        rs.getString("url"), rs.getLong("uid"), rs.getInt("mid")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    RowDData selectDOne(String did) {
        RowDData res = null;
        try {
            dSelectOne.setString(1, did);
            ResultSet rs = dSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowDData(rs.getInt("did"), rs.getString("dname"), rs.getString("ownname"),
                        rs.getString("documentId"), rs.getString("modifyinfo"), rs.getLong("quotaBytesUsed"),
                        rs.getString("url"), rs.getLong("uid"), rs.getInt("mid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String selectDOName(String did) {
        String res = null;
        try {
            dSelectName.setString(1, did);
            ResultSet rs = dSelectName.executeQuery();
            if (rs.next()) {
                res = rs.getString("ownname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String selectDName(int did) {
        String res = null;
        try {
            dSelectDName.setInt(1, did);
            ResultSet rs = dSelectDName.executeQuery();
            if (rs.next()) {
                res = rs.getString("dname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String selectDModiInfo(String did) {
        String res = null;
        try {
            dSelectMInfo.setString(1, did);
            ResultSet rs = dSelectMInfo.executeQuery();
            if (rs.next()) {
                res = rs.getString("modifyinfo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    Long selectDsize() {
        Long res = (long) 0;
        try {
            ResultSet rs = dSelectDsize.executeQuery();
            if (rs.next()) {
                res = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    void deleteDRow() {
        // int res = -1;
        try {
            int res = dDeleteOutdate.executeUpdate();
            if (res > 0)
                System.out.println("Successfully Delete Outdated document");
            else
                System.out.println("ERROR OCCURED :(");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addBlockCol() {
        try {
            uBlockCol.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addFlagCol() {
        try {
            mFlagCol.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* For Test */
    void createxTable() {
        try {
            mCreatexTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void dropxTable() {
        try {
            mDropxTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addXNewC() {
        try {
            xNewCol.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ArrayList<RowAData> selectBlock() {
        ArrayList<RowAData> res = new ArrayList<RowAData>();
        try {
            ResultSet rs = uSelectBlock.executeQuery();
            while (rs.next()) {
                res.add(new RowAData(rs.getLong("uid"), rs.getString("username")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<RowMData> selectFlag() {
        ArrayList<RowMData> res = new ArrayList<RowMData>();
        try {
            ResultSet rs = mSelectFlag.executeQuery();
            while (rs.next()) {
                res.add(new RowMData(rs.getInt("mid"), rs.getString("message")));
                // res = rs.getLong("mid");
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}