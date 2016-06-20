package autodao.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/18.
 */
public class TableContractGenerator extends ClazzGenerator {

    public JavaFile generateTableContractClass(HashMap<String, ClazzElement> clazzElements, ClazzElement clazzElement) {
        String clazzName = clazzElement.getName() + TABLE_CONTRACT_SUFFIX;

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // table name field
        FieldSpec tableNameSpec = FieldSpec.builder(String.class, getTableNameFieldContractName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", clazzElement.getTableName())
                .build();
        typeSpecBuilder.addField(tableNameSpec);

        // column field
        for (FieldElement fieldElement : clazzElement.getFieldElements()) {
            FieldSpec fieldSpec = FieldSpec.builder(String.class, getFieldContractName(fieldElement.getColumnName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", fieldElement.getColumnName())
                    .build();
            typeSpecBuilder.addField(fieldSpec);
        }

        // create table sql field
        FieldSpec createTableSqlSpec = FieldSpec.builder(String.class, getCreateTableContractName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", createTableSql(clazzElements, clazzElement))
                .build();
        typeSpecBuilder.addField(createTableSqlSpec);

        List<ClazzElement.Index> indices = clazzElement.getIndices();
        for (ClazzElement.Index index:indices) {
            FieldSpec fieldSpec = FieldSpec.builder(String.class, getIndexFieldName(index.getName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", createIndexSql(clazzElement.getTableName(), index))
                    .build();
            typeSpecBuilder.addField(fieldSpec);
        }

        return JavaFile.builder(clazzElement.getPackageName(), typeSpecBuilder.build()).build();
    }

    private String createTableSql(HashMap<String, ClazzElement> clazzElements, ClazzElement clazzElement) {
        List<String> columnSqls = new ArrayList<>();
        for (FieldElement columnElement:clazzElement.getFieldElements()) {
            StringBuilder columnSB = new StringBuilder();
            String columnName = columnElement.getColumnName();
            String columnType = columnElement.getType();
            String baseColumnType = ColumnTypeUtils.getSQLiteColumnType(columnType);

            if (TextUtils.isEmpty(baseColumnType)){ // not the base type
                if (clazzElements.containsKey(columnElement.getType())){ // one to one
                    columnType = ColumnTypeUtils.INTEGER_TYPE;
                }else {
                    if (columnType.startsWith("java.util.List")
                            || columnType.startsWith("java.util.ArrayList")){
                        if (columnType.equals("java.util.List")
                                || columnType.equals("java.util.ArrayList")){
                            throw new IllegalArgumentException("Must use generic <Type>");
                        }else {
                            int start = columnType.indexOf("<");
                            int end  = columnType.indexOf(">");
                            String generic = columnType.substring(start+1, end);
                            if (clazzElements.containsKey(generic)){
                                for (FieldElement fe : clazzElements.get(generic).getFieldElements()) {
                                    if (fe.getColumnName().equals(columnElement.getMappingColumnName())){
                                        columnType = ColumnTypeUtils.getSQLiteColumnType(fe.getType());
                                        break;
                                    }
                                }
                            }else {
                                throw new IllegalArgumentException("Must use generic type <"+generic+">");
                            }
                        }
                    }else {
                        throw new IllegalArgumentException("Not support type ("+columnType+") yet!!!");
                    }
                }
            }else { // the base type
                columnType = baseColumnType;
            }
            if (!columnName.equals("_id")){
                columnSB.append(columnName)
                        .append(" ")
                        .append(columnType)
                        .append(getNotNULLConstraint(columnElement))
                        .append(getDefaultConstraint(columnElement))
                        .append(getCheckConstraint(columnElement))
                        .append(getUniqueConstraint(columnElement));
            }else {
                columnSB.append(columnName)
                        .append(" ")
                        .append(columnType)
                        .append(" PRIMARY KEY AUTOINCREMENT");
            }
            columnSqls.add(columnSB.toString());
        }

        for (FieldElement columnElement:clazzElement.getFieldElements()) {
            FieldElement.ForeignKey foreignKey = columnElement.getForeignKey();
            if (foreignKey == null) continue;
            StringBuilder columnSB = new StringBuilder();
            columnSB.append("FOREIGN KEY(")
                    .append(columnElement.getColumnName())
                    .append(")")
                    .append(" REFERENCES ")
                    .append(foreignKey.getReferenceTableName())
                    .append("(")
                    .append(foreignKey.getReferenceColumnName())
                    .append(") ")
                    .append(foreignKey.getAction());
            columnSqls.add(columnSB.toString());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
                .append(clazzElement.getTableName());
        sb.append("(");
        sb.append(TextUtils.join(",", columnSqls));
        sb.append(")");
        return sb.toString();
    }

    public String createIndexSql(String tableName, ClazzElement.Index index){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE INDEX ")
                .append(index.getName())
                .append(" ON ")
                .append(tableName)
                .append("(")
                .append(TextUtils.join(",", index.getColumns()))
                .append(")");
        return sb.toString();
    }

    private String getUniqueConstraint(FieldElement columnElement){
        if(columnElement.isUnique()) {
            return " UNIQUE";
        }else {
            return "";
        }
    }

    private String getCheckConstraint(FieldElement columnElement){
        if(!TextUtils.isEmpty(columnElement.getCheck())) {
            return " CHECK("+columnElement.getCheck()+")";
        }else {
            return "";
        }
    }

    private String getDefaultConstraint(FieldElement columnElement){
        if(!TextUtils.isEmpty(columnElement.getDefaultValue())) {
            return " default "+columnElement.getDefaultValue();
        }else {
            return "";
        }
    }

    private String getNotNULLConstraint(FieldElement columnElement){
        if(columnElement.isNotNULL()) {
            return " NOT NULL";
        }else {
            return "";
        }
    }

}
