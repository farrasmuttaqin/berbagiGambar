package com.berbagibagi.berbagigambar;

import java.io.Serializable;

public class ListItem implements Serializable{

    private String imageUrl;

    public ListItem( String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl(){
        return imageUrl;
    }

}