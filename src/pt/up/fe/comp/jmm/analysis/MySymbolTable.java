package pt.up.fe.comp.jmm.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySymbolTable implements SymbolTable {

    String className;
    String extendSuper;
    List<String> imports;
    List<Symbol> fields;
    HashMap<String, List<Symbol>> parameters;
    HashMap<String, List<Symbol>> localVariables;
    HashMap<String, Type> types;

    public MySymbolTable()
    {
        imports =  new ArrayList<>();
        fields = new ArrayList<>();
        parameters = new HashMap<>();
        localVariables = new HashMap<>();
        types = new HashMap<>();
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return extendSuper;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    @Override
    public List<String> getMethods() {
        return new ArrayList<>(parameters.keySet());
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return types.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return parameters.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return localVariables.get(methodSignature);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setExtendSuper(String extendSuper) {
        this.extendSuper = extendSuper;
    }

    public void addImport(String importName) {
        imports.add(importName);
    }

    public void addField(Symbol field) {
        fields.add(field);
    }

    public void addMethod(String methodName, List<Symbol> params) {
        parameters.put(methodName, params);
    }

    public void addLocalVariables(String methodName, List<Symbol> variables) {
        localVariables.put(methodName, variables);
    }

    public void addMethodType(String methodName, Type returnType) {
        types.put(methodName, returnType);
    }

    public boolean containsField(String varName){
        for (Symbol field : fields) {
            if (field.getName().equals(varName))
                return true;
        }
        return false;
    }
}
