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
    private String name;

    @Column(notNULL = true, defaultValue = "89900", check = "idCard>0", unique = false)
    private int idCard;
    private float vision;
    private long registTime;
    private boolean logined;
    private byte[] avatar;

    @Column(name = "addressName")
    @Mapping(name = "name")
    private List<Address> addresses;

    @Column(name = "photoId")
    private Photo photo;

    @Serializer(
            serializerCanonicalName = "com.example.autodao.DateSerializer",
            serializedTypeCanonicalName = "long"
    )
    public Date createTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdCard() {
        return idCard;
    }

    public void setIdCard(int idCard) {
        this.idCard = idCard;
    }

    public float getVision() {
        return vision;
    }

    public void setVision(float vision) {
        this.vision = vision;
    }

    public long getRegistTime() {
        return registTime;
    }

    public void setRegistTime(long registTime) {
        this.registTime = registTime;
    }

    public boolean isLogined() {
        return logined;
    }

    public void setLogined(boolean logined) {
        this.logined = logined;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }

    @Override
    public String toString() {
        return "User{" +
                "addresses=" + addresses +
                ", name='" + name + '\'' +
                ", idCard=" + idCard +
                ", vision=" + vision +
                ", registTime=" + registTime +
                ", logined=" + logined +
                ", avatar=" + Arrays.toString(avatar) +
                ", photo=" + photo +
                '}';
    }
}
