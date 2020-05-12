package kibe.dev.genesis.Models;

import com.google.firebase.database.ServerValue;

public class User {

    private String id;
    private String username;
    private String email;
    private String bio;
    private String imageUrl;
    private Object timestamp;

    public User(){

    }

    public User(String id, String username, String email, String bio, String imageUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.imageUrl = imageUrl;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
