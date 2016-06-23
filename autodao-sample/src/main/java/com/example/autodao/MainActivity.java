package com.example.autodao;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import autodao.AutoDao;
import autodao.DatabaseManager;
import autodao.Delete;
import autodao.Insert;
import autodao.Model;
import autodao.Select;
import autodao.Update;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "autodao";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        User user = new User();
        user.setName("tubb");
        user.setIdCard(1111);
        user.setVision(5.0f);
        user.setRegistTime(System.currentTimeMillis());
        user.setLogined(true);
//        user.createTime = new Date(System.currentTimeMillis());
        byte[] avatar = "哈哈".getBytes();
        user.setAvatar(avatar);

        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address("长沙", 1111));
        addresses.add(new Address("北京", 1112)); // foreign key failed

        Photo photo = new Photo();
        photo.desc = "最后的晚餐";
        photo.path = new File(getCacheDir().getPath());
        new Insert().from(Photo.class).with(photo).insert();

        user.setPhoto(photo);
        user.setAddresses(addresses);
        new Insert().from(User.class).with(user).insert();

        for (Address address:addresses)
            new Insert().from(Address.class).with(address).insert();

//        new Delete().from(User.class).where("idCard=?", 1111).delete(); // foreign key failed
//        Address.delete(Address.class, 1);

//        new Delete().from(Photo.class).where("desc=?", "最后的晚餐").delete();

        photo.desc = "最后的早餐";
        new Update().from(Photo.class).where(PhotoContract.DESC_COLUMN+"=?", "最后的晚餐").with(photo).update();

        user.setName("涂冰冰");
        user.setVision(70.9f);
        user.setLogined(false);
        user.setIdCard(19120);
        user.createTime = new Date(System.currentTimeMillis());

        new Update(UserContract.USERNAME_COLUMN, UserContract.VISION_COLUMN, UserContract.IDCARD_COLUMN, UserContract.CREATETIME_COLUMN)
                .from(User.class)
                .where(UserContract.IDCARD_COLUMN+"=?", 1111)
                .with(user)
                .update();

        List<User> users = new Select()
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN+"=?", "涂冰冰")
                .select();
//        String usersJson = JSON.toJSONString(users);
//        Log.e(TAG, usersJson);
        User userObj = new Select(UserContract._ID_COLUMN, UserContract.USERNAME_COLUMN, UserContract.IDCARD_COLUMN)
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN+"=?", "涂冰冰")
                .selectSingle();
//        Log.e(TAG, JSON.toJSONString(userObj));
    }

}
