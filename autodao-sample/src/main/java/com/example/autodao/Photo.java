package com.example.autodao;

import java.io.File;

import autodao.Column;
import autodao.Model;
import autodao.Serializer;
import autodao.Table;

/**
 * Created by tubingbing on 16/6/3.
 */
@Table(name = "photo")
public class Photo extends Model{

    public String desc;
    @Serializer(
            serializedTypeCanonicalName = "java.lang.String",
            serializerCanonicalName = "com.example.autodao.FileSerializer")
    public File path;

}
