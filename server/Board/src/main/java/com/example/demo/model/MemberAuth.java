package com.example.demo.model;

import java.util.HashMap;
import java.util.Map;


/*
MemberAuth  : 회원들의 권한을 관리
MemberAuth는 멤버들의 권한정보를 String으로 관리하기 보다는
enum으로 관리하는게~
String을 이용하여 권한을 찾을 일이 많을 거 같아서
Map처럼 사용할 수 있도록 lookup 맵을 생성하여
조회 및 포함여부 ㅇㅇ
 */


public enum MemberAuth {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN"),

    ;

    private final String abbreviation;

    private static final Map<String,MemberAuth> lookup = new HashMap<>();

    static {
        for(MemberAuth auth : MemberAuth.values()) {
            lookup.put(auth.abbreviation,auth);
        }
    }

    // private
    MemberAuth(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public static MemberAuth get(String abbreviation) {
        return lookup.get(abbreviation);
    }

    public static boolean containsKey(String abbreviation) {
        return lookup.containsKey(abbreviation);
    }


