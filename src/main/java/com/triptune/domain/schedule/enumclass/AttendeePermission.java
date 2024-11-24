package com.triptune.domain.schedule.enumclass;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AttendeePermission {

    ALL(1, "전체 허용", "편집 및 채팅 허용"),
    EDIT(2, "편집 허용", "편집 허용, 채팅 불가"),
    CHAT(3, "채팅 허용", "편집 불가, 채팅 허용"),
    READ(4, "읽기 허용", "편집 및 채팅 불가");

    private final int id;
    private final String permission;
    private final String description;

    public boolean isEnableChat(){
        return this.equals(ALL) || this.equals(CHAT);
    }
}
