package com.skinplush.models;

/**
 * Created by Awuni Junior  on 18/11/2020.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompareData {

    @SerializedName("image1")
    @Expose
    private String image1;
    @SerializedName("image2")
    @Expose
    private String image2;
    @SerializedName("outputImage")
    @Expose
    private String outputImage;

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getOutputImage() {
        return outputImage;
    }

    public void setOutputImage(String outputImage) {
        this.outputImage = outputImage;
    }

}