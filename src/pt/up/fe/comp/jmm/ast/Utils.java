package pt.up.fe.comp.jmm.ast;


import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.analysis.MySymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class Utils {
    /**
     * Checks the type of the node by searching in fields, imports, parameter, declaration.
     */
    public static String getVariableType(JmmNode node, Analysis analysis, String parentMethodName) {
        if (node.getKind().equals("Number")) return "int";
        else if (node.getKind().equals("Correct") || node.getKind().equals("Incorrect")) return "boolean";
        else if (node.getKind().equals("That")) return analysis.getSymbolTable().getClassName();

        List<Symbol> localVariables = analysis.getSymbolTable().getLocalVariables(parentMethodName);
        List<Symbol> fields = analysis.getSymbolTable().getFields();
        List<Symbol> parameters = analysis.getSymbolTable().getParameters(parentMethodName);

        // Verifies if the element is in the symbol and table. And if it is, return the type.
        for (Symbol symb : localVariables) {
            String varName = symb.getName();
            String auxName;

            if (node.getChildren().size() != 0) {
                auxName = node.getJmmChild(1).getJmmChild(0).get("name");
            }
            else
                auxName = node.get("name");

            if (varName.equals(auxName))
                return symb.getType().getName() + (symb.getType().isArray() ? "[]" : "");
        }

        for (Symbol symb : fields) {
            String varName = symb.getName();
            String auxName;

            if (node.getChildren().size() != 0) {
                auxName = node.getJmmChild(1).getJmmChild(0).get("name");
            }
            else
                auxName = node.get("name");

            if (varName.equals(auxName))
                return symb.getType().getName() + (symb.getType().isArray() ? "[]" : "");
        }

        for (Symbol symb : parameters) {
            String varName = symb.getName();
            String auxName;

            if (node.getChildren().size() != 0) {
                auxName = node.getJmmChild(1).getJmmChild(0).get("name");
            }
            else
                auxName = node.get("name");

            if (varName.equals(auxName))
                return symb.getType().getName() + (symb.getType().isArray() ? "[]" : "");
        }

        return "undefined";
    }

    public static String getNodeType(JmmNode node, Analysis analysis){
        String kind = node.getKind();

        if(isMathExpression(kind)) return "int";
        if(isBooleanExpression(kind)) return "boolean";

        switch (kind){
            case "DotExpr":
                return getReturnValueMethod(node,analysis);
            case "ArrayAccess":
                return "int";
            case "NewObject":
                return node.getChildren().get(0).get("name");
            case "NewIntArray":
                return "int[]";
            default:
                // Identifier
                String parentMethodName = getParentMethodName(node);
                return getVariableType(node,analysis,parentMethodName);
        }

    }


    public static String getParentMethodName(JmmNode node) {
        JmmNode currentNode = node;
        while (!currentNode.getKind().equals("MethodGeneric") && !currentNode.getKind().equals("MethodMain")) {
            currentNode = currentNode.getJmmParent();
        }
        if (currentNode.getKind().equals("MethodGeneric"))
            return currentNode.getChildren().get(1).get("name");
        return "main";
    }

    public static boolean isMathExpression(String kind) {
        return kind.equals("MultOp") || kind.equals("AddOp") || kind.equals("SubOp") || kind.equals("DivOp");
    }

    public static boolean isBooleanExpression(String kind) {
        return kind.equals("LessOp") || kind.equals("AndOp") || kind.equals("NotOp");
    }

    public static Boolean isOperator(String kind) {
        return kind.equals("AddOp") ||
                kind.equals("MultOp") ||
                kind.equals("SubOp") ||
                kind.equals("DivOp") ||
                kind.equals("LessOp") ||
                kind.equals("AndOp") ||
                kind.equals("ArrayAccess")||
                kind.equals("ArrayExpression")||
                kind.equals("ArrayAssignment");
    }

    public static String getReturnValueMethod(JmmNode dotNode, Analysis analysis) {
        JmmNode leftNode = dotNode.getChildren().get(0);
        JmmNode rigthNode = dotNode.getChildren().get(1);

        String typeName = Utils.getNodeType(leftNode, analysis);
        String className = analysis.getSymbolTable().getClassName();

        if(rigthNode.getKind().equals("Len")) return "int";

        //System.out.println(dotNode.getChildren().get(0));
        String methodName;
        if (dotNode.getJmmChild(0).getKind().equals("That"))
            methodName = dotNode.getJmmChild(1).getJmmChild(0).get("name");
        else if (dotNode.getJmmChild(0).getKind().equals("NewObject"))
            methodName = dotNode.getJmmChild(0).getJmmChild(0).get("name");
        else
            methodName = dotNode.getJmmChild(0).get("name");

        boolean containsMethodName = analysis.getSymbolTable().getMethods().contains(methodName);

        if (containsMethodName && (typeName.equals(className) || dotNode.getKind().equals("That"))) {
            Type returnType = analysis.getSymbolTable().getReturnType(methodName);
            return returnType.getName() + (returnType.isArray() ? "[]" : "");
        }

        return "undefined";
    }


    public static boolean hasImport(String checkImport, SymbolTable symbolTable){
        for(String importName : symbolTable.getImports()) {
            String[] splitImport = importName.split("\\.");
            if (splitImport[splitImport.length - 1].equals(checkImport)) return true;
        }
        return false;
    }
}