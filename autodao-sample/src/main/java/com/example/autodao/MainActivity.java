package com.example.autodao;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import autodao.AutoSQLiteOpenHelper;
import autodao.Injector;
import autodao.Insert;
import autodao.Select;
import autodao.Update;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "autodao";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** database1 */
        AutoSQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "autodao.db", null, 2);
        SQLiteDatabase db = helper.getWritableDatabase();
        Injector injector = helper.getInjector(db);

        User user = new User();
        user.setName("tubb");
        user.setIdCard(1111);
        user.setVision(5.0f);
        user.setRegistTime(System.currentTimeMillis());
        user.setLogined(true);
        byte[] avatar = "哈哈".getBytes();
        user.setAvatar(avatar);

        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address("长沙", 1111));
        addresses.add(new Address("北京", 1112)); // foreign key failed

        Photo photo = new Photo();
        photo.desc = "最后的晚餐";
        photo.path = new File(getCacheDir().getPath());
        new Insert(injector).from(Photo.class).with(photo).insert();

        user.setPhoto(photo);
        user.setAddresses(addresses);
        new Insert(injector).from(User.class).with(user).insert();

        for (Address address:addresses)
            new Insert(injector).from(Address.class).with(address).insert();

        photo.desc = "最后的早餐";
        new Update(injector).from(Photo.class).where(PhotoContract.DESC_COLUMN+"=?", "最后的晚餐").with(photo).update();

        user.setName("涂冰冰");
        user.setVision(70.9f);
        user.setLogined(false);
        user.setIdCard(19120);
        user.createTime = new Date(System.currentTimeMillis());

        new Update(injector, UserContract.USERNAME_COLUMN, UserContract.VISION_COLUMN, UserContract.IDCARD_COLUMN, UserContract.CREATETIME_COLUMN)
                .from(User.class)
                .where(UserContract.IDCARD_COLUMN+"=?", 1111)
                .with(user)
                .update();

        List<User> users = new Select(injector)
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN+"=?", "涂冰冰")
                .select();

        User userObj = new Select(injector, UserContract._ID_COLUMN, UserContract.USERNAME_COLUMN, UserContract.IDCARD_COLUMN)
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN+"=?", "涂冰冰")
                .selectSingle();

        db.close();

        /** database2 */
        AutoSQLiteOpenHelper helperS = new MySQLiteOpenHelper(this, "autodao2.db", null);
        SQLiteDatabase dbS = helperS.getWritableDatabase();
        Injector injectorS = helperS.getInjector(dbS);
        Photo photo2 = new Photo();
        photo2.desc = "最后的xx";
        photo2.path = new File(getCacheDir().getPath());
        new Insert(injectorS).from(Photo.class).with(photo2).insert();
        dbS.close();
    }
}
