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

    public String name;

//    @ForeignKey(referenceTableName = "user", referenceColumnName = "idCard", action = "ON UPDATE CASCADE")
    public long userId;

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", userId=" + userId +
                '}';
    }
}
