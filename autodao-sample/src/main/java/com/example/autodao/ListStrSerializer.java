package com.example.autodao;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

import autodao.TypeSerializer;

/**
 * Created by tubingbing on 16/6/19.
 */
public class ListStrSerializer implements TypeSerializer{

    @Override
    public Object serialize(Object data) {
        List<String> strList = (List<String>) data;
        return TextUtils.join(",", strList);
    }

    @Override
    public Object deserialize(Object data) {
        String listStr = String.valueOf(data);
        return Arrays.asList(listStr.split(","));
    }
}
