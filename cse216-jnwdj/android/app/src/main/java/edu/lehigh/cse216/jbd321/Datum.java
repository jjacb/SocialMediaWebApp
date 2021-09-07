package edu.lehigh.cse216.jbd321;

public class Datum {
    /**
     * An integer index for this piece of data
     */
    int mIndex;

    /**
     * The string contents that comprise this piece of data
     */
    String mText;

    /**
     * The number of votes a given message has
     */
    //int mVotes;

    /**
     * An integer index for this piece of data
     */
    int mUid;

    /**
     * The string contents that comprise this piece of data
     */
    //String mComment;

    /**
     * Construct a Datum by setting its index and text
     *
     * @param idx The index of this piece of data
     * @param txt The string contents for this piece of data
     */
    Datum(int idx, String txt /*, int votes /*, String comment*/,int uid) {
        mIndex = idx;
        mText = txt;
        //mVotes = votes;
        //mComment = comment;
        mUid = uid;
    }


}
