package com.example.friendship.constant;


public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私密"),
    SECRET(2,"加密");

    private Integer value;
    private String description;

    public static TeamStatusEnum getStatusNumber(Integer status) {
        if(status < 0 || status == null) {
            return null;
        }
        TeamStatusEnum[] teamStatusEnums = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : teamStatusEnums) {
            if(status.equals(teamStatusEnum.getValue())) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
