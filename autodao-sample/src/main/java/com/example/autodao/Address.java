package com.example.autodao;

import autodao.ForeignKey;
import autodao.Model;
import autodao.Table;

/**
 * Created by tubingbing on 16/6/2.
 */
@Table(name = "address")
public class Address extends Model{

    public Address(){}

    public Address(String name, long userId) {
        this.name = name;
        this.userId = userId;
    }

    private String name;

//    @ForeignKey(referenceTableName = "user", referenceColumnName = "idCard", action = "ON UPDATE CASCADE")
    private long userId;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", userId=" + userId +
                '}';
    }
}
