package autodao.compiler;

import android.support.annotation.NonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/18.
 */
public class ModelDaoGenerator extends ClazzGenerator {

    public JavaFile generateTableDao(HashMap<String, ClazzElement> clazzElements, ClazzElement clazzElement) {
        String modelName = clazzElement.getName();
        String clazzName = modelName + TABLE_DAO_SUFFIX;
        String objName = modelName.replaceFirst(modelName, modelName.toLowerCase());

        ClassName modelDao = ClassName.get("autodao", "ModelDao");
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(modelDao);

        ClassName model = ClassName.get(clazzElement.getPackageName(), modelName);
        ClassName sqlite = ClassName.get("android.database.sqlite", "SQLiteDatabase");
        ClassName contentValues = ClassName.get("android.content", "ContentValues");
        ClassName autodao = ClassName.get("autodao", "AutoDao");
        ClassName cursor = ClassName.get("android.database", "Cursor");

        MethodSpec.Builder saveBuilder = generateSaveMethod(clazzElements, clazzElement, objName, model, sqlite, contentValues, autodao);
        MethodSpec.Builder updateBuilder = generateUpdateMethod(clazzElements, clazzElement, objName, model, sqlite, contentValues, autodao);

        /**
         <M extends Model> List<M> select(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs
         , String groupBy, String having, String orderBy, String limit);
         <M extends Model> M selectSingle(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
         */

        TypeVariableName modelTypeVariableName = TypeVariableName.get("M extends autodao.Model");
        TypeVariableName listTypeGenerics = TypeVariableName.get("java.util.List<M>");
        TypeVariableName modelTypeGenerics = TypeVariableName.get("M");

        MethodSpec.Builder selectBuilder = generateSelectMethod(clazzElements, clazzElement, objName, modelDao, model, sqlite, autodao, cursor, modelTypeVariableName, listTypeGenerics);
        MethodSpec.Builder selectSingleBuilder = generateSelectSingleMethod(clazzElements, clazzElement, objName, modelDao, model, sqlite, autodao, cursor, modelTypeVariableName, modelTypeGenerics);

//        MethodSpec.Builder selectMappingBuilder = generateSelectMethod(clazzElements, clazzElement, objName, modelDao, model, sqlite, autodao, cursor, modelTypeVariableName, listTypeGenerics);
//        MethodSpec.Builder selectSingleMappingBuilder = generateSelectSingleMethod(clazzElements, clazzElement, objName, modelDao, model, sqlite, autodao, cursor, modelTypeVariableName, modelTypeGenerics);

        typeSpecBuilder.addMethod(saveBuilder.build());
        typeSpecBuilder.addMethod(updateBuilder.build());
//        typeSpecBuilder.addMethod(selectMappingBuilder.build());
//        typeSpecBuilder.addMethod(selectSingleMappingBuilder.build());
        typeSpecBuilder.addMethod(selectBuilder.build());
        typeSpecBuilder.addMethod(selectSingleBuilder.build());

        return JavaFile.builder(clazzElement.getPackageName(), typeSpecBuilder.build()).build();
    }

    @NonNull
    private MethodSpec.Builder generateSelectSingleMethod(HashMap<String, ClazzElement> clazzElements
            , ClazzElement clazzElement
            , String objName
            , ClassName modelDao
            , ClassName model
            , ClassName sqlite
            , ClassName autodao
            , ClassName cursor
            , TypeVariableName modelTypeVariableName
            , TypeVariableName modelTypeGenerics) {

        return generateSelectOrSelectSingleMethod(clazzElements, clazzElement, objName, modelDao, model, sqlite, autodao, cursor, modelTypeVariableName, modelTypeGenerics, false);
    }

    @NonNull
    private MethodSpec.Builder generateSelectMethod(HashMap<String, ClazzElement> clazzElements
            , ClazzElement clazzElement
            , String objName
            , ClassName modelDao
            , ClassName model
            , ClassName sqlite
            , ClassName autodao
            , ClassName cursor
            , TypeVariableName modelTypeVariableName
            , TypeVariableName listTypeGenerics) {

        return generateSelectOrSelectSingleMethod(clazzElements, clazzElement, objName, modelDao, model, sqlite, autodao, cursor, modelTypeVariableName, listTypeGenerics, true);
    }

    @NonNull
    private MethodSpec.Builder generateSelectOrSelectSingleMethod(HashMap<String, ClazzElement> clazzElements
            , ClazzElement clazzElement
            , String objName
            , ClassName modelDao
            , ClassName model
            , ClassName sqlite
            , ClassName autodao
            , ClassName cursor
            , TypeVariableName modelTypeVariableName
            , TypeVariableName returnType
            , boolean isCreateSelectMethod) {

        MethodSpec.Builder selectBuilder = MethodSpec.methodBuilder(isCreateSelectMethod?"select":"selectSingle")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(modelTypeVariableName)
                .returns(returnType)
                .addParameter(boolean.class, "distinct")
                .addParameter(String.class, "table")
                .addParameter(String[].class, "columns")
                .addParameter(String.class, "selection")
                .addParameter(String[].class, "selectionArgs")
                .addParameter(String.class, "groupBy")
                .addParameter(String.class, "having")
                .addParameter(String.class, "orderBy")
                .addParameter(String.class, "limit");
//        if (isMapping)
//            selectBuilder.addParameter(Object.class, "mappingObj");
        String objNameList = objName + "List";
        if (isCreateSelectMethod)
            selectBuilder.addStatement("$T<$T> $L = null", List.class, model, objNameList);
        selectBuilder.addStatement("$T cursor = null", cursor);
        selectBuilder.beginControlFlow("try");
        //---------- try block start ---------------------
        // db
        selectBuilder.addStatement("$T db = $T.openDatabase()", sqlite, autodao);
        selectBuilder.addStatement("cursor = db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)");

        if (isCreateSelectMethod)
            selectBuilder.addStatement("$L = new $T<>(cursor.getCount())", objNameList, ArrayList.class);
        /**
         List<String> targetColumns = null;
         if (columns != null)
         targetColumns = Arrays.asList(columns);
         */
        selectBuilder.addStatement("$T<String> targetColumns = null", List.class);

        selectBuilder.beginControlFlow("if (columns != null)");
        selectBuilder.addStatement("targetColumns = $T.asList(columns)", Arrays.class);
        selectBuilder.endControlFlow();

        selectBuilder.beginControlFlow("while(cursor.moveToNext())");
        selectBuilder.addStatement("$T $L = new $T()", model, objName, model);
        for (FieldElement fieldElement : clazzElement.getFieldElements()) {
            String columnName = fieldElement.getColumnName();
            String fieldName = fieldElement.getName();
            String columnType = fieldElement.getType();

            // String name = cursor.getString(cursor.getColumnIndex("userName"));
            selectBuilder.beginControlFlow("if(targetColumns == null || targetColumns.contains($S))", columnName);

            if (ColumnTypeUtils.isBoolean(columnType)){
                selectBuilder.addStatement("$L $L = cursor.$L(cursor.getColumnIndex($S)) == 1"
                        , columnType, fieldName
                        , ColumnTypeUtils.getSQLiteTypeMethod(columnType)
                        , columnName);
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            }else if (ColumnTypeUtils.isChar(columnType) || ColumnTypeUtils.isByte(columnType)){
                selectBuilder.addStatement("$L $L = ($L)(cursor.$L(cursor.getColumnIndex($S)))"
                        , columnType, fieldName, columnType
                        , ColumnTypeUtils.getSQLiteTypeMethod(columnType)
                        , columnName);
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            }else if (columnType.startsWith("java.util.List")
                    || columnType.startsWith("java.util.ArrayList")){
                int start = columnType.indexOf("<");
                int end  = columnType.indexOf(">");
                String generic = columnType.substring(start+1, end);
                // one to many
                if (clazzElements.containsKey(generic)){
                    for (FieldElement fe : clazzElements.get(generic).getFieldElements()) {
                        if (fe.getColumnName().equals(fieldElement.getMappingColumnName())){
                            ClazzElement mappingClazzElement = clazzElements.get(generic);
                            selectBuilder.addStatement("$T modelDao = $T.getInjector().getModelDao($S)"
                                    , modelDao, autodao
                                    , mappingClazzElement.getPackageName()+"."+mappingClazzElement.getName());
                            selectBuilder.addStatement("$L $L = cursor.$L(cursor.getColumnIndex($S))"
                                    , fe.getType()
                                    , fe.getName()
                                    , ColumnTypeUtils.getSQLiteTypeMethod(fe.getType())
                                    , columnName);
                            selectBuilder.addStatement("$T mappingSelectionArgs = new $T{$L}", String[].class, String[].class, fe.getName());
//                            if (!isMapping){
                                selectBuilder.addStatement("$L $L = modelDao.select(false, $S, null, $S, mappingSelectionArgs, null, null, null, null)"
                                        , columnType
                                        , fieldName
                                        , mappingClazzElement.getTableName()
                                        , fieldElement.getMappingColumnName()+"=?");
                                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
//                            } else {
//                                selectBuilder.beginControlFlow("if($L.getClass().getCanonicalName().equals($S))", "mappingObj", columnType);
//                                if (fieldElement.getModifiers().contains(Modifier.PUBLIC)){
//                                    selectBuilder.addStatement("$L.$L = ($L)$L", objName, fieldName, columnType, "mappingObj");
//                                }else {
//                                    String fieldSetName = buildAccessorName("set", fieldName);
//                                    selectBuilder.addStatement("$L.$L($L)", objName, fieldSetName, fieldName);
//                                }
//                                selectBuilder.endControlFlow();
//                                selectBuilder.beginControlFlow("else");
//                                selectBuilder.addStatement("$L $L = modelDao.select(false, $S, null, $S, mappingSelectionArgs, null, null, null, null, ($T)$L)"
//                                        , columnType
//                                        , fieldName
//                                        , mappingClazzElement.getTableName()
//                                        , fieldElement.getMappingColumnName()+"=?"
//                                        , Object.class
//                                        , objName);
//                                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
//                                selectBuilder.endControlFlow();
//                            }

                            break;
                        }
                    }
                }else {
                    throw new IllegalArgumentException("Must use generic type <"+generic+">");
                }
            }else if (ColumnTypeUtils.getSQLiteColumnType(columnType) == null){ // one to one

                selectBuilder.addStatement("$T modelDao = $T.getInjector().getModelDao($S)"
                        , modelDao
                        , autodao
                        , columnType);
                selectBuilder.addStatement("$T mappingTableName = $T.getInjector().getTableName($S)"
                        , String.class
                        , autodao
                        , columnType);
                selectBuilder.addStatement("$L _id = cursor.$L(cursor.getColumnIndex($S))"
                        , long.class
                        , ColumnTypeUtils.getSQLiteTypeMethod("long")
                        , columnName);
//                if (!isMapping){
                    selectBuilder.addStatement("$L $L = modelDao.selectSingle(false, mappingTableName, null, $S, new $T[]{$T.valueOf(_id)}, null, null, null, null)"
                            , columnType
                            , fieldName
                            , "_id=?"
                            , String.class
                            , String.class);
                    generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
//                }else {
//                    selectBuilder.beginControlFlow("if($L.getClass().getCanonicalName().equals($S))", "mappingObj", columnType);
//                    if (fieldElement.getModifiers().contains(Modifier.PUBLIC)){
//                        selectBuilder.addStatement("$L.$L = ($L)$L", objName, fieldName, columnType, "mappingObj");
//                    }else {
//                        String fieldSetName = buildAccessorName("set", fieldName);
//                        selectBuilder.addStatement("$L.$L($L)", objName, fieldSetName, fieldName);
//                    }
//                    selectBuilder.endControlFlow();
//                    selectBuilder.beginControlFlow("else");
//                    selectBuilder.addStatement("$L $L = modelDao.selectSingle(false, mappingTableName, null, $S, new $T[]{$T.valueOf(_id)}, null, null, null, null, ($T)$L)"
//                            , columnType
//                            , fieldName
//                            , "_id=?"
//                            , String.class
//                            , String.class
//                            , Object.class
//                            , objName);
//                    generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
//                    selectBuilder.endControlFlow();
//                }

            }else {
                selectBuilder.addStatement("$L $L = cursor.$L(cursor.getColumnIndex($S))"
                        , columnType, fieldName
                        , ColumnTypeUtils.getSQLiteTypeMethod(columnType)
                        , columnName);
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            }
            selectBuilder.endControlFlow();
        }
        if (isCreateSelectMethod)
            selectBuilder.addStatement("$L.add($L)", objNameList, objName);
        else
            selectBuilder.addStatement("return (M)$L", objName);
        selectBuilder.endControlFlow();
        //---------- try block end ---------------------
        selectBuilder.endControlFlow();
        selectBuilder.beginControlFlow("catch($T e)", RuntimeException.class);
        // --------- catch block start -----------
        selectBuilder.addStatement("throw e");
        // --------- catch block end -------------
        selectBuilder.endControlFlow();
        selectBuilder.beginControlFlow("finally");
        // --------- finally block start -----------
        selectBuilder.addStatement("$T.closeDatabase()", autodao).beginControlFlow("if(cursor != null)").addStatement("cursor.close()").endControlFlow();
        // --------- finally block end -------------
        selectBuilder.endControlFlow();
        if (isCreateSelectMethod)
            selectBuilder.addStatement("return ($T<M>)$L", List.class, objNameList);
        else
            selectBuilder.addStatement("return null");

        return selectBuilder;
    }


    private void generateSetFieldValueStatement(String objName, MethodSpec.Builder selectBuilder, FieldElement fieldElement, String fieldName) {
        if (fieldElement.getModifiers().contains(Modifier.PUBLIC)){
            selectBuilder.addStatement("$L.$L = $L", objName, fieldName, fieldName);
        }else {
            String fieldSetName = buildAccessorName("set", fieldName);
            selectBuilder.addStatement("$L.$L($L)", objName, fieldSetName, fieldName);
        }
    }

    @NonNull
    private MethodSpec.Builder generateUpdateMethod(HashMap<String, ClazzElement> clazzElements, ClazzElement clazzElement, String objName, ClassName model, ClassName sqlite, ClassName contentValues, ClassName autodao) {
        // update method
        MethodSpec.Builder updateBuilder = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(Object.class, "obj")
                .addParameter(String.class, "whereClause")
                .addParameter(String[].class, "whereArgs")
                .addParameter(String[].class, "targetColumns");

        updateBuilder.addStatement("$T affectedRows = -1", int.class);

        updateBuilder.beginControlFlow("try");

        updateBuilder.addStatement("$T $L = ($T)obj", model, objName, model);
        updateBuilder.addStatement("$T db = $T.openDatabase()", sqlite, autodao);
        updateBuilder.addStatement("$T cv = new $T()", contentValues, contentValues);
        genetateContentValues(clazzElements, clazzElement, objName, updateBuilder, true);
        updateBuilder.addStatement("affectedRows = db.update($S, cv, whereClause, whereArgs)", clazzElement.getTableName());

        updateBuilder.endControlFlow();
        updateBuilder.beginControlFlow("catch($T e)", RuntimeException.class);
        updateBuilder.addStatement("throw e");
        updateBuilder.endControlFlow();
        updateBuilder.beginControlFlow("finally");
        updateBuilder.addStatement("$T.closeDatabase()", autodao);
        updateBuilder.endControlFlow();

        updateBuilder.addStatement("return affectedRows");
        return updateBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateSaveMethod(HashMap<String, ClazzElement> clazzElements, ClazzElement clazzElement, String objName, ClassName model, ClassName sqlite, ClassName contentValues, ClassName autodao) {
        // save method
        MethodSpec.Builder saveBuilder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .returns(long.class)
                .addParameter(Object.class, "obj");

        saveBuilder.addStatement("$T _id = -1", long.class);

        saveBuilder.beginControlFlow("try");

        saveBuilder.addStatement("$T $L = ($T)obj", model, objName, model);
        saveBuilder.addStatement("$T db = $T.openDatabase()", sqlite, autodao);
        saveBuilder.addStatement("$T cv = new $T()", contentValues, contentValues);
        genetateContentValues(clazzElements, clazzElement, objName, saveBuilder, false);
        saveBuilder.addStatement("_id = db.insert($S, $L, cv)", clazzElement.getTableName(), null);
        saveBuilder.addStatement("$L._id = _id", objName);

        saveBuilder.endControlFlow();
        saveBuilder.beginControlFlow("catch($T e)", RuntimeException.class);
        saveBuilder.addStatement("throw e");
        saveBuilder.endControlFlow();
        saveBuilder.beginControlFlow("finally");
        saveBuilder.addStatement("$T.closeDatabase()", autodao);
        saveBuilder.endControlFlow();

        saveBuilder.addStatement("return _id");
        return saveBuilder;
    }

    private void genetateContentValues(HashMap<String, ClazzElement> clazzElements, ClazzElement clazzElement, String objName, MethodSpec.Builder saveBuilder, boolean filterColumns) {

        if(filterColumns){
            saveBuilder
                    .addStatement("$T shouldFilter = true", boolean.class)
                    .addStatement("$T targetColumnList = null", List.class)
                    .beginControlFlow("if (targetColumns != null)")
                    .addStatement("targetColumnList = $T.asList(targetColumns)", Arrays.class)
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("shouldFilter = false")
                    .endControlFlow();
        }

        for (FieldElement fieldElement:clazzElement.getFieldElements()){

            if ("_id".equals(fieldElement.getName())) continue;

            if (filterColumns){
                saveBuilder.beginControlFlow("if (shouldFilter)");
                saveBuilder.beginControlFlow("if (targetColumnList.contains($S))", fieldElement.getColumnName());
                putContentValues(clazzElements, objName, saveBuilder, fieldElement);
                saveBuilder.endControlFlow();
                saveBuilder.endControlFlow();
                saveBuilder.beginControlFlow("else");
                putContentValues(clazzElements, objName, saveBuilder, fieldElement);
                saveBuilder.endControlFlow();
            }else {
                putContentValues(clazzElements, objName, saveBuilder, fieldElement);
            }
        }
    }

    private void putContentValues(HashMap<String, ClazzElement> clazzElements, String objName, MethodSpec.Builder saveBuilder, FieldElement fieldElement) {
        String columnType = fieldElement.getType();
        if (TextUtils.isEmpty(ColumnTypeUtils.getSQLiteColumnType(columnType))){ // not the base type
            if (clazzElements.containsKey(fieldElement.getType())){ // one to one
                ClazzElement fieldClazz = clazzElements.get(fieldElement.getType());
                ClassName fieldClazzName = ClassName.get(fieldClazz.getPackageName(), fieldClazz.getName());
                saveBuilder.addStatement("$T $L = $L.$L"
                        , fieldClazzName
                        , fieldElement.getName()
                        , objName
                        , fieldElement.getModifiers().contains(Modifier.PUBLIC)?fieldElement.getName():buildAccessorName("get", fieldElement.getName())+"()");
                saveBuilder.beginControlFlow("if ($L == null)", fieldElement.getName())
                        .addStatement("cv.put($S, -1)", fieldElement.getColumnName())
                        .endControlFlow();
                saveBuilder.beginControlFlow("else")
                        .addStatement("cv.put($S, $L._id)", fieldElement.getColumnName(), fieldElement.getName())
                        .endControlFlow();
            }else {
                if (columnType.startsWith("java.util.List")
                        || columnType.startsWith("java.util.ArrayList")){

                    saveBuilder.addStatement("$L $L = $L.$L"
                            , fieldElement.getType()
                            , fieldElement.getName()
                            , objName
                            , fieldElement.getModifiers().contains(Modifier.PUBLIC)?fieldElement.getName():buildAccessorName("get", fieldElement.getName())+"()");
                    FieldElement targetFieldElement = null;
                    int start = columnType.indexOf("<");
                    int end  = columnType.indexOf(">");
                    String generic = columnType.substring(start+1, end);
                    if (clazzElements.containsKey(generic)){
                        for (FieldElement fe : clazzElements.get(generic).getFieldElements()) {
                            if (fe.getColumnName().equals(fieldElement.getMappingColumnName())){
                                targetFieldElement = fe;
                                break;
                            }
                        }
                    }else {
                        throw new IllegalArgumentException("Must use generic type <"+generic+">");
                    }
                    saveBuilder.beginControlFlow("if($L != null && $L.size() > 0)", fieldElement.getName(), fieldElement.getName())
                            .addStatement("cv.put($S, $L.$L.get(0).$L)"
                                    ,fieldElement.getColumnName()
                                    ,objName
                                    ,fieldElement.getModifiers().contains(Modifier.PUBLIC)?fieldElement.getName():buildAccessorName("get", fieldElement.getName())+"()"
                                    ,targetFieldElement.getModifiers().contains(Modifier.PUBLIC)?targetFieldElement.getName():buildAccessorName("get", targetFieldElement.getName())+"()")
                            .endControlFlow();
                    saveBuilder.beginControlFlow("else")
                            .addStatement("cv.put($S, $S)"
                                    , fieldElement.getColumnName()
                                    , "")
                            .endControlFlow();
                }else {
                    throw new IllegalArgumentException("Not support type ("+columnType+") yet!!!");
                }
            }
        }else { // the base type
            saveBuilder.addStatement("cv.put($S, $L.$L)"
                    , fieldElement.getColumnName()
                    , objName
                    , fieldElement.getModifiers().contains(Modifier.PUBLIC)?fieldElement.getName():buildAccessorName("boolean".endsWith(columnType)?"is":"get", fieldElement.getName())+"()");
        }
    }

}
