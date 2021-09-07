package edu.lehigh.cse216.jbd321.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import com.sendgrid.*;

import edu.lehigh.cse216.jbd321.admin.Database.RowAData;

import java.io.IOException;

/**
 * App is our basic admin app. For now, it is a demonstration of the six key
 * operations on a database: connect, insert, update, query, delete, disconnect
 */
public class App {

    /**
     * Print the menu for our program
     */
    static void menu() {
        System.out.println("Main Menu");
        System.out.println("  [U] Create userData");
        System.out.println("  [u] Drop userData");
        System.out.println("  [M] Create messageData");
        System.out.println("  [m] Drop messageData");

        System.out.println("  [C] Create commentsData");
        System.out.println("  [c] Drop commentsData");

        System.out.println("  [D] Create documentData");
        System.out.println("  [d] Drop documentData");

        System.out.println("  [G] Insert a comment row");
        System.out.println("  [g] Delete a comment row");
        System.out.println("  [h] Update a comment row");

        System.out.println("  [K] Create votesData");
        System.out.println("  [k] Drop votesData");
        System.out.println("  [B] Insert a vote row");
        // System.out.println(" [b] delete a vote row");

        System.out.println("  [1] Query for a specific message row");
        System.out.println("  [*] Query for all message rows");
        System.out.println("  [-] Delete a message row");
        System.out.println("  [+] Insert a new mesgage row");
        System.out.println("  [~] Update a message row");
        System.out.println("  [=] Query for a specific user row");
        System.out.println("  [&] Query for all user rows");
        System.out.println("  [!] Delete a user row");
        System.out.println("  [^] Insert a new user row");
        System.out.println("  [_] Update a user row");
        System.out.println("  [L] Update user location");
        System.out.println("  [V] Update a messgae votes");

        System.out.println("  [Z] List all document in Google Drive");
        System.out.println("  [z] Delete a document in Google Drive");
        System.out.println("  [A] View the owner of a document");
        System.out.println("  [a] View the modify info of a document");
        System.out.println("  [E] View the content of one document");
        System.out.println("  [e] View the owner name of a document");
        System.out.println("  [F] Delete outdate document");
        System.out.println("  [f] Get the memory size");

        System.out.println("  [Y] Add Flag Column into message table");
        System.out.println("  [y] Add Block Column into user table");

        System.out.println("  [W] View Flag message id");
        System.out.println("  [w] View users need to be blocked");

        System.out.println("  [q] Quit Program");
        System.out.println("  [?] Help (this message)");
    }

    /**
     * Ask the user to enter a menu option; repeat until we get a valid option
     * 
     * @param in A BufferedReader, for reading from the keyboard
     * 
     * @return The character corresponding to the chosen menu option
     */
    static char prompt(BufferedReader in) {
        // The valid actions:
        String actions = "UuMm1*-+~=&!^_LVq?CcKkGghBDdZzAaEeFfXxYyWw";

        // We repeat until a valid single-character option is selected
        while (true) {
            System.out.print("[" + actions + "] :> ");
            String action;
            try {
                action = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (action.length() != 1)
                continue;
            if (actions.contains(action)) {
                return action.charAt(0);
            }
            System.out.println("Invalid Command");
        }
    }

    /**
     * Ask the user to enter a String message
     * 
     * @param in      A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     * 
     * @return The string that the user provided. May be "".
     */
    static String getString(BufferedReader in, String message) {
        String s;
        try {
            System.out.print(message + " :> ");
            s = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return s;
    }

    /**
     * Ask the user to enter an integer
     * 
     * @param in      A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     * 
     * @return The integer that the user provided. On error, it will be -1
     */
    static int getInt(BufferedReader in, String message) {
        int i = -1;
        try {
            System.out.print(message + " :> ");
            i = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    static Long getLong(BufferedReader in, String message) {
        Long i = (long) -1;
        try {
            System.out.print(message + " :> ");
            i = Long.parseLong(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    /*
     * int email(String email){ //Email from = new Email("test@example.com"); String
     * subject = "Sending with SendGrid is Fun"; Email to = new
     * Email("test@example.com"); Content content = new Content("text/plain",
     * "and easy to do anywhere, even with Java"); Mail mail = new Mail(from,
     * subject, to, content);
     * 
     * SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY")); Request
     * request = new Request(); try { request.setMethod(Method.POST);
     * request.setEndpoint("mail/send"); request.setBody(mail.build()); Response
     * response = sg.api(request); System.out.println(response.getStatusCode());
     * System.out.println(response.getBody());
     * System.out.println(response.getHeaders()); } catch (IOException ex) { throw
     * ex; } //send an email with a link to change password //that password needs to
     * be updated in the sql table }
     */

    /**
     * The main routine runs a loop that gets a request from the user and processes
     * it
     * 
     * @param argv Command-line options. Ignored by this program.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void main(String[] argv) throws IOException, GeneralSecurityException {
        DriveQuickstart quickstart = new DriveQuickstart();

        // get the Postgres configuration from the environment
        Map<String, String> env = System.getenv();
        // String ip = env.get("POSTGRES_IP");
        // String port = env.get("POSTGRES_PORT");
        // String user = env.get("POSTGRES_USER");
        // String pass = env.get("POSTGRES_PASS");

        // Get a fully-configured connection to the database, or exit
        // immediately
        // String db_url = env.get("DATABASE_URL");
        String db_url = "postgres://zlaennwxsskfdj:cecfea3727b02543045f3779ef412fcf1e31a121325e45a448a7d227a6f61bd6@ec2-54-235-92-244.compute-1.amazonaws.com:5432/d2tdob33qt6k0f";
        Database db = Database.getDatabase(db_url);
        if (db == null)
            return;

        // Start our basic command-line interpreter:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // Get the user's request, and do it
            //
            // NB: for better testability, each action should be a separate
            // function call
            char action = prompt(in);
            if (action == '?') {
                menu();
            } else if (action == 'q') {
                break;
            } else if (action == 'M') {
                db.createMsgTable();
            } else if (action == 'C') {
                db.createCommentsTable();
            } else if (action == 'K') {
                db.createVotesTable();
            } else if (action == 'm') {
                db.dropMsgTable();
            } else if (action == 'c') {
                db.dropCommentsTable();
            } else if (action == 'k') {
                db.dropVotesTable();
            } else if (action == 'U') {
                db.createUserTable();
            } else if (action == 'u') {
                db.dropUserTable();
            } else if (action == 'D') {
                db.createDocTable();
            } else if (action == 'd') {
                db.dropDocTable();
            } else if (action == 'X') {
                db.createxTable();
            } else if (action == 'x') {
                db.dropxTable();
            } else if (action == 'Y') {
                db.addFlagCol();
            } else if (action == 'y') {
                db.addBlockCol();
            } else if (action == '1') {
                int mid = getInt(in, "Enter the row ID");
                if (mid == -1)
                    continue;
                Database.RowData res = db.selectOne(mid);
                if (res != null) {
                    System.out.println("  [" + res.mMid + "] " /* + res.mMessage */ + " - votes: " + res.mVotes
                            + " By user: " + res.mUid);
                    System.out.println("  --> " + res.mMessage);
                }
            } else if (action == '*') {
                ArrayList<Database.RowData> res = db.selectAll();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println(
                            "  [" + rd.mMid + "] " + rd.mMessage + " - votes: " + rd.mVotes + " By user: " + rd.mUid);
                }
            } else if (action == '-') {
                int mid = getInt(in, "Enter the row ID");
                if (mid == -1)
                    continue;
                int res = db.deleteMRow(mid);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == '+') {
                String message = getString(in, "Enter the message");
                Long uid = getLong(in, "Enter the Uid");
                if (message.equals(""))
                    continue;
                int res = db.insertMRow(message, uid);
                System.out.println(res + " rows added");
            } else if (action == '~') {
                int mid = getInt(in, "Enter the row ID :> ");
                if (mid == -1)
                    continue;
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateOne(mid, newMessage);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == '=') {
                Long uid = getLong(in, "Enter the row ID");
                if (uid == -1)
                    continue;
                Database.RowUData res = db.selectUOne(uid);
                if (res != null) {
                    System.out.println("  [" + res.uUid + "] " + res.uUsername + " - location: " + res.uLocation);
                }
            } else if (action == '&') {
                ArrayList<Database.RowUData> res = db.selectUAll();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowUData rd : res) {
                    System.out.println("  [" + rd.uUid + "] " + rd.uUsername + " - location: " + rd.uLocation);
                }
            } else if (action == '!') {
                Long uid = getLong(in, "Enter the row ID");
                if (uid == -1)
                    continue;
                int res = db.deleteURow(uid);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
                // insert email activation method
                // here.*************************************************
            } else if (action == '^') {
                // String uid = getInt(in, "Enter the user ID");
                String username = getString(in, "Enter the username");
                String location = getString(in, "Enter the location");
                String email = getString(in, "Enter the email");
                String password = getString(in, "Enter the password");
                String salt = getString(in, "Enter the salt value");

                if (username.equals("") || location.equals(""))
                    continue;
                int res = db.insertURow(username, location, email, password, salt);
                // int res = db.insertURow(uid, username, location, email, password, salt);
                System.out.println(res + " rows added");

            } else if (action == '_') {
                Long uid = getLong(in, "Enter the row ID :> ");
                if (uid == -1)
                    continue;
                String username = getString(in, "Enter the new username");
                int res = db.updateUname(uid, username);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'L') {
                Long uid = getLong(in, "Enter the row ID :> ");
                if (uid == -1)
                    continue;
                String location = getString(in, "Enter the new location");
                int res = db.updateUloc(uid, location);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'V') {
                Long uid = getLong(in, "Enter the user's id ");
                int mid = getInt(in, "Enter the message ID :> ");
                if (mid == -1)
                    continue;
                int votes = getInt(in, "Enter the new vote count");
                int res = db.updateVote(mid, uid, votes);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            }

            else if (action == 'G') {
                // int cid = getInt(in, "Enter the new comment id");
                String com_msg = getString(in, "Enter the new comment msg");
                Long uid = getLong(in, "Enter the user's id");
                int mid = getInt(in, "Enter the message id");
                // add a comment to a message (mid)
                // int res = db.insertComRow(cid, com_msg, uid, mid);
                int res = db.insertComRow(com_msg, uid, mid);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            }

            else if (action == 'g') {
                int cid = getInt(in, "Enter the row ID");
                if (cid == -1)
                    continue;
                int res = db.deleteCRow(cid);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == 'B') {

                int mid = getInt(in, "Enter the message id");
                Long uid = getLong(in, "Enter the user's id");
                int votes = getInt(in, "Enter the vote count");
                // add a comment to a message (mid)
                // int res = db.insertComRow(cid, com_msg, uid, mid);
                int res = db.insertVoteRow(mid, uid, votes);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            }
            /*
             * else if (action == 'b') { int mid = getInt(in, "Enter the message ID"); if
             * (mid == -1) continue; int uid = getInt(in, "Enter the user ID"); if (uid ==
             * -1) continue; int res = db.deleteVoteRow(mid); if (res == -1) continue;
             * System.out.println("  " + res + " rows deleted"); }
             * 
             * 
             * else if (action == 'h') { int cid = getInt(in, "Enter the row ID :> "); if
             * (cid == -1) continue; String newComment = getString(in,
             * "Enter the new comment"); int res = db.updateMsgComment(cid, newComment); if
             * (res == -1) continue; System.out.println("  " + res + " rows updated"); }
             */
            else if (action == 'Z') {
                System.out.print("List content in document:");
                quickstart.listDoc();
            } else if (action == 'z') {
                String fileId = getString(in, "Enter the file id for the file you want to delete");
                quickstart.deleteFile(fileId);
            } else if (action == 'A') {
                String did = getString(in, "Enter the document id that you want to view the owner");
                String res = db.selectDOName(did);
                System.out.println("Owner Name for this document is :  " + res + "");
            } else if (action == 'a') {
                String did = getString(in, "Enter the document id for the file you want to check the modify info with");
                String res = db.selectDModiInfo(did);
                System.out.println("Modify Information:  " + res + "");
            } else if (action == 'E') {
                String did = getString(in, "Enter the document id that you want to see");
                Database.RowDData res = db.selectDOne(did);
                if (res != null) {
                    System.out.println("Document [" + res.ddid + "] -- " + res.name);
                    System.out.println(
                            " UserId: " + res.duid + " MessageID: " + res.dmid + " Document ID: " + res.dDocId);
                    String newline = System.getProperty("line.separator");
                    System.out.println(" Modify Info: " + res.dModifty + newline + " URL: " + res.durl + newline
                            + " Document Size: " + res.dDsize + " bytes " + newline + " Owner: " + res.owner);
                }
            } else if (action == 'e') {
                int did = getInt(in, "Enter the did that you want to check its document name");
                String res = db.selectDName(did);
                System.out.println("Document Name:  " + res + "");
            } else if (action == 'F') {
                db.deleteDRow();
            } else if (action == 'f') {
                System.out.print(
                        "The used memory of drive is " + db.selectDsize() + " bytes out of 15GB (1.5e^10 bytes)");
                // db.selectDsize();
            } else if (action == 'W') {
                ArrayList<Database.RowMData> res = db.selectFlag();
                if (res == null)
                    continue;
                System.out.print("Here are the message might need to changed: ");
                // System.out.println(res);
                for (Database.RowMData rd : res) {
                    System.out.println("  [" + rd.mMid + "] " + rd.mMessage);
                }

                System.out.println("Do you want to modify this message?");
                String answer;
                boolean yn;
                Scanner input = new Scanner(System.in);
                System.out.println("Please type y for Yes, n for No");
                while (true) {
                    answer = input.nextLine().trim().toLowerCase();
                    if (answer.equals("y")) {
                        yn = true;
                        int mid = getInt(in, "Enter the message ID ");
                        if (mid == -1)
                            continue;
                        String newMessage = getString(in, "Enter the new message ");
                        int result = db.updateOne(mid, newMessage);
                        if (result == -1)
                            continue;
                        System.out.println("  " + mid + " is updated");
                        break;
                    } else if (answer.equals("n")) {
                        yn = false;
                        break;
                    } else {
                        System.out.println("Sorry, I didn't catch that. Please answer y/n");
                    }
                }

            } else if (action == 'w') {
                ArrayList<Database.RowAData> res = db.selectBlock();
                if (res == null)
                    continue;
                System.out.println("Users need to be block ");
                System.out.println("-------------------------");
                for (RowAData rd : res) {
                    System.out.println("UserId [" + rd.uUid + "] -- " + rd.uUsername);
                }
                System.out.println("Do you want to delete this user?");
                String answer;
                boolean yn;
                Scanner input = new Scanner(System.in);
                System.out.println("Please type y for Yes, n for No");
                while (true) {
                    answer = input.nextLine().trim().toLowerCase();
                    if (answer.equals("y")) {
                        yn = true;
                        Long uid = getLong(in, "Please enter the uid for user which you want to delete: ");
                        if (uid == -1)
                            continue;
                        int result = db.deleteURow(uid);
                        if (result == -1)
                            continue;
                        System.out.println("  " + uid + " deleted");
                        break;
                    } else if (answer.equals("n")) {
                        yn = false;
                        break;
                    } else {
                        System.out.println("Sorry, I didn't catch that. Please answer y/n");
                    }
                }
            }
        }
        // Always remember to disconnect from the database when the program
        // exits
        db.disconnect();
    }

}
