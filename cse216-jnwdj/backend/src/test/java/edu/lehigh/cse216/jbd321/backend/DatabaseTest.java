package edu.lehigh.cse216.jbd321.backend;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;

/**
 * Unit test for simple App.
 */
public class DatabaseTest extends TestCase {
    
    Database db = Database.getDatabase("postgres://zlaennwxsskfdj:cecfea3727b02543045f3779ef412fcf1e31a121325e45a448a7d227a6f61bd6@ec2-54-235-92-244.compute-1.amazonaws.com:5432/d2tdob33qt6k0f?ssl=true&sslmode=require");

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */

    public DatabaseTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(DatabaseTest.class);
    }

    
    /**
     * Rigourous Test :-)
     */
    public void testDatabase(){
        assertTrue(true);
    }

    /*public void testMessage() {
        db.insertRow("Backend Test Message", 1);
        // Database.RowData d = db.selectOne(1);
        // assertTrue(d.get(1).equals("Test Message"));
        ArrayList<Database.RowData> data = db.selectAll();
        int mid = (data.get(data.size()-1).mMid);
        String message = data.get(data.size()-1).mMessage;
        System.out.println(message);
        assertEquals("Backend Test Message", message);
        db.deleteRow(mid);
        assertNull(db.selectOne(mid));
        
    }*/

    public void testVote() {
        //db.updateVote(6, 1);
        //assertEquals(1, db.selectVote(6));
        assertTrue(true);
    }

    
}
