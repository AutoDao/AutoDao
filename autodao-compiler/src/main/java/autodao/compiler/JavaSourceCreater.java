package autodao.compiler;

import com.squareup.javapoet.JavaFile;

import java.util.HashMap;

/**
 * Created by tubingbing on 16/6/3.
 */
public class JavaSourceCreater {

    TableContractGenerator tableContractGenerator;
    ModelDaoGenerator modelDaoGenerator;
    AutoDaoInjectorGenerator autoDaoInjectorGenerator;

    JavaSourceCreater() {
        tableContractGenerator = new TableContractGenerator();
        modelDaoGenerator = new ModelDaoGenerator();
        autoDaoInjectorGenerator = new AutoDaoInjectorGenerator();
    }

    public JavaFile generateTableContractClass(HashMap<String, ClazzElement> clazzElements,
                                               ClazzElement clazzElement) {
        return tableContractGenerator.generateTableContractClass(clazzElements, clazzElement);
    }

    public JavaFile generateTableDao(HashMap<String, ClazzElement> clazzElements,
                                     ClazzElement clazzElement) {
        return modelDaoGenerator.generateTableDao(clazzElements, clazzElement);
    }

    public JavaFile generateAutoDaoInjector(HashMap<String, ClazzElement> clazzElements) {
        return autoDaoInjectorGenerator.generateAutoDaoInjector(clazzElements);
    }
}
