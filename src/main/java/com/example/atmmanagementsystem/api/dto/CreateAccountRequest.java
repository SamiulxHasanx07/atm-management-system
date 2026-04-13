package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class CreateAccountRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("phone_number")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("gender")
    private String gender;

    @SerializedName("profession")
    private String profession;

    @SerializedName("nationality")
    private String nationality;

    @SerializedName("nid")
    private String nid;

    @SerializedName("address")
    private String address;

    @SerializedName("initial_deposit")
    private double initialDeposit;

    public CreateAccountRequest(String name, String phone, String email, String gender, 
                                 String profession, String nationality, String nid, 
                                 String address, double initialDeposit) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.profession = profession;
        this.nationality = nationality;
        this.nid = nid;
        this.address = address;
        this.initialDeposit = initialDeposit;
    }
}
