package com.triptune.schedule.enums;


public enum AttendeeRole {
    AUTHOR, GUEST;

    public boolean isAuthor(){
        return this == AUTHOR;
    }
}
