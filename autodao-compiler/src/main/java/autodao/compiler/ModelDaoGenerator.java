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

    HashMap<String, ClazzElement> clazzElements;

    public ModelDaoGenerator(HashMap<String, ClazzElement> clazzElements) {
        this.clazzElements = clazzElements;
    }

    public JavaFile generateTableDao(ClazzElement clazzElement) {
        String modelName = clazzElement.getName();
        String clazzName = modelName + TABLE_DAO_SUFFIX;
        String objName = modelName.replaceFirst(modelName, modelName.toLowerCase());

        ClassName modelDao = ClassName.get("autodao", "ModelDao");
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL)
                .addSuperinterface(modelDao);

        ClassName model = ClassName.get(clazzElement.getPackageName(), modelName);
        ClassName sqlite = ClassName.get("android.database.sqlite", "SQLiteDatabase");
        ClassName contentValues = ClassName.get("android.content", "ContentValues");
        ClassName cursor = ClassName.get("android.database", "Cursor");

        TypeVariableName modelTypeVariableName = TypeVariableName.get("M extends autodao.Model");
        TypeVariableName listTypeGenerics = TypeVariableName.get("java.util.List<M>");
        TypeVariableName modelTypeGenerics = TypeVariableName.get("M");

        MethodSpec.Builder sqlSelectBuilder = generateSqlSelectMethod(clazzElement,
                objName,
                modelDao,
                model,
                cursor,
                modelTypeVariableName,
                listTypeGenerics);
        MethodSpec.Builder sqlSelectSingleBuilder = generateSqlSelectSingleMethod(clazzElement,
                objName,
                modelDao,
                model,
                cursor,
                modelTypeVariableName,
                modelTypeGenerics);

        MethodSpec.Builder joinSelectBuilder = generateJoinSelectMethod(cursor);

        ClassName statementClass = ClassName.get("android.database.sqlite", "SQLiteStatement");
        ClassName operatorClass = ClassName.get("autodao", "Operator");
        MethodSpec.Builder sqlDeleteBuilder = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(operatorClass, "operator");
        sqlDeleteBuilder.addStatement("db.acquireReference()");
        sqlDeleteBuilder
                .beginControlFlow("try")
                .addStatement("$T compileSql = operator.toSql()", String.class)
                .addStatement("$T statement = injector.getStatement(compileSql)", statementClass)
                .beginControlFlow("if (statement == null) ")
                .addStatement("statement = db.compileStatement(compileSql)", statementClass)
                .addStatement("injector.putStatement(compileSql, statement)", statementClass)
                .endControlFlow()
                .addStatement("statement.clearBindings()")
                .addStatement("operator.bindStatement(statement)")
                .beginControlFlow("try")
                .addStatement("return statement.executeUpdateDelete()")
                .endControlFlow()
                .beginControlFlow("finally")
//                .addStatement("statement.close()")
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("db.releaseReference()")
                .endControlFlow();

        ClassName injectorClass = ClassName.get("autodao", "Injector");
        typeSpecBuilder.addField(sqlite, "db", Modifier.PRIVATE);
        typeSpecBuilder.addField(injectorClass, "injector", Modifier.PRIVATE);
        MethodSpec flux = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqlite, "db")
                .addParameter(injectorClass, "injector")
                .addStatement("this.$N = $N", "db", "db")
                .addStatement("this.$N = $N", "injector", "injector")
                .build();
        typeSpecBuilder.addMethod(flux);
        typeSpecBuilder.addMethod(generateSqlSaveMethod(clazzElement, objName, model, contentValues).build());
        typeSpecBuilder.addMethod(sqlSelectBuilder.build());
        typeSpecBuilder.addMethod(sqlSelectSingleBuilder.build());
        typeSpecBuilder.addMethod(sqlDeleteBuilder.build());
        typeSpecBuilder.addMethod(joinSelectBuilder.build());
        typeSpecBuilder.addMethod(generateSqlUpdateMethod(clazzElement, objName, model, contentValues).build());

        return JavaFile.builder(clazzElement.getPackageName(), typeSpecBuilder.build()).build();
    }

    private MethodSpec.Builder generateJoinSelectMethod(ClassName cursor) {

        ClassName operatorClass = ClassName.get("autodao", "Operator");
        MethodSpec.Builder joinSelectBuilder = MethodSpec
                .methodBuilder("joinSelect")
                .addModifiers(Modifier.PUBLIC)
                .returns(cursor)
                .addParameter(operatorClass, "operator");
        joinSelectBuilder.addStatement("return db.rawQuery(operator.toSql(), operator.getArgments())");
        return joinSelectBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateSqlSelectSingleMethod(
            ClazzElement clazzElement,
            String objName,
            ClassName modelDao,
            ClassName model,
            ClassName cursor,
            TypeVariableName modelTypeVariableName,
            TypeVariableName modelTypeGenerics) {

        return generateSqlSelectOrSelectSingleMethod(clazzElement,
                objName,
                modelDao,
                model,
                cursor,
                modelTypeVariableName,
                modelTypeGenerics,
                false);
    }

    @NonNull
    private MethodSpec.Builder generateSqlSelectMethod(ClazzElement clazzElement,
                                                       String objName,
                                                       ClassName modelDao,
                                                       ClassName model,
                                                       ClassName cursor,
                                                       TypeVariableName modelTypeVariableName,
                                                       TypeVariableName listTypeGenerics) {

        return generateSqlSelectOrSelectSingleMethod(clazzElement,
                objName,
                modelDao,
                model,
                cursor,
                modelTypeVariableName,
                listTypeGenerics,
                true);
    }

    @NonNull
    private MethodSpec.Builder generateSqlSelectOrSelectSingleMethod(ClazzElement clazzElement,
                                                                     String objName,
                                                                     ClassName modelDao,
                                                                     ClassName model,
                                                                     ClassName cursor,
                                                                     TypeVariableName modelTypeVariableName,
                                                                     TypeVariableName returnType,
                                                                     boolean isCreateSelectMethod) {

        ClassName operatorClass = ClassName.get("autodao", "Operator");
        MethodSpec.Builder selectBuilder = MethodSpec
                .methodBuilder(isCreateSelectMethod ? "select" : "selectSingle")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(modelTypeVariableName)
                .returns(returnType)
                .addParameter(operatorClass, "operator");
        String objNameList = objName + "List";
        if (isCreateSelectMethod)
            selectBuilder.addStatement("$T<$T> $L = null", List.class, model, objNameList);
        selectBuilder.addStatement("$T cursor = null", cursor);
        selectBuilder.beginControlFlow("try");
        //---------- try block start ---------------------
        // db
        selectBuilder.addStatement("cursor = db.rawQuery(operator.toSql(), operator.getArgments())");

        if (isCreateSelectMethod)
            selectBuilder.addStatement("$L = new $T<>(cursor.getCount())",
                    objNameList,
                    ArrayList.class);
        selectBuilder.addStatement("$T<String> targetColumns = null", List.class);

        selectBuilder.beginControlFlow("if (operator.getTargetColumns() != null)");
        selectBuilder.addStatement("targetColumns = $T.asList(operator.getTargetColumns())", Arrays.class);
        selectBuilder.endControlFlow();

        selectBuilder.beginControlFlow("while(cursor.moveToNext())");
        selectBuilder.addStatement("$T $L = new $T()", model, objName, model);
        for (FieldElement fieldElement : clazzElement.getFieldElements()) {
            String columnName = fieldElement.getColumnName();
            String fieldName = fieldElement.getName();
            String columnType = fieldElement.getType();

            if ("_id".equals(columnName)) {
                selectBuilder.addStatement("$L.$L = cursor.$L(cursor.getColumnIndex($S))",
                        objName,
                        fieldName,
                        ColumnTypeUtils.getSQLiteTypeMethod(columnType),
                        columnName);
                continue;
            }

            // String name = cursor.getString(cursor.getColumnIndex("userName"));
            selectBuilder.beginControlFlow("if(targetColumns == null || targetColumns.contains($S))", columnName);

            if (ColumnTypeUtils.isBoolean(columnType)) {
                selectBuilder.addStatement("$L $L = cursor.$L(cursor.getColumnIndex($S)) == 1"
                        , columnType, fieldName
                        , ColumnTypeUtils.getSQLiteTypeMethod(columnType)
                        , columnName);
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            } else if (ColumnTypeUtils.isChar(columnType) || ColumnTypeUtils.isByte(columnType)) {
                selectBuilder.addStatement("$L $L = ($L)(cursor.$L(cursor.getColumnIndex($S)))"
                        , columnType, fieldName, columnType
                        , ColumnTypeUtils.getSQLiteTypeMethod(columnType)
                        , columnName);
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            } else if (fieldElement.getSerializer() != null) { // serializer
                String serializerCanonicalName = fieldElement.getSerializer().getSerializerCanonicalName();
                String serializedTypeCanonicalName = fieldElement.getSerializer().getSerializedTypeCanonicalName();
                selectBuilder.addStatement("$L $L = ($L)(injector.getSerializer($S).deserialize(cursor.$L(cursor.getColumnIndex($S))))",
                        columnType,
                        fieldName,
                        columnType,
                        serializerCanonicalName,
                        ColumnTypeUtils.getSQLiteTypeMethod(serializedTypeCanonicalName),
                        columnName);
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            } else if (columnType.startsWith("java.util.List")
                    || columnType.startsWith("java.util.ArrayList")) {
                int start = columnType.indexOf("<");
                int end = columnType.indexOf(">");
                String generic = columnType.substring(start + 1, end);
                // one to many
                if (clazzElements.containsKey(generic)) {
                    for (FieldElement fe : clazzElements.get(generic).getFieldElements()) {
                        if (fe.getColumnName().equals(fieldElement.getMappingColumnName())) {
                            ClazzElement mappingClazzElement = clazzElements.get(generic);
                            String mappingCN = mappingClazzElement.getPackageName() + "." + mappingClazzElement.getName();
                            selectBuilder.addStatement("$T modelDao = injector.getModelDao($S)",
                                    modelDao,
                                    mappingCN);
                            selectBuilder.addStatement("$L $L = cursor.$L(cursor.getColumnIndex($S))"
                                    , fe.getType()
                                    , fe.getName()
                                    , ColumnTypeUtils.getSQLiteTypeMethod(fe.getType())
                                    , columnName);
                            selectBuilder.addStatement("$T $L = new $T{$L}",
                                    String[].class,
                                    "mappingSelectionArgs",
                                    String[].class,
                                    fe.getName());

                            selectBuilder.addStatement("$L $L = new autodao.Select(injector).from($L.class).where($S, $L).select()",
                                    columnType,
                                    fieldName,
                                    mappingCN,
                                    fieldElement.getMappingColumnName() + "=?",
                                    fe.getName());
                            generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
                            break;
                        }
                    }
                } else {
                    throw new IllegalArgumentException("The generic type must be Model");
                }
            } else if (ColumnTypeUtils.getSQLiteColumnType(columnType) == null) { // one to one

                selectBuilder.addStatement("$T modelDao = injector.getModelDao($S)"
                        , modelDao
                        , columnType);
                selectBuilder.addStatement("$T mappingTableName = injector.getTableName($S)"
                        , String.class
                        , columnType);
                selectBuilder.addStatement("$L _id = cursor.$L(cursor.getColumnIndex($S))"
                        , long.class
                        , ColumnTypeUtils.getSQLiteTypeMethod("long")
                        , columnName);

                selectBuilder.addStatement("$L $L = new autodao.Select(injector).from($L.class).where($S, $L).selectSingle()",
                        columnType,
                        fieldName,
                        columnType,
                        "_id=?",
                        "_id");
                generateSetFieldValueStatement(objName, selectBuilder, fieldElement, fieldName);
            } else {
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
        selectBuilder
                .beginControlFlow("if(cursor != null)")
                .addStatement("cursor.close()")
                .endControlFlow();
        // --------- finally block end -------------
        selectBuilder.endControlFlow();
        if (isCreateSelectMethod)
            selectBuilder.addStatement("return ($T<M>)$L", List.class, objNameList);
        else
            selectBuilder.addStatement("return null");

        return selectBuilder;
    }

    private void generateSetFieldValueStatement(String objName,
                                                MethodSpec.Builder selectBuilder,
                                                FieldElement fieldElement,
                                                String fieldName) {
        if (fieldElement.getModifiers().contains(Modifier.PUBLIC)) {
            selectBuilder.addStatement("$L.$L = $L", objName, fieldName, fieldName);
        } else {
            String fieldSetName = buildAccessorName("set", fieldName);
            selectBuilder.addStatement("$L.$L($L)", objName, fieldSetName, fieldName);
        }
    }

    @NonNull
    private MethodSpec.Builder generateSqlUpdateMethod(ClazzElement clazzElement,
                                                       String objName,
                                                       ClassName model,
                                                       ClassName contentValues) {
        ClassName statementClass = ClassName.get("android.database.sqlite", "SQLiteStatement");
        ClassName operatorClass = ClassName.get("autodao", "Operator");

        // update method
        MethodSpec.Builder updateBuilder = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(operatorClass, "operator");

        updateBuilder.addStatement("$T $L = ($T)(operator.getModel())", model, objName, model);
        updateBuilder.addStatement("$T cv = new $T()", contentValues, contentValues);
        updateBuilder.addStatement("$T[] targetColumns = operator.getTargetColumns()", String.class);
        generateContentValues(clazzElement, objName, updateBuilder, true);

        updateBuilder.addStatement("db.acquireReference()");
        updateBuilder
                .beginControlFlow("try")
                .addStatement("$T compileSql = operator.toSql(cv)", String.class)
                .addStatement("$T statement = injector.getStatement(compileSql)", statementClass)
                .beginControlFlow("if (statement == null) ")
                .addStatement("statement = db.compileStatement(compileSql)", statementClass)
                .addStatement("injector.putStatement(compileSql, statement)", statementClass)
                .endControlFlow()
                .addStatement("statement.clearBindings()")
                .addStatement("operator.bindStatement(statement)")
                .beginControlFlow("try")
                .addStatement("return statement.executeUpdateDelete()")
                .endControlFlow()
                .beginControlFlow("finally")
//                .addStatement("statement.close()")
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("db.releaseReference()")
                .endControlFlow();

        return updateBuilder;
    }

    @NonNull
    private MethodSpec.Builder generateSqlSaveMethod(ClazzElement clazzElement,
                                                     String objName,
                                                     ClassName model,
                                                     ClassName contentValues) {
        ClassName statementClass = ClassName.get("android.database.sqlite", "SQLiteStatement");
        ClassName operatorClass = ClassName.get("autodao", "Operator");

        // update method
        MethodSpec.Builder updateBuilder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .returns(long.class)
                .addParameter(operatorClass, "operator");

        updateBuilder.addStatement("$T $L = ($T)(operator.getModel())", model, objName, model);
        updateBuilder.addStatement("$T cv = new $T()", contentValues, contentValues);
        updateBuilder.addStatement("$T[] targetColumns = operator.getTargetColumns()", String.class);
        generateContentValues(clazzElement, objName, updateBuilder, true);

        updateBuilder.addStatement("db.acquireReference()");
        updateBuilder
                .beginControlFlow("try")
                .addStatement("$T compileSql = operator.toSql(cv)", String.class)
                .addStatement("$T statement = injector.getStatement(compileSql)", statementClass)
                .beginControlFlow("if (statement == null) ")
                .addStatement("statement = db.compileStatement(compileSql)", statementClass)
                .addStatement("injector.putStatement(compileSql, statement)", statementClass)
                .endControlFlow()
                .addStatement("statement.clearBindings()")
                .addStatement("operator.bindStatement(statement)")
                .beginControlFlow("try")
                .addStatement("return statement.executeInsert()")
                .endControlFlow()
                .beginControlFlow("finally")
//                .addStatement("statement.close()")
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("db.releaseReference()")
                .endControlFlow();

        return updateBuilder;
    }

    private void generateContentValues(ClazzElement clazzElement,
                                       String objName,
                                       MethodSpec.Builder saveBuilder,
                                       boolean filterColumns) {

        if (filterColumns) {
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

        for (FieldElement fieldElement : clazzElement.getFieldElements()) {

            if ("_id".equals(fieldElement.getName())) continue;

            if (filterColumns) {
                saveBuilder.beginControlFlow("if (shouldFilter)");
                saveBuilder.beginControlFlow("if (targetColumnList.contains($S))", fieldElement.getColumnName());
                putContentValues(objName, saveBuilder, fieldElement);
                saveBuilder.endControlFlow();
                saveBuilder.endControlFlow();
                saveBuilder.beginControlFlow("else");
                putContentValues(objName, saveBuilder, fieldElement);
                saveBuilder.endControlFlow();
            } else {
                putContentValues(objName, saveBuilder, fieldElement);
            }
        }
    }

    private void putContentValues(String objName,
                                  MethodSpec.Builder saveBuilder,
                                  FieldElement fieldElement) {
        String columnType = fieldElement.getType();
        if (TextUtils.isEmpty(ColumnTypeUtils.getSQLiteColumnType(columnType))) { // not the base type
            if (clazzElements.containsKey(fieldElement.getType())) { // one to one
                ClazzElement fieldClazz = clazzElements.get(fieldElement.getType());
                ClassName fieldClazzName = ClassName.get(fieldClazz.getPackageName(), fieldClazz.getName());
                saveBuilder.addStatement("$T $L = $L.$L"
                        , fieldClazzName
                        , fieldElement.getName()
                        , objName
                        , fieldElement
                                .getModifiers()
                                .contains(Modifier.PUBLIC)
                                ? fieldElement.getName()
                                : buildAccessorName("get", fieldElement.getName()) + "()");
                saveBuilder.beginControlFlow("if ($L == null)", fieldElement.getName())
                        .addStatement("cv.put($S, -1)", fieldElement.getColumnName())
                        .endControlFlow();
                saveBuilder.beginControlFlow("else")
                        .addStatement("cv.put($S, $L._id)",
                                fieldElement.getColumnName(),
                                fieldElement.getName())
                        .endControlFlow();
            } else if (fieldElement.getSerializer() != null) {
                String serializerCanonicalName = fieldElement
                        .getSerializer()
                        .getSerializerCanonicalName();
                String serializedTypeCanonicalName = fieldElement
                        .getSerializer()
                        .getSerializedTypeCanonicalName();
                saveBuilder.addStatement("$L $L = ($L)(injector.getSerializer($S))"
                        , serializerCanonicalName
                        , "serializer"
                        , serializerCanonicalName
                        , serializerCanonicalName);
                saveBuilder.beginControlFlow("if(serializer == null)")
                        .addStatement("throw new $T($S)", IllegalArgumentException.class, "Can't find " + serializerCanonicalName + " serializer")
                        .endControlFlow();
                saveBuilder.addStatement("$T serializedValue = serializer.serialize($L.$L)"
                        , Object.class
                        , objName
                        , fieldElement.getModifiers().contains(Modifier.PUBLIC) ? fieldElement.getName() : buildAccessorName("boolean".endsWith(columnType) ? "is" : "get", fieldElement.getName()) + "()");
                saveBuilder.beginControlFlow("if(serializedValue != null)")
                        .addStatement("cv.put($S, ($L)serializedValue)", fieldElement.getColumnName(), serializedTypeCanonicalName)
                        .endControlFlow();
            } else if (columnType.startsWith("java.util.List")
                    || columnType.startsWith("java.util.ArrayList")) {
                saveBuilder.addStatement("$L $L = $L.$L"
                        , fieldElement.getType()
                        , fieldElement.getName()
                        , objName
                        , fieldElement
                                .getModifiers()
                                .contains(Modifier.PUBLIC) ? fieldElement.getName() : buildAccessorName("get", fieldElement.getName()) + "()");
                FieldElement targetFieldElement = null;
                int start = columnType.indexOf("<");
                int end = columnType.indexOf(">");
                String generic = columnType.substring(start + 1, end);
                if (clazzElements.containsKey(generic)) {
                    for (FieldElement fe : clazzElements.get(generic).getFieldElements()) {
                        if (fe.getColumnName().equals(fieldElement.getMappingColumnName())) {
                            targetFieldElement = fe;
                            break;
                        }
                    }
                } else {
                    throw new IllegalArgumentException(generic + " Model should be apply @Table annotation");
                }
                if (targetFieldElement == null)
                    throw new IllegalArgumentException("Can't find "
                            + fieldElement.getMappingColumnName()
                            + " column on " + generic);
                saveBuilder.beginControlFlow("if($L != null && $L.size() > 0)", fieldElement.getName(), fieldElement.getName())
                        .addStatement("cv.put($S, $L.$L.get(0).$L)",
                                fieldElement.getColumnName(),
                                objName,
                                fieldElement.getModifiers().contains(Modifier.PUBLIC)
                                        ? fieldElement.getName()
                                        : buildAccessorName("get", fieldElement.getName()) + "()",
                                targetFieldElement.getModifiers().contains(Modifier.PUBLIC)
                                        ? targetFieldElement.getName()
                                        : buildAccessorName("get", targetFieldElement.getName()) + "()")
                        .endControlFlow();
                saveBuilder.beginControlFlow("else")
                        .addStatement("cv.put($S, $S)", fieldElement.getColumnName(), "")
                        .endControlFlow();
            } else {
                throw new IllegalArgumentException("Not support type (" + columnType + ") yet!!!");
            }
        } else { // the base type
            saveBuilder.addStatement("cv.put($S, $L.$L)"
                    , fieldElement.getColumnName()
                    , objName
                    , fieldElement
                            .getModifiers().contains(Modifier.PUBLIC) ? fieldElement.getName()
                            : buildAccessorName("boolean".endsWith(columnType) ? "is" : "get",
                            fieldElement.getName()) + "()");
        }
    }

}
