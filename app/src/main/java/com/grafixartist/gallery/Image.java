package com.grafixartist.gallery;

/**
 * Created by Clement on 11/8/2015.
 */
public class Image {
    private String name;
    private String path;
    private String size;
    private String dateTaken;

    public Image(String path, String name, String size, String dateTaken){
        this.name = name;
        this.path = path;
        this.size = size;
        this.dateTaken = dateTaken;
    }

    public Image(){
        this.name = "";
        this.path = "";
        this.size = "";
        this.dateTaken = "";
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path;
    }

    public String getSize(){
        return size;
    }

    public String getDateTaken(){
        return dateTaken;
    }
}
