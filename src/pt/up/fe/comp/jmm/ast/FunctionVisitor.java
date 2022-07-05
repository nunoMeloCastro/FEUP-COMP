package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;


public class FunctionVisitor extends PreorderJmmVisitor<Analysis, Boolean>
{
    public FunctionVisitor()
    {
        addVisit("DotExpr", this::visitDot);
        addVisit("NewObject", this::visitNewObj);
    }

    public Boolean validateLength(JmmNode left, Analysis analysis){
        String type = Utils.getNodeType(left, analysis);
        return type.equals("int[]"); //|| type.equals("String[]");
    }

    public Boolean checkObject(JmmNode node, String nodeName, Analysis analysis) {
        String methodName = Utils.getParentMethodName(node);

        JmmNode calledMethod = node.getChildren().get(1).getChildren().get(0);

        List<Symbol> localVariables = analysis.getSymbolTable().getLocalVariables(methodName);
        List<Symbol> classFields = analysis.getSymbolTable().getFields();
        List<Symbol> methodParams = analysis.getSymbolTable().getParameters(methodName);

        return containsObject(localVariables, nodeName, calledMethod, analysis) || containsObject(classFields, nodeName, calledMethod, analysis) ||
                containsObject(methodParams, nodeName, calledMethod, analysis);
    }

    public Boolean isValidType(String typeName) {
        return !typeName.equals("int") && !typeName.equals("String") && !typeName.equals("boolean");
    }

    public Boolean containsObject(List<Symbol> vars, String varName,  JmmNode calledMethod, Analysis analysis){
        for(Symbol symbol: vars){
            // Check if the variable exists and its type is valid
            if(symbol.getName().equals(varName) && isValidType(symbol.getType().getName())) {
                // Check if it is an object of the class
                if (symbol.getType().getName().equals(analysis.getSymbolTable().getClassName())) {
                    if (analysis.getSymbolTable().getSuper() != null) return true; // Extends
                    else if (!analysis.getSymbolTable().getMethods().contains(calledMethod.get("name"))) {
                        analysis.addReport(calledMethod,
                                "\"" + calledMethod.get("name") + "\" is not a class method");
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void hasThisDotMethod(JmmNode node, Analysis analysis){
        String identifier = node.getChildren().get(0).get("name");
        if (!analysis.getSymbolTable().getMethods().contains(identifier)) {
            analysis.addReport(node,"Function \"" + identifier + "\" is undefined");
        }
    }

    public Boolean visitDot(JmmNode node, Analysis analysis)
    {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);

        if(right.getKind().equals("Len"))
        {
            if(!validateLength(left, analysis))
            {
                analysis.addReport(node, "Built-in \"length\" is only valid over arrays.");
                return false;
            }
            return true;
        }

        if (left.getKind().equals("Id"))
        {
            String name = left.get("name");

            if(!Utils.hasImport(name, analysis.getSymbolTable()))
                if(!checkObject(node, name, analysis))
                {
                    analysis.addReport(left, "\"" + name + "\" is not an import nor an object");
                }
        }

        if (left.getKind().equals("That") && right.getKind().equals("DotMethod")){
            if(analysis.getSymbolTable().getSuper() != null) return true;
            hasThisDotMethod(right, analysis);
        }

        return true;
    }

    public Boolean visitNewObj(JmmNode node, Analysis analysis)
    {
        JmmNode objectNode = node.getChildren().get(0);
        String objectName = objectNode.get("name");

        // Check if the object is an instance of the actual class
        if(objectName.equals(analysis.getSymbolTable().getClassName())) return true;

        // Check if the object belongs to an extended class
        if(analysis.getSymbolTable().getSuper() != null &&
                objectName.equals(analysis.getSymbolTable().getSuper())) return true;

        // Check if it is an importation
        if (Utils.hasImport(objectName, analysis.getSymbolTable())) return true;

        analysis.addReport(objectNode,  "\"" + objectName + "\" is not an import nor an object");
        return false;
    }
}
