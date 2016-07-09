package com.example.autodao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import autodao.AutoDaoLog;
import autodao.AutoSQLiteOpenHelper;
import autodao.Delete;
import autodao.Injector;
import autodao.Insert;
import autodao.JoinSelect;
import autodao.Select;
import autodao.Update;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "autodao";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoDaoLog.setDebug(true);

        /** database1 */
        AutoSQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "autodao.db", null, 2);
        SQLiteDatabase db = helper.getWritableDatabase();
        Injector injector = helper.getInjector(db);

        User user = new User();
        user.name = "tubb";
        user.idCard = 1111;
        user.vision = 5.0f;
        user.registTime = System.currentTimeMillis();
        user.logined = true;
        byte[] avatar = "哈哈".getBytes();
        user.avatar = avatar;
        user.createTime = new Date(System.currentTimeMillis());

        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address("长沙", 1111));
//        addresses.add(new Address("北京", 1112)); // foreign key failed

        Photo photo = new Photo();
        photo.desc = "最后的晚餐";
        photo.path = new File(getCacheDir().getPath());

//        long preT = System.currentTimeMillis();
//        db.beginTransaction();
        for (int i = 0; i < 10; i++) {
            photo.desc = "photo";
            new Insert(injector, PhotoContract.DESC_COLUMN).from(Photo.class).with(photo).insert();
        }
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        long take = System.currentTimeMillis() - preT;

//        AutoDaoLog.d("take:"+take);

        user.photo = photo;
        user.addresses = addresses;
        new Insert(injector).from(User.class).with(user).insert();

        for (Address address : addresses)
            new Insert(injector).from(Address.class).with(address).insert();

        photo.desc = "最后的早餐";
//        new Update(injector).from(Photo.class).where(PhotoContract.DESC_COLUMN+"=?", "最后的晚餐").with(photo).update();

        user.name = "涂冰冰";
        user.vision = 70.9f;
        user.logined = false;
        user.idCard = 19120;
        user.createTime = new Date(System.currentTimeMillis());

//        new Update(injector, UserContract.USERNAME_COLUMN, UserContract.VISION_COLUMN, UserContract.IDCARD_COLUMN, UserContract.CREATETIME_COLUMN)
//                .from(User.class)
//                .where(UserContract.IDCARD_COLUMN+"=?", 1111)
//                .with(user)
//                .update();

        List<User> users = new Select(injector)
                .from(User.class)
//                .where(UserContract.USERNAME_COLUMN + "=?", "tubb")
                .or(UserContract.IDCARD_COLUMN+"=?", 19120)
                .select();

        User userObj = new Select(injector, UserContract._ID_COLUMN, UserContract.USERNAME_COLUMN, UserContract.IDCARD_COLUMN)
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN + "=?", "tubb")
                .selectSingle();

        Object obj = new JoinSelect(injector, "u._id uid", "u.userName userName", "a.name addressName")
                .from(User.class, "u")
                .innerJoin(Address.class, "a")
                .on("u._id=a._id")
                .select(new JoinSelect.CursorHandler() {
                    @Override
                    public Object onHandle(Cursor cursor) {
                        List<UserAddress> userAddresses = new ArrayList<>(cursor.getCount());
                        while (cursor.moveToNext()) {
                            UserAddress userAddress = new UserAddress();
                            long userId = cursor.getLong(cursor.getColumnIndex("uid"));
                            String userName = cursor.getString(cursor.getColumnIndex("userName"));
                            String addressName = cursor.getString(cursor.getColumnIndex("addressName"));
                            userAddress.userId = userId;
                            userAddress.userName = userName;
                            userAddress.addressName = addressName;
                            userAddresses.add(userAddress);
                        }
                        cursor.close();
                        return userAddresses;
                    }
                });
        List<UserAddress> userAddressList = (List<UserAddress>) obj;
        if (AutoDaoLog.isDebug()) {
            AutoDaoLog.d(JSON.toJSONString(userAddressList));
        }

        db.close();

        /** database2 */
        AutoSQLiteOpenHelper helperS = new MySQLiteOpenHelper(this, "autodao2.db", null);
        SQLiteDatabase dbS = helperS.getWritableDatabase();
        Injector injectorS = helperS.getInjector(dbS);
        Photo photo2 = new Photo();
        photo2.desc = "最后的xx";
        photo2.path = new File(getCacheDir().getPath());
        new Insert(injectorS).from(Photo.class).with(photo2).insert();
//        new Delete(injectorS).from(Photo.class).where(PhotoContract.DESC_COLUMN+"=?", "最后的xx").delete();
        photo2.desc = "最后的哈哈";
        new Update(injectorS, PhotoContract.DESC_COLUMN).from(Photo.class).where(PhotoContract.DESC_COLUMN + "=?", "最后的xx").with(photo2).update();
        dbS.close();
    }

    class UserAddress {
        long userId;
        public String userName;
        public String addressName;
    }
}
