package com.cinema.CineConnect.model;

import java.time.LocalDate;

public class Client extends User{

    public Client( String name,String password, String email, String role, LocalDate birth_date) {
        super(name,password, email, role, birth_date);
    }
    public String GetInfo() {
        return ("Hello user "+name+"! your email is "+email+" you are a client!");
    }

}
