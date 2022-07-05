package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableVisitor extends PreorderJmmVisitor<Analysis, Boolean>{

    public SymbolTableVisitor(){
        addVisit("ImportedClasses", this::visitImportation);
        addVisit("ClassName", this::visitClassName);
        addVisit("ClassFields", this::visitFields);
        addVisit("MethodMain", this::visitMain);
        addVisit("MethodGeneric", this::visitGeneric);
    }

    public Boolean visitImportation(JmmNode node, Analysis analysis)
    {
        StringBuilder importNames = new StringBuilder();
        for(int i = 0; i < node.getChildren().size(); i++)
        {
            importNames.append(node.getChildren().get(i).get("name"));
            if (i != node.getChildren().size() - 1)
                importNames.append(".");
        }
        analysis.getSymbolTable().addImport(importNames.toString());
        return true;
    }

    public boolean visitClassName(JmmNode node, Analysis analysis)
    {
        analysis.getSymbolTable().setClassName(node.getChildren().get(0).get("name"));
        if(node.getChildren().size() == 2) //Has an extension
        {
            analysis.getSymbolTable().setExtendSuper(node.getChildren().get(1).get("name"));
        }
        return true;
    }

    public Boolean visitFields(JmmNode node, Analysis analysis)
    {

        for(int i = 0; i < node.getChildren().size(); i++)
        {
            JmmNode variable = node.getChildren().get(i);
            Symbol symbol = parseVariable(variable);

            if(analysis.getSymbolTable().containsField(symbol.getName()))
            {
                analysis.addReport(variable, "Duplicate field \"" + symbol.getName() + "\"");
            }
            else
                analysis.getSymbolTable().addField(symbol);
        }

        return true;
    }

    public boolean visitMain(JmmNode node, Analysis analysis)
    {
        Type type = new Type("void", false);

        //Parameter
        String name = node.getChildren().get(0).getJmmChild(0).getJmmChild(0).get("name"); // get the name of the id production in method main???

        //Symbol
        Symbol symbol = new Symbol(new Type("String", true), name);
        List<Symbol> parameters = new ArrayList<>();
        parameters.add(symbol);

        analysis.getSymbolTable().addMethod("main", parameters);
        analysis.getSymbolTable().addMethodType("main", type);

        // parse the body
        JmmNode body = node.getChildren().get(1);
        List<Symbol> localVar = parseMethodBody("main", body, analysis);

        analysis.getSymbolTable().addLocalVariables("main", localVar);

        return true;
    }

    public Boolean visitGeneric(JmmNode node, Analysis analysis)
    {
        String type = node.getChildren().get(0).get("typeName");
        String name = node.getChildren().get(1).get("name");
        Type returnType;

        //Is the type an array?
        if(type.equals("int[]")) returnType = new Type("int", true);
        else returnType = new Type(type, false);

        //Parse parameters
        List<Symbol> params = parseMethodParameters(name, node, analysis);
        analysis.getSymbolTable().addMethod(name, params);
        analysis.getSymbolTable().addMethodType(name, returnType);

        JmmNode body = node.getJmmChild(3);
        List<Symbol> localVars = parseMethodBody(name, body, analysis);
        analysis.getSymbolTable().addLocalVariables(name, localVars);
        return true;
    }

    public Symbol parseVariable(JmmNode variableNode){

        JmmNode typeNode = variableNode.getChildren().get(0);
        String name = variableNode.getChildren().get(1).get("name");

        // Verifies if it's an array.
        Type type;
        if (typeNode.get("typeName").equals("int[]")) type = new Type("int", true);
        else type = new Type(typeNode.get("typeName"), false);

        return new Symbol(type, name);
    }

    public boolean containsLocal(List<Symbol> locals, String varName){
        for (Symbol variable: locals){
            if (variable.getName().equals(varName))
                return true;
        }
        return false;
    }

    public List<Symbol> parseMethodBody(String methodName, JmmNode methodBody, Analysis analysis)
    {
        JmmNode varDecls = methodBody.getChildren().get(0);
        List<Symbol> decls = new ArrayList<>();

        for(int i = 0; i < varDecls.getChildren().size(); i++)
        {
            JmmNode varDec = varDecls.getChildren().get(i);
            Symbol symbol = parseVariable(varDec);

            if(containsLocal(decls, symbol.getName()))
            {
                analysis.addReport(varDec, "Duplicate local variable \"" + symbol.getName() + "\"");
            }
            else
            {
                decls.add(symbol);
            }
        }

        return decls;
    }

    public List<Symbol> parseMethodParameters(String mName, JmmNode node, Analysis analysis)
    {
        //Get the methodParameters production rule
        List<Symbol> parametersSymbols = new ArrayList<>();
        JmmNode parametersNode = node.getChildren().get(2);

        for(int i = 0; i < parametersNode.getNumChildren(); i ++) //get the type and id globbed together
        {
            JmmNode methodParam = parametersNode.getChildren().get(i);
            Symbol s = parseVariable(methodParam);

            if(containsParam(parametersSymbols, s.getName()))
            {
                analysis.addReport(parametersNode, "Duplicate argument name \"" + s.getName() + "\"");
            }
            parametersSymbols.add(s);
        }

        return parametersSymbols;
    }

    public boolean containsParam(List<Symbol> parameters, String varName){
        for (Symbol variable: parameters ){
            if (variable.getName().equals(varName))
                return true;
        }
        return false;
    }
}
