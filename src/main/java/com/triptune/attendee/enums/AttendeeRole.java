package com.triptune.attendee.enums;


public enum AttendeeRole {
    AUTHOR, GUEST;

    public boolean isAuthor(){
        return this == AUTHOR;
    }
}
