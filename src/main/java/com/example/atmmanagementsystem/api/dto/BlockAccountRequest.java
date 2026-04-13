package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class BlockAccountRequest {
    @SerializedName("nid_proof")
    private String nidProof;

    public BlockAccountRequest(String nidProof) {
        this.nidProof = nidProof;
    }
}
