package edu.lehigh.cse216.jbd321;

public class CommentData {

    /**
     * An integer index for this piece of data
     */
    int cIndex;

    /**
     * The string contents that comprise this piece of data
     */
    String mComment;

    /**
     * An integer index for this piece of data
     */
    //int mId;


    /**
     * Construct a Datum by setting its index and text
     *
     * @param idx The index of this piece of data
     * @param comment The string contents for this piece of data
     * @param //mid The index contents for this piece of data
     */
    CommentData(int idx, String comment/*,int mid*/) {
        cIndex = idx;
        mComment = comment;
        //mId = mid;
    }

}
