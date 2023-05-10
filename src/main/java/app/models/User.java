package app.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    @JsonProperty(value = "Username", access= JsonProperty.Access.WRITE_ONLY)
    private String username;
    @JsonProperty(value = "Password", access= JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonProperty("Name")
    private String displayName;
    @JsonProperty("Bio")
    private String bio;
    @JsonProperty("Image")
    private String image;
    @JsonIgnore
    private int coins;
    @JsonIgnore
    private int elo;
    @JsonIgnore
    private int battlesWon;
    @JsonIgnore
    private int battlesLost;

    //default constructor
    public User() {}
}
