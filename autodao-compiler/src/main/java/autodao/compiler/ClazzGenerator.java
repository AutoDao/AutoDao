package autodao.compiler;

/**
 * Created by tubingbing on 16/6/18.
 */
public class ClazzGenerator {

    public static final String TABLE_CONTRACT_SUFFIX = "Contract";
    public static final String TABLE_DAO_SUFFIX = "Dao";
    public static final String INJECTOR_PKG = "autodao";
    public static final String INJECTOR_NAME = "AutoDaoInjector";
    public static final String INJECTOR_DAO_MAP_FIELD_NAME = "daoMap";
    public static final String INJECTOR_TABLE_MAP_FIELD_NAME = "tableMap";
    public static final String INJECTOR_SERIALIZER_MAP_FIELD_NAME = "serializerMap";

    public String getCreateTableContractName(){
        return "CREATE_TABLE_SQL";
    }

    public String getIndexFieldName(String indexName) {
        return "CREATE_"+indexName.toUpperCase()+"_SQL";
    }

    public String getTableNameFieldContractName(){
        return "TABLE_NAME";
    }

    public String getFieldContractName(String columnName){
        return columnName.toUpperCase()+"_COLUMN";
    }

    /**
     * prefix + suffix (setter,getter,is)
     * @param prefix
     * @param suffix
     * @return
     */
    public static String buildAccessorName(String prefix, String suffix) {
        if (suffix.length() == 0) return prefix;
        if (prefix.length() == 0) return suffix;

        char first = suffix.charAt(0);
        if (Character.isLowerCase(first)) {
            boolean useUpperCase = suffix.length() > 2
                    && (Character.isLowerCase(suffix.charAt(1)) || !Character.isLetter(suffix.charAt(1)));
            suffix = String.format("%s%s",
                    useUpperCase ? Character.toUpperCase(first) : first,
                    suffix.subSequence(1, suffix.length()));
        }
        return String.format("%s%s", prefix, suffix);
    }

}
