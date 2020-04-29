package com.juhe.creditcardapplyfor.utils.enums;



public enum Constants {
    RESP_CODE("resp_code"),
    SUCCESS("200"),
    ERROR("400"),
    RESP_MESSAGE("resp_message"),
    RESULT("result"),
    LEVEL_THREE("3");


    private String code;

    private Constants(String code) {
        this.code= code;
    }

    @Override
    public String toString() {
        return code;
    }
}
