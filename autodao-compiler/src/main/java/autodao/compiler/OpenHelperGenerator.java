package autodao.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/7/1.
 */
public class OpenHelperGenerator extends ClazzGenerator {

    HashMap<String, ClazzElement> clazzElements;

    public OpenHelperGenerator(HashMap<String, ClazzElement> clazzElements) {
        this.clazzElements = clazzElements;
    }

    public JavaFile generateOpenHelper() {

        TypeSpec.Builder typeSpecBuilder = TypeSpec
                .classBuilder("AutoSQLiteOpenHelper")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(ClassName.get("android.database.sqlite", "SQLiteOpenHelper"));

        ClassName cursorFactoryClass = ClassName.get("android.database.sqlite.SQLiteDatabase", "CursorFactory");
        ClassName sqliteDatabaseClass = ClassName.get("android.database.sqlite", "SQLiteDatabase");

        ClassName autoDaoInjectorClass = ClassName.get("autodao", "AutoDaoInjector");
        ClassName injectorClass = ClassName.get("autodao", "Injector");

        // Context context, String name, SQLiteDatabase.CursorFactory factory, int version
        MethodSpec.Builder constructorWithVersion = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(String.class, "name")
                .addParameter(cursorFactoryClass, "factory")
                .addParameter(int.class, "version")
                .addStatement("super(context, name, factory, version)");
        // Context context, String name, SQLiteDatabase.CursorFactory factory, int version
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(String.class, "name")
                .addParameter(cursorFactoryClass, "factory")
                .addStatement("this(context, name, factory, 1)");

        MethodSpec.Builder onCreate = MethodSpec.methodBuilder("onCreate")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqliteDatabaseClass, "db")
                .addStatement("createAllTables(db)");

        MethodSpec.Builder createAllTables = MethodSpec.methodBuilder("createAllTables")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(sqliteDatabaseClass, "db");

        for (Map.Entry<String, ClazzElement> entry: clazzElements.entrySet()) {
            ClazzElement clazzElement = entry.getValue();
            ClassName contract = ClassName.get(clazzElement.getPackageName(),
                    clazzElement.getName() + ClazzGenerator.TABLE_CONTRACT_SUFFIX);
            createAllTables.addStatement("$T.createTable(db)", contract);
        }

        for (Map.Entry<String, ClazzElement> entry: clazzElements.entrySet()) {
            ClazzElement clazzElement = entry.getValue();
            ClassName contract = ClassName.get(clazzElement.getPackageName(),
                    clazzElement.getName() + ClazzGenerator.TABLE_CONTRACT_SUFFIX);
            createAllTables.addStatement("$T.createIndex(db)", contract);
        }

        MethodSpec.Builder dropAllTables = MethodSpec.methodBuilder("dropAllTables")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(sqliteDatabaseClass, "db");
        for (Map.Entry<String, ClazzElement> entry: clazzElements.entrySet()) {
            ClazzElement clazzElement = entry.getValue();
            ClassName contract = ClassName.get(clazzElement.getPackageName(),
                    clazzElement.getName() + ClazzGenerator.TABLE_CONTRACT_SUFFIX);
            dropAllTables.addStatement("$T.dropTable(db)", contract);
        }

        //     if (injector == null) injector = new AutoDaoInjector(getWritableDatabase());
        MethodSpec.Builder getInjector = MethodSpec.methodBuilder("getInjector")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .addParameter(sqliteDatabaseClass, "db")
                .returns(injectorClass)
                .beginControlFlow("if (injector == null)")
                .addStatement("injector = new $T(db)", autoDaoInjectorClass)
                .endControlFlow()
                .addStatement("return injector");

        /**
         *
         * if (!db.isReadOnly()) {
         // Enable foreign key constraints
         db.execSQL("PRAGMA foreign_keys=ON;");
         }
         */

        MethodSpec.Builder onOpen = MethodSpec.methodBuilder("onOpen")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqliteDatabaseClass, "db")
                .addStatement("super.onOpen(db)")
                .beginControlFlow("if (!db.isReadOnly()) ")
                .addStatement("db.execSQL($S)", "PRAGMA foreign_keys=ON;")
                .endControlFlow();

        FieldSpec.Builder injectorField = FieldSpec.builder(injectorClass, "injector");
        FieldSpec.Builder openCounterField = FieldSpec.builder(AtomicInteger.class, "mOpenCounter")
                .initializer("new $T()", AtomicInteger.class);
        FieldSpec.Builder databaseField = FieldSpec.builder(sqliteDatabaseClass, "mDatabase");

        /**
         *
         * SQLiteDatabase openDatabase() {
         if (mOpenCounter.incrementAndGet() == 1) {
         mDatabase = AutoDao.getSQLiteOpenHelper().getWritableDatabase();
         }
         return mDatabase;
         }

         void closeDatabase() {
         if (mOpenCounter.decrementAndGet() == 0) {
         mDatabase.close();
         }
         }
         *
         */

        MethodSpec.Builder openDatabase = MethodSpec.methodBuilder("openDatabase")
                .addModifiers(Modifier.PUBLIC)
                .returns(sqliteDatabaseClass)
                .beginControlFlow("if (mOpenCounter.incrementAndGet() == 1) ")
                .addStatement("mDatabase = getWritableDatabase()")
                .endControlFlow()
                .addStatement("return mDatabase");

        MethodSpec.Builder closeDatabase = MethodSpec.methodBuilder("closeDatabase")
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("if (mOpenCounter.decrementAndGet() == 0) ")
                .addStatement("mDatabase.close()")
                .endControlFlow();

        typeSpecBuilder
                .addField(databaseField.build())
                .addField(openCounterField.build())
                .addField(injectorField.build())
                .addMethod(constructor.build())
                .addMethod(constructorWithVersion.build())
                .addMethod(onCreate.build())
                .addMethod(createAllTables.build())
                .addMethod(dropAllTables.build())
                .addMethod(getInjector.build())
                .addMethod(onOpen.build())
                .addMethod(openDatabase.build())
                .addMethod(closeDatabase.build());

        return JavaFile.builder("autodao", typeSpecBuilder.build()).build();
    }
}
