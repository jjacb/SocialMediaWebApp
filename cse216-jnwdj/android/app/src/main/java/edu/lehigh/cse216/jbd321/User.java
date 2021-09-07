package edu.lehigh.cse216.jbd321;

public class User {

    private int id;
    //the session key
    private String username;

    public User(int id , String username) {
        this.id = id;
        this.username = username; //the session key provided by the backend
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
       return username;
    }


}
