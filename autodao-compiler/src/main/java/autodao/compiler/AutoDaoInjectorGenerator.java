package autodao.compiler;

import android.support.annotation.NonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/18.
 */
public class AutoDaoInjectorGenerator extends ClazzGenerator {

    HashMap<String, ClazzElement> clazzElements;

    public AutoDaoInjectorGenerator(HashMap<String, ClazzElement> clazzElements) {
        this.clazzElements = clazzElements;
    }

    JavaFile generateAutoDaoInjector() {

        ClassName string = ClassName.get(String.class);
        ClassName modelDao = ClassName.get("autodao", "ModelDao");
        ClassName serializer = ClassName.get("autodao", "TypeSerializer");
        ClassName statement = ClassName.get("android.database.sqlite", "SQLiteStatement");
        ClassName hashMap = ClassName.get("java.util", "HashMap");
        ClassName lruCache = ClassName.get("android.support.v4.util", "LruCache");

        TypeName hashMapOfDao = ParameterizedTypeName.get(hashMap, string, modelDao);
        TypeName hashMapOfTable = ParameterizedTypeName.get(hashMap, string, string);
        TypeName hashMapOfSerializer = ParameterizedTypeName.get(hashMap, string, serializer);
        TypeName lruCacheOfStatement = ParameterizedTypeName.get(lruCache, string, statement);

        // select & selectSingle method
        TypeVariableName modelTypeVariableName = TypeVariableName.get("M extends autodao.Model");
        TypeVariableName listTypeGenerics = TypeVariableName.get("java.util.List<M>");
        TypeVariableName modelTypeGenerics = TypeVariableName.get("M");

        FieldSpec daoMapFieldSpec = generateDaoMapField(hashMap, hashMapOfDao);
        FieldSpec tableFieldSpec = generateTableField(hashMap, hashMapOfTable);
        FieldSpec serializerFieldSpec = generateSerializerMapField(hashMap, hashMapOfSerializer);

        FieldSpec statementFieldSpec = generateStatementMapField(lruCacheOfStatement, statement);

        MethodSpec.Builder flux = generateConstructorMethod();
        MethodSpec.Builder joinSelect = generateJoinSelectMethod(modelDao);
        MethodSpec.Builder getModelDao = generateGetModelDaoMethod(modelDao);
        MethodSpec.Builder getTableName = generateGetTableNameMethod();
        MethodSpec.Builder getSerializer = generateGetSerializerMethod();
        MethodSpec.Builder getStatement = generateGetStatementMethod();
        MethodSpec.Builder putStatement = generatePutStatementMethod();

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(INJECTOR_NAME)
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL)
                .addSuperinterface(ClassName.get("autodao", "Injector"))
                .addField(daoMapFieldSpec)
                .addField(tableFieldSpec)
                .addField(serializerFieldSpec)
                .addField(statementFieldSpec)
                .addMethod(flux.build())
                .addMethod(generateSqlSaveMethod(modelDao))
                .addMethod(generateSqlUpdateMethod(modelDao))
                .addMethod(generateSqlDeleteMethod(modelDao))
                .addMethod(generateSqlSelectMethod(modelDao, modelTypeVariableName, listTypeGenerics).build())
                .addMethod(generateSqlSelectSingleMethod(modelDao,
                        modelTypeVariableName,
                        modelTypeGenerics).build())
                .addMethod(joinSelect.build())
                .addMethod(getModelDao.build())
                .addMethod(getTableName.build())
                .addMethod(getSerializer.build())
                .addMethod(getStatement.build())
                .addMethod(putStatement.build());

        return JavaFile.builder(INJECTOR_PKG, typeSpecBuilder.build()).build();
    }

    @NonNull
    private MethodSpec.Builder generateGetTableNameMethod() {
        // String getTableName(String clazzName);
        MethodSpec.Builder getTableNameBuilder = MethodSpec.methodBuilder("getTableName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "clazzName");
        getTableNameBuilder.addStatement("return $L.get(clazzName)", INJECTOR_TABLE_MAP_FIELD_NAME);
        return getTableNameBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateGetModelDaoMethod(ClassName modelDao) {
        // ModelDao getModelDao(String clazzName);
        MethodSpec.Builder getModelDaoBuilder = MethodSpec.methodBuilder("getModelDao")
                .addModifiers(Modifier.PUBLIC)
                .returns(modelDao)
                .addParameter(String.class, "clazzName");
        getModelDaoBuilder.addStatement("return $L.get(clazzName)", INJECTOR_DAO_MAP_FIELD_NAME);
        return getModelDaoBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateGetSerializerMethod() {
        MethodSpec.Builder getSerializerBuilder = MethodSpec.methodBuilder("getSerializer")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("autodao", "TypeSerializer"))
                .addParameter(String.class, "serializerCanonicalName");
        getSerializerBuilder.addStatement("return $L.get(serializerCanonicalName)",
                INJECTOR_SERIALIZER_MAP_FIELD_NAME);
        return getSerializerBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateGetStatementMethod() {
        MethodSpec.Builder getStatementBuilder = MethodSpec.methodBuilder("getStatement")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("android.database.sqlite", "SQLiteStatement"))
                .addParameter(String.class, "mappingSql");
        getStatementBuilder.addStatement("return $L.get(mappingSql)",
                INJECTOR_STATEMEN_MAP_FIELD_NAME);
        return getStatementBuilder;
    }

    @NonNull
    private MethodSpec.Builder generatePutStatementMethod() {
        MethodSpec.Builder putStatementBuilder = MethodSpec.methodBuilder("putStatement")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "mappingSql")
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteStatement"), "statement");
        putStatementBuilder.addStatement("$L.put(mappingSql, statement)",
                INJECTOR_STATEMEN_MAP_FIELD_NAME);
        return putStatementBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateJoinSelectMethod(ClassName modelDao) {
        ClassName cursorClass = ClassName.get("android.database", "Cursor");
        ClassName operatorClass = ClassName.get("autodao", "Operator");
        return MethodSpec.methodBuilder("joinSelect")
                .addModifiers(Modifier.PUBLIC)
                .returns(cursorClass)
                .addParameter(operatorClass, "operator")
                .addStatement("$T dao = $L.get(operator.getModelCanonicalName())",
                        modelDao,
                        INJECTOR_DAO_MAP_FIELD_NAME)
                .addStatement("return dao.joinSelect(operator)");
    }

    @NonNull
    private String getJoinSelectParams() {
        return "distinct,table,columns,selection,selectionArgs,groupBy,having,orderBy,limit,joinTable,joinType,on,fromTableAlias,joinTableAlias";
    }

    @NonNull
    private String getSelectParams() {
        return "distinct,table,columns,selection,selectionArgs,groupBy,having,orderBy,limit";
    }

    @NonNull
    private MethodSpec.Builder generateSqlSelectMethod(ClassName modelDao,
                                                       TypeVariableName modelTypeVariableName,
                                                       TypeVariableName listTypeGenerics) {
        ClassName operatorClass = ClassName.get("autodao", "Operator");
        return MethodSpec.methodBuilder("select")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(modelTypeVariableName)
                .returns(listTypeGenerics)
                .addParameter(operatorClass, "operator")
                .addStatement("$T dao = $L.get(operator.getModelCanonicalName())",
                        modelDao,
                        INJECTOR_DAO_MAP_FIELD_NAME)
                .addStatement("return dao.select(operator)");
    }

    @NonNull
    private MethodSpec.Builder generateSqlSelectSingleMethod(ClassName modelDao,
                                                             TypeVariableName modelTypeVariableName,
                                                             TypeVariableName listTypeGenerics) {
        ClassName operatorClass = ClassName.get("autodao", "Operator");
        return MethodSpec.methodBuilder("selectSingle")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(modelTypeVariableName)
                .returns(listTypeGenerics)
                .addParameter(operatorClass, "operator")
                .addStatement("$T dao = $L.get(operator.getModelCanonicalName())",
                        modelDao,
                        INJECTOR_DAO_MAP_FIELD_NAME)
                .addStatement("return dao.selectSingle(operator)");
    }

    @NonNull
    private MethodSpec generateSqlUpdateMethod(ClassName modelDao) {
        ClassName operator = ClassName.get("autodao", "Operator");
        // int delete(Class clazz, String whereClause, String[] whereArgs);
        MethodSpec.Builder deleteBuilder = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(operator, "operator");
        deleteBuilder
                .addStatement("$T dao = $L.get(operator.getModelCanonicalName())",
                        modelDao,
                        INJECTOR_DAO_MAP_FIELD_NAME)
                .addStatement("return dao.update(operator)");
        return deleteBuilder.build();
    }

    @NonNull
    private MethodSpec generateSqlDeleteMethod(ClassName modelDao) {
        ClassName operator = ClassName.get("autodao", "Operator");
        // int delete(Class clazz, String whereClause, String[] whereArgs);
        MethodSpec.Builder deleteBuilder = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(operator, "operator");
        deleteBuilder
                .addStatement("$T dao = $L.get(operator.getModelCanonicalName())",
                        modelDao,
                        INJECTOR_DAO_MAP_FIELD_NAME)
                .addStatement("return dao.delete(operator)");
        return deleteBuilder.build();
    }

    @NonNull
    private MethodSpec generateSqlSaveMethod(ClassName modelDao) {
        ClassName operator = ClassName.get("autodao", "Operator");
        // int delete(Class clazz, String whereClause, String[] whereArgs);
        MethodSpec.Builder deleteBuilder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .returns(long.class)
                .addParameter(operator, "operator");
        deleteBuilder
                .addStatement("$T dao = $L.get(operator.getModelCanonicalName())",
                        modelDao,
                        INJECTOR_DAO_MAP_FIELD_NAME)
                .addStatement("return dao.save(operator)");
        return deleteBuilder.build();
    }

    @NonNull
    private MethodSpec.Builder generateConstructorMethod() {

        ClassName sqlite = ClassName.get("android.database.sqlite", "SQLiteDatabase");

        MethodSpec.Builder flux = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqlite, "db");

        for (Map.Entry<String, ClazzElement> clazzElementEntry : clazzElements.entrySet()) {
            ClazzElement clazzElement = clazzElementEntry.getValue();
            ClassName daoClassName = ClassName.get(clazzElement.getPackageName(),
                    clazzElement.getName() + TABLE_DAO_SUFFIX);
            // dao map
            flux.addStatement("$L.put($S, new $T(db, this))",
                    INJECTOR_DAO_MAP_FIELD_NAME,
                    clazzElement.getPackageName() + "." + clazzElement.getName(),
                    daoClassName);
            // table map
            flux.addStatement("$L.put($S, $S)",
                    INJECTOR_TABLE_MAP_FIELD_NAME,
                    clazzElement.getPackageName() + "." + clazzElement.getName(),
                    clazzElement.getTableName());
            // serializer map
            for (FieldElement fieldElement : clazzElement.getFieldElements()) {
                if (fieldElement.getSerializer() != null) {
                    FieldElement.Serializer serializer = fieldElement.getSerializer();
                    String serializedTypeCN = serializer.getSerializedTypeCanonicalName();
                    String serializerCN = serializer.getSerializerCanonicalName();
                    if (TextUtils.isEmpty(ColumnTypeUtils.getSQLiteColumnType(serializedTypeCN))) {
                        throw new IllegalArgumentException("serializedTypeCN is not the base type");
                    } else if (TextUtils.isEmpty(serializerCN)) {
                        throw new IllegalArgumentException("serializerCN can not be empty");
                    }
                    flux.addStatement("$L.put($S, new $L())",
                            INJECTOR_SERIALIZER_MAP_FIELD_NAME,
                            serializerCN,
                            serializerCN);
                }
            }
        }
        return flux;
    }

    @NonNull
    private FieldSpec generateTableField(ClassName hashMap, TypeName hashMapOfTable) {
        return FieldSpec.builder(hashMapOfTable, INJECTOR_TABLE_MAP_FIELD_NAME)
                .addModifiers(Modifier.PRIVATE)
                .initializer("new $T()", hashMap)
                .build();
    }

    @NonNull
    private FieldSpec generateDaoMapField(ClassName hashMap, TypeName hashMapOfDao) {
        return FieldSpec.builder(hashMapOfDao, INJECTOR_DAO_MAP_FIELD_NAME)
                .addModifiers(Modifier.PRIVATE)
                .initializer("new $T()", hashMap)
                .build();
    }

    @NonNull
    private FieldSpec generateSerializerMapField(ClassName hashMap, TypeName hashMapOfDao) {
        return FieldSpec.builder(hashMapOfDao, INJECTOR_SERIALIZER_MAP_FIELD_NAME)
                .addModifiers(Modifier.PRIVATE)
                .initializer("new $T()", hashMap)
                .build();
    }

    @NonNull
    private FieldSpec generateStatementMapField(TypeName lruCacheOfStatement, ClassName statement) {

        /**
         *
         * /**
         new LruCache<String, SQLiteStatement>(3) {
        @Override protected void entryRemoved(boolean evicted, String key, SQLiteStatement oldValue, SQLiteStatement newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
        if (evicted && oldValue != null) {
        oldValue.close();
        }
        }
        };
         */


        return FieldSpec.builder(lruCacheOfStatement, INJECTOR_STATEMEN_MAP_FIELD_NAME)
                .addModifiers(Modifier.PRIVATE)
                .initializer("new $T(3){$L}", lruCacheOfStatement, "@Override\n" +
                        "        protected void entryRemoved(boolean evicted, String key, SQLiteStatement oldValue, SQLiteStatement newValue) {\n" +
                        "        super.entryRemoved(evicted, key, oldValue, newValue);\n" +
                        "        if (evicted && oldValue != null) {\n" +
                        "        oldValue.close();\n" +
                        "        }\n" +
                        "        }")
                .build();
    }

}
