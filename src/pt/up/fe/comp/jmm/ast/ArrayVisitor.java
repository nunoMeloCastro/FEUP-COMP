package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class ArrayVisitor extends PreorderJmmVisitor<Analysis, Boolean>{
    public ArrayVisitor(){
        addVisit("ArrayAccess", this::visitArrayAccess);
        addVisit("NewIntArray", this::visitNewExpression);
        addVisit("ArrayAssignment", this::visitArrayAssignment);
    }

    public Boolean visitArrayAccess (JmmNode arrayNode, Analysis analysis){
        JmmNode accessNode = arrayNode.getChildren().get(1);

        return visitAccessedArray(arrayNode, analysis) &&
                checkBrackets(arrayNode, analysis, accessNode, "access");
    }

    public Boolean visitArrayAssignment(JmmNode arrayNode, Analysis analysis){
        JmmNode accessNode = arrayNode.getChildren().get(1).getChildren().get(0);

        return visitAccessedArray(arrayNode, analysis) &&
                checkBrackets(arrayNode, analysis ,accessNode,"access");

    }

    public Boolean visitNewExpression(JmmNode arrayNode, Analysis analysis){
        JmmNode sizeNode = arrayNode.getChildren().get(0);

        return checkBrackets(arrayNode, analysis ,sizeNode,"size");
    }

    public Boolean visitAccessedArray(JmmNode newExpresionNode, Analysis analysis){
        JmmNode arrayNode = newExpresionNode.getChildren().get(0);
        String kind = arrayNode.getKind();

        // Check if the Identifier is an array
        if(kind.equals("Id")){
            String parentMethodName = Utils.getParentMethodName(newExpresionNode);
            String type = Utils.getVariableType(arrayNode, analysis, parentMethodName);
            if(!type.equals("int[]") && !type.equals("String[]")){
                analysis.addReport(arrayNode, "Invalid identifier, must be an int[].");
                return false;
            }
            return true;
        }

        // Check if the array is the return type of a method
        if(returnIntArrayMethod(arrayNode,analysis)) return true;

        analysis.addReport(arrayNode, "Invalid array access operation. This operation is only valid for int[].");
        return false;
    }

    private Boolean checkBrackets(JmmNode arrayNode, Analysis analysis, JmmNode node, String context) {
        String kind = node.getKind();
        // Check if it the index is an identifier
        if(kind.equals("Id")){
            String parentMethodName = Utils.getParentMethodName(arrayNode);
            String type = Utils.getVariableType(node, analysis, parentMethodName);

            // If it is an identifier, it must be an integer
            if(!type.equals("int")){
                analysis.addReport(node, "Invalid array " + context + ", identifier must be an integer. Provided: " + type);
                return false;
            }
        }
        // Check if the index is a number, an expression that returns a numeric value,
        // a function that returns an int or another array access
        else if(!kind.equals("Number") && !Utils.isMathExpression(kind) &&
                !kind.equals("ArrayAccess") && !returnIntMethod(node,analysis)) {
            analysis.addReport(node, "Invalid array " + context + ". Must be an integer.");
            return false;
        }
        return true;
    }

    private boolean returnIntMethod(JmmNode node, Analysis analysis){
        if(!node.getKind().equals("Dot")) return false;
        String returnValue = Utils.getReturnValueMethod(node,analysis);
        return returnValue.equals("undefined") || returnValue.equals("int");
    }

    private boolean returnIntArrayMethod(JmmNode node, Analysis analysis){
        if(!node.getKind().equals("Dot")) return false;
        String returnValue = Utils.getReturnValueMethod(node,analysis);
        return returnValue.equals("undefined") || returnValue.equals("int[]");
    }
}
