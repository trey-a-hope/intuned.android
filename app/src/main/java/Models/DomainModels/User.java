package Models.DomainModels;

import java.util.ArrayList;

public class User {
    public String id;
    public String uid;
    public String email;
    public String username;
    public Song song;
    public String favoriteGenre;
    public String favoriteArtist;
    public String favoriteSong;
    public ArrayList<String> followers;
    public ArrayList<String> followings;
}