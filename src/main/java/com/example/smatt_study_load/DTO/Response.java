package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class Response {
    private String respone;
    public Response(String response){
        this.respone=response;
    }
}
