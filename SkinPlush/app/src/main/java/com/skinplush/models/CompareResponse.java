package com.skinplush.models;

/**
 * Created by Awuni Junior on 18/11/2020.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompareResponse {

    @SerializedName("data")
    @Expose
    private CompareData data;
    @SerializedName("message")
    @Expose
    private String message;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @SerializedName("error")
    @Expose
    private String error;

    public void setCompareData(CompareData data) {
        this.data = data;
    }

    @SerializedName("statusCode")
    @Expose
    private Integer statusCode;

    public CompareData getCompareData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

}