package app.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class UserWithCards {
    private String username;
    private String password;
    private String displayName;
    private String bio;
    private String image;
    private int coins;
    private int elo;
    private int battlesWon;
    private int battlesLost;
    private ArrayList<Card> deck;
    private ArrayList<Card> stack;

    public UserWithCards() {}
}
