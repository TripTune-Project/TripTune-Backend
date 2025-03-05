package com.triptune.schedule.enumclass;


public enum AttendeeRole {
    AUTHOR, GUEST;

    public boolean isAuthor(){
        return this == AUTHOR;
    }
}
