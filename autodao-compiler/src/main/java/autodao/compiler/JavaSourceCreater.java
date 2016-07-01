package autodao.compiler;

import com.google.common.hash.BloomFilter;
import com.squareup.javapoet.JavaFile;

import java.util.HashMap;

/**
 * Created by tubingbing on 16/6/3.
 */
public class JavaSourceCreater {

    TableContractGenerator tableContractGenerator;
    ModelDaoGenerator modelDaoGenerator;
    AutoDaoInjectorGenerator autoDaoInjectorGenerator;
    OpenHelperGenerator openHelperGenerator;

    JavaSourceCreater(HashMap<String, ClazzElement> clazzElements) {
        tableContractGenerator = new TableContractGenerator(clazzElements);
        modelDaoGenerator = new ModelDaoGenerator(clazzElements);
        autoDaoInjectorGenerator = new AutoDaoInjectorGenerator(clazzElements);
        openHelperGenerator = new OpenHelperGenerator(clazzElements);
    }

    public JavaFile generateTableContractClass(ClazzElement clazzElement) {
        return tableContractGenerator.generateTableContractClass(clazzElement);
    }

    public JavaFile generateTableDao(ClazzElement clazzElement) {
        return modelDaoGenerator.generateTableDao(clazzElement);
    }

    public JavaFile generateAutoDaoInjector() {
        return autoDaoInjectorGenerator.generateAutoDaoInjector();
    }

    public JavaFile generateOpenHelper() {
        return openHelperGenerator.generateOpenHelper();
    }
}
