package com.example.pic_retouching.piceditUI;

public class Filter {
    // encapsulate the filter_name, filter_id and colorMatrix(float[]) in to Filter class
    private String name;
    private int filterID;
    private float[] colorMatrix;

    public Filter (String name, int filterID){
        this.name = name;
        this.filterID = filterID;
    }

    public Filter (int filterID){
        this.filterID = filterID;
    }

    public Filter (String name, int filterID, float[] colorMatrix){
        this.name = name;
        this.filterID = filterID;
        this.colorMatrix = colorMatrix;
    }

    public String getName(){
        return name;
    }// get info

    public int getFilterID(){
        return filterID;
    }// get ImageID

    public float[] getColorMatrix() {
        return colorMatrix;
    }

    public void setInfo(String info){
        this.name = info;
    }// set info

    public void setImageID(int ID){
        this.filterID = ID;
    }// set ImageID

    public void setColorMatrix(float[] colorMatrix) {
        this.colorMatrix = colorMatrix;
    }

    // getter and setter
}
