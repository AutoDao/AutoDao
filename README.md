#### AutoDao
[AutoDao][4]是一个基于`编译时`，`支持DSL`，`灵活`，`性能良好`的Android ORM框架

#### ORM的思考
几年前，自己开始进行Android应用程序开发的时候，老大说了一句至今印象深刻的话：没有必要不要去用ORM框架，会影响应用的性能。后来随着负责开发的应用越来越复杂，本地存储场景也越来越多，使用ORM框架的来简便工作变得有必要了，那么ORM框架能帮我们节省的工作有哪些呢？个人认为有如下几点:

* 实体对象到数据库表之间的映射（表、列的定义等）
* SQL的编写（创建、删除表SQL，CRUD SQL等）
* 实体的存储和读取

上面列举的几点可能没有包含ORM的所有功能，但都是现有ORM框架的核心功能点。既然这些`boil code`我们不想去写，那么可不可以由工具来生成或者由通用代码来替代呢？

现在主流的ORM实现也大部分是这两个方向，通过工具来生成`boil code`的代表是[greenDAO][1]，通过代码（反射）来替代`boil code`的代表有[ActiveAndroid][2]、[LitePal][3]等。一般来说通过`反射`会比`代码生成`方式性能差一点，但对外接口会灵活很多。基于这两点的对比，自己才编写了`AutoDao`框架，满足`性能`和`接口灵活`两方面的需求。

#### 现有的功能
`AutoDao`还处在快速开发阶段，现在只包含了一些核心的功能

* 实体对象到数据库表之间的映射（表、列名字的自动生成）
* SQL生成（建表SQL、建索引SQL等）
* 自动生成实体DAO，主要包含CRUD方法
* Custom Type支持，复杂数据类型到基本数据类型映射
* 多数据库支持
* DSL支持，CRUD仅需一行代码
* 预编译缓存，性能提升

#### 快速开始

##### 注解
```java
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
```

##### 生成代码
对实体类定义了注解后，需要生成相关的代码（DAO，Contract），直接使用Gradle命令行工具
```xml
./gradlew clean assembleDebug /** MAC平台 */
```
##### CRUD
###### 插入
```java
new Insert(injector).from(User.class).with(user).insert();
```

###### 删除
```java
new Delete(injector).from(User.class).where(UserContract.USERNAME_COLUMN+"=?", "xx").delete();
```

###### 修改
```java
new Update(injector, UserContract.USERNAME_COLUMN, UserContract.VISION_COLUMN, UserContract.IDCARD_COLUMN, UserContract.CREATETIME_COLUMN)
                .from(User.class)
                .where(UserContract.IDCARD_COLUMN+"=?", 1111)
                .with(user)
                .update();
```

###### 查询
```java
List<User> users = new Select(injector)
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN + "=?", "tubb")
                .or(UserContract.IDCARD_COLUMN+"=?", 19120)
                .select();

User userObj = new Select(injector, UserContract._ID_COLUMN, UserContract.USERNAME_COLUMN, UserContract.IDCARD_COLUMN)
                .from(User.class)
                .where(UserContract.USERNAME_COLUMN + "=?", "tubb")
                .selectSingle();
```

###### Join Select
```java
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
```

#### NOTE
`AutoDao`还处在快速开发阶段，在对源码不熟悉的情况下切勿应用在正式的项目上！
非常欢迎对这个项目感兴趣的童鞋提供建议，或者直接参与进来 : )

[1]:https://github.com/greenrobot/greenDAO
[2]:https://github.com/pardom/ActiveAndroid
[3]:https://github.com/LitePalFramework/LitePal
[4]:https://github.com/AutoDao/AutoDao