package com.example.autodao;

import java.io.File;

import autodao.TypeSerializer;

/**
 * Created by tubingbing on 16/6/23.
 */
public class FileSerializer extends TypeSerializer{

    @Override
    public Object serialize(Object data) {
        if (data instanceof File) return ((File)data).getAbsolutePath();
        return null;
    }

    @Override
    public Object deserialize(Object data) {
        if (data instanceof String) return new File(String.valueOf(data));
        return null;
    }
}
