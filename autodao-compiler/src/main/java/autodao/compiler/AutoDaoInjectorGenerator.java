package autodao.compiler;

import android.support.annotation.NonNull;
import android.text.*;

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

    JavaFile generateAutoDaoInjector(HashMap<String, ClazzElement> clazzElements) {

        ClassName string = ClassName.get(String.class);
        ClassName modelDao = ClassName.get("autodao", "ModelDao");
        ClassName serializer = ClassName.get("autodao", "TypeSerializer");
        ClassName hashMap = ClassName.get("java.util", "HashMap");

        TypeName hashMapOfDao = ParameterizedTypeName.get(hashMap, string, modelDao);
        TypeName hashMapOfTable = ParameterizedTypeName.get(hashMap, string, string);
        TypeName hashMapOfSerializer = ParameterizedTypeName.get(hashMap, string, serializer);

        // select & selectSingle method
        TypeVariableName modelTypeVariableName = TypeVariableName.get("M extends autodao.Model");
        TypeVariableName listTypeGenerics = TypeVariableName.get("java.util.List<M>");
        TypeVariableName modelTypeGenerics = TypeVariableName.get("M");

        FieldSpec daoMapFieldSpec = generateDaoMapField(hashMap, hashMapOfDao);
        FieldSpec tableFieldSpec = generateTableField(hashMap, hashMapOfTable);
        FieldSpec serializerFieldSpec = generateSerializerMapField(hashMap, hashMapOfSerializer);

        MethodSpec.Builder flux = generateConstructorMethod(clazzElements);
        MethodSpec executePragma = generateExecutePragmaMethod();
        MethodSpec createTable = generateCreateTableMethod(clazzElements);
        MethodSpec createIndex = generateCreateIndexMethod(clazzElements);
        MethodSpec save = generateSaveMethod(modelDao);
        MethodSpec update = generateUpdateMethod(modelDao);
        MethodSpec delete = generateDeleteMethod();
        MethodSpec.Builder select = generateSelectMethod(modelDao, modelTypeVariableName, listTypeGenerics);
        MethodSpec.Builder selectSingle = generateSelectSingleMethod(modelDao, modelTypeVariableName, modelTypeGenerics);
        MethodSpec.Builder getModelDao = generateGetModelDaoMethod(modelDao);
        MethodSpec.Builder getTableName = generateGetTableNameMethod();
        MethodSpec.Builder getSerializer = generateGetSerializerMethod();

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(INJECTOR_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get("autodao", "Injector"))
                .addField(daoMapFieldSpec)
                .addField(tableFieldSpec)
                .addField(serializerFieldSpec)
                .addMethod(flux.build())
                .addMethod(save)
                .addMethod(update)
                .addMethod(delete)
                .addMethod(select.build())
                .addMethod(selectSingle.build())
                .addMethod(executePragma)
                .addMethod(createTable)
                .addMethod(createIndex)
                .addMethod(getModelDao.build())
                .addMethod(getTableName.build())
                .addMethod(getSerializer.build());

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
        getSerializerBuilder.addStatement("return $L.get(serializerCanonicalName)", INJECTOR_SERIALIZER_MAP_FIELD_NAME);
        return getSerializerBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateSelectSingleMethod(ClassName modelDao, TypeVariableName modelTypeVariableName, TypeVariableName modelTypeGenerics) {
        return MethodSpec.methodBuilder("selectSingle")
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(modelTypeVariableName)
                    .returns(modelTypeGenerics)
                    .addParameter(boolean.class, "distinct")
                    .addParameter(Class.class, "clazz")
                    .addParameter(String[].class, "columns")
                    .addParameter(String.class, "selection")
                    .addParameter(String[].class, "selectionArgs")
                    .addParameter(String.class, "groupBy")
                    .addParameter(String.class, "having")
                    .addParameter(String.class, "orderBy")
                    .addParameter(String.class, "limit")
                    .addStatement("String table = $L.get(clazz.getCanonicalName())", INJECTOR_TABLE_MAP_FIELD_NAME)
                    .addStatement("$T dao = $L.get(clazz.getCanonicalName())", modelDao, INJECTOR_DAO_MAP_FIELD_NAME)
                    .addStatement("return dao.selectSingle(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)");
    }

    @NonNull
    private MethodSpec.Builder generateSelectMethod(ClassName modelDao, TypeVariableName modelTypeVariableName, TypeVariableName listTypeGenerics) {
        return MethodSpec.methodBuilder("select")
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(modelTypeVariableName)
                    .returns(listTypeGenerics)
                    .addParameter(boolean.class, "distinct")
                    .addParameter(Class.class, "clazz")
                    .addParameter(String[].class, "columns")
                    .addParameter(String.class, "selection")
                    .addParameter(String[].class, "selectionArgs")
                    .addParameter(String.class, "groupBy")
                    .addParameter(String.class, "having")
                    .addParameter(String.class, "orderBy")
                    .addParameter(String.class, "limit")
                    .addStatement("String table = $L.get(clazz.getCanonicalName())", INJECTOR_TABLE_MAP_FIELD_NAME)
                    .addStatement("$T dao = $L.get(clazz.getCanonicalName())", modelDao, INJECTOR_DAO_MAP_FIELD_NAME)
                    .addStatement("return dao.select(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)");
    }

    @NonNull
    private MethodSpec generateDeleteMethod() {
        // int delete(Class clazz, String whereClause, String[] whereArgs);
        MethodSpec.Builder deleteBuilder = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(Class.class, "clazz")
                .addParameter(String.class, "whereClause")
                .addParameter(String[].class, "whereArgs");
        ClassName autodao = ClassName.get("autodao", "AutoDao");
        deleteBuilder.addStatement("String table = $L.get(clazz.getCanonicalName())", INJECTOR_TABLE_MAP_FIELD_NAME);
        deleteBuilder.addStatement("int number = $T.openDatabase().delete(table, whereClause, whereArgs)", autodao);
        deleteBuilder.addStatement("$T.closeDatabase()", autodao);
        deleteBuilder.addStatement("return number");
        return deleteBuilder.build();
    }

    @NonNull
    private MethodSpec generateUpdateMethod(ClassName modelDao) {
        // int update(Class clazz, Object obj, String whereClause, String[] whereArgs);
        MethodSpec.Builder updateBuilder = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(Class.class, "clazz")
                .addParameter(Object.class, "obj")
                .addParameter(String.class, "whereClause")
                .addParameter(String[].class, "whereArgs")
                .addParameter(String[].class, "targetColumns");
        updateBuilder.addStatement("$T dao = $L.get(clazz.getCanonicalName())", modelDao, INJECTOR_DAO_MAP_FIELD_NAME);
        updateBuilder.addStatement("return dao.update(obj, whereClause, whereArgs, targetColumns)");
        return updateBuilder.build();
    }

    @NonNull
    private MethodSpec generateSaveMethod(ClassName modelDao) {
        // long save(Class clazz, Object obj);
        MethodSpec.Builder saveBuilder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .returns(long.class)
                .addParameter(Class.class, "clazz")
                .addParameter(Object.class, "obj");
        saveBuilder.addStatement("$T dao = $L.get(clazz.getCanonicalName())", modelDao, INJECTOR_DAO_MAP_FIELD_NAME);
        saveBuilder.addStatement("return dao.save(obj)");
        return saveBuilder.build();
    }

    @NonNull
    private MethodSpec generateCreateIndexMethod(HashMap<String, ClazzElement> clazzElements) {
        MethodSpec.Builder createIndexBuilder = MethodSpec.methodBuilder("createIndex")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db");
        for (Map.Entry<String, ClazzElement> clazzElementEntry:clazzElements.entrySet()) {
            ClazzElement clazzElement = clazzElementEntry.getValue();
            if (clazzElement.getIndices() != null && clazzElement.getIndices().size() > 0){
                for (ClazzElement.Index index:clazzElement.getIndices()) {
                    ClassName indexClassName = ClassName.get(clazzElement.getPackageName(), clazzElement.getName() + TABLE_CONTRACT_SUFFIX);
                    createIndexBuilder.addStatement("db.execSQL($T."+getIndexFieldName(index.getName())+")", indexClassName);
                }
            }
        }
        return createIndexBuilder.build();
    }

    @NonNull
    private MethodSpec generateCreateTableMethod(HashMap<String, ClazzElement> clazzElements) {
        MethodSpec.Builder createTableBuilder = MethodSpec.methodBuilder("createTable")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db");

        for (Map.Entry<String, ClazzElement> clazzElementEntry:clazzElements.entrySet()) {
            ClazzElement clazzElement = clazzElementEntry.getValue();
            ClassName contract = ClassName.get(clazzElement.getPackageName(), clazzElement.getName() + TABLE_CONTRACT_SUFFIX);
            createTableBuilder.addStatement("db.execSQL($T."+getCreateTableContractName()+")", contract);
        }
        return createTableBuilder.build();
    }

    @NonNull
    private MethodSpec generateExecutePragmaMethod() {
        return MethodSpec.methodBuilder("executePragma")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db")
                    .addStatement("db.execSQL($S)", "PRAGMA foreign_keys = ON")
                    .build();
    }

    @NonNull
    private MethodSpec.Builder generateConstructorMethod(HashMap<String, ClazzElement> clazzElements) {
        // constructor method
        MethodSpec.Builder flux = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Map.Entry<String, ClazzElement> clazzElementEntry:clazzElements.entrySet()) {
            ClazzElement clazzElement = clazzElementEntry.getValue();
            ClassName daoClassName = ClassName.get(clazzElement.getPackageName(), clazzElement.getName()+TABLE_DAO_SUFFIX);
            // dao map
            flux.addStatement("$L.put($S, new $T())", INJECTOR_DAO_MAP_FIELD_NAME, clazzElement.getPackageName()+"."+clazzElement.getName(), daoClassName);
            // table map
            flux.addStatement("$L.put($S, $S)", INJECTOR_TABLE_MAP_FIELD_NAME, clazzElement.getPackageName()+"."+clazzElement.getName(), clazzElement.getTableName());
            // serializer map
            for (FieldElement fieldElement :
                    clazzElement.getFieldElements()) {
                if (fieldElement.getSerializer() != null){
                    FieldElement.Serializer serializer = fieldElement.getSerializer();
                    String serializedTypeCanonicalName = serializer.getSerializedTypeCanonicalName();
                    String serializerCanonicalName = serializer.getSerializerCanonicalName();
                    if (TextUtils.isEmpty(ColumnTypeUtils.getSQLiteColumnType(serializedTypeCanonicalName))){
                        throw new IllegalArgumentException("serializedTypeCanonicalName is not the base type");
                    }else if (TextUtils.isEmpty(serializerCanonicalName)){
                        throw new IllegalArgumentException("serializerCanonicalName can not be empty");
                    }
                    flux.addStatement("$L.put($S, new $L())", INJECTOR_SERIALIZER_MAP_FIELD_NAME, serializerCanonicalName, serializerCanonicalName);
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

}
