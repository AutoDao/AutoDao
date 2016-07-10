package com.example.autodao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import autodao.Column;
import autodao.Index;
import autodao.Mapping;
import autodao.Model;
import autodao.Serializer;
import autodao.Table;

/**
 * Created by tubingbing on 16/5/31.
 */
@Table(name = "user")
@Index(name = "id_card_vision_index", columns = {"idCard", "vision"}, unique = true)
public class User extends Model{

    @Column(name = "userName", notNULL = true)
    public String name;

    @Column(notNULL = true, defaultValue = "89900", check = "idCard>0", unique = false)
    public int idCard;
    public float vision;
    public long registTime;
    public boolean logined;
    public byte[] avatar;

    @Column(name = "addressName")
    @Mapping(name = "name")
    public List<Address> addresses;

    @Column(name = "photoId")
    public Photo photo;

    @Serializer(
            serializerCanonicalName = "com.example.autodao.DateSerializer",
            serializedTypeCanonicalName = "long"
    )
    public Date createTime;

}
