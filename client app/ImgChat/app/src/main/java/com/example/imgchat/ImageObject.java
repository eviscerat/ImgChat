package com.example.imgchat;

import android.graphics.Bitmap;

public class ImageObject {

    Bitmap image;
    String nickname;
    String coordinates;
    int userId;
    ImageObject(Bitmap img, String str, int id, String coord)
    {
        this.image = img;
        this.nickname=str;
        this.userId = id;
        this.coordinates = coord;
    }
    Bitmap getImg()
    {
        return image;
    }
    String getNickname()
    {
        return nickname;
    }
    String getCoordinates()
    {
        return coordinates;
    }
    int getUserId()
    {
        return userId;
    }
}
