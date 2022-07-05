package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import java.util.List;

public class UndefinedVisitor extends PreorderJmmVisitor<Analysis, Boolean>
{
    public UndefinedVisitor()
    {
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("ObjectMethodParam", this::visitObjectParam);
        addVisit("ReturnStatement", this::visitReturn);
        addVisit("VarDeclaration", this::visitVarDecl);
        addVisit("Extends", this::visitExtends);
    }

    public Boolean containsSymbol(List<Symbol> vars, String varName){
        for(Symbol symbol: vars){
            if(symbol.getName().equals(varName)){
                return true;
            }
        }
        return false;
    }

    public void varIsDefined(String methodName, Analysis analysis, JmmNode identifierNode) {
        List<Symbol> localVariables = analysis.getSymbolTable().getLocalVariables(methodName);
        List<Symbol> classFields = analysis.getSymbolTable().getFields();
        List<Symbol> methodParams = analysis.getSymbolTable().getParameters(methodName);
        String varName = identifierNode.get("name");

        if(!containsSymbol(localVariables, varName) && !containsSymbol(classFields, varName) &&
                !containsSymbol(methodParams, varName)){
            analysis.addReport(identifierNode, "Variable \"" + identifierNode.get("name") + "\" is undefined");
        }
    }

    public void validateNode(String methodName, JmmNode node, Analysis analysis){
        if (node.getKind().equals("Id")) {
            varIsDefined(methodName, analysis, node);
        } else if (Utils.isOperator(node.getKind()) || node.getKind().equals("NewIntArray")) {
            validateExpression(methodName, node, analysis);
        }
    }

    public void validateExpression(String methodName, JmmNode node, Analysis analysis) {
        JmmNode left, right;

        left = node.getChildren().get(0);
        validateNode(methodName,left,analysis);

        if(node.getNumChildren() == 1) return;

        right = node.getChildren().get(1);
        validateNode(methodName,right,analysis);

    }

    public Boolean visitExtends(JmmNode node, Analysis analysis)
    {
        String name = node.get("name");

        if(Utils.hasImport(name, analysis.getSymbolTable())) return true;

        analysis.addReport(node, "Class with this name \"" + name + "\" is not being imported");
        return false;
    }

    public Boolean isClassInstance(String typeStr){
        return !typeStr.equals("int") && !typeStr.equals("int[]") && !typeStr.equals("String") && !typeStr.equals("boolean");
    }

    public Boolean visitVarDecl(JmmNode node, Analysis analysis){
        JmmNode typeNode = node.getChildren().get(0);

        String typeStr = typeNode.get("typeName");
        if (!isClassInstance(typeStr)) return true;

        if (Utils.hasImport(typeStr, analysis.getSymbolTable()) ||
                analysis.getSymbolTable().getClassName().equals(typeStr)){
            return true;
        }

        analysis.addReport(node, "Type \"" + typeStr + "\" is missing.");
        return false;
    }

    public Boolean visitObjectParam(JmmNode node, Analysis analysis){
        String methodName = Utils.getParentMethodName(node);

        // Check each parameter of a method call
        for (JmmNode nodeChild: node.getChildren()) {
            if (nodeChild.getNumChildren() > 0 && !nodeChild.getKind().equals("DotExpr") && !nodeChild.getKind().equals("NewObject"))
                validateExpression(methodName, nodeChild, analysis);
            else if (node.getKind().equals("Id"))
                varIsDefined(methodName, analysis, nodeChild);
        }

        return true;
    }

    public String getMethodName(JmmNode methodScope){
        if(methodScope.getKind().equals("MethodGeneric")){
            return methodScope.getChildren().get(1).get("name");
        }
        return "main";
    }

    public JmmNode getMethodBody(JmmNode methodScope){
        for (JmmNode child: methodScope.getChildren()){
            if (child.getKind().equals("MethodBody")) return child;
        }
        return null;
    }

    public void validateAssignments(String methodName, JmmNode methodBody, Analysis analysis){
        for (JmmNode node: methodBody.getChildren()){
            if(node.getKind().equals("Assignment")){
                validateExpression(methodName, node, analysis);
            }
        }
    }

    public Boolean visitMethodDeclaration(JmmNode node, Analysis analysis){
        JmmNode methodScope = node.getChildren().get(0); // MethodMain or MethodGeneric
        String methodName = getMethodName(methodScope); // Get method name
        JmmNode methodBody = getMethodBody(methodScope);

        validateAssignments(methodName, methodBody, analysis);

        return true;
    }

    public Boolean visitReturn(JmmNode node, Analysis analysis)
    {
        JmmNode nodeChild = node.getChildren().get(0);
        String methodName = Utils.getParentMethodName(node);

        if (nodeChild.getKind().equals("DotExpr")){
            String ret1 = analysis.getSymbolTable().getReturnType(methodName).getName();
            String methodName2 = nodeChild.getChildren().get(1).getChildren().get(0).get("name");
            if(analysis.getSymbolTable().getReturnType(methodName2) != null) {
                String ret2 = analysis.getSymbolTable().getReturnType(methodName2).getName();
                if (!ret2.equals(ret1)) {
                    analysis.addReport(node, "Return types mismatch");
                }
            }
        }

        if (nodeChild.getNumChildren() > 0 && !nodeChild.getKind().equals("DotExpr") && !nodeChild.getKind().equals("NewObject")) {
            validateExpression(methodName, nodeChild, analysis);
        }
        else if (nodeChild.getKind().equals("Id")){
            varIsDefined(methodName, analysis, nodeChild);
        }

        return true;
    }
}
