package com.example.autodao;

import java.util.Date;

import autodao.TypeSerializer;

/**
 * Created by tubingbing on 16/6/20.
 */
public class DateSerializer extends TypeSerializer{

    @Override
    public Object serialize(Object data) {
        if (data instanceof Date) return ((Date)data).getTime();
        return null;
    }

    @Override
    public Object deserialize(Object data) {
        if (data instanceof Long)
            return new Date((Long) data);
        return null;
    }
}
