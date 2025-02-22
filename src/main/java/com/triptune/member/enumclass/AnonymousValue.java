package com.triptune.member.enumclass;

import lombok.Getter;

@Getter
public enum AnonymousValue {
    ANONYMOUS("알 수 없음");

    private final String value;

    AnonymousValue(String value) {
        this.value = value;
    }
}
