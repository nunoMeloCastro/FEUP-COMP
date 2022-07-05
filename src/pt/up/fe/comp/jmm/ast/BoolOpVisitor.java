package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class BoolOpVisitor extends PreorderJmmVisitor<Analysis, Boolean>{

    public BoolOpVisitor()
    {
        addVisit("LessOp", this::visitLessOperator);
        addVisit("AndOp", this::visitAndOp);
        addVisit("NotOp", this::visitNotOp);
    }

    public Boolean visitLessOperator(JmmNode node, Analysis analysis)
    {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);

        if(left.getKind().equals("DotExpr"))
        {
            String retType = Utils.getReturnValueMethod(left, analysis);
            if(!retType.equals("undefined") && !retType.equals("int") && !retType.equals("int[]"))
            {
                analysis.addReport(left, "\"" + left + "\" invalid type. Expecting int or int[]");
            }
        }
        else if(left.getChildren().size() > 0 && !left.getKind().equals("ArrayAccess") && !Utils.isMathExpression(left.getKind()))
            analysis.addReport(left, "\"" + left + "\" unexpected operator.");
        else if(left.getNumChildren() == 0 && right.getNumChildren() == 0)
        {
            //We have identifiers or numbers involved
            String parenMeth = Utils.getParentMethodName(node);
            String leftKind = Utils.getVariableType(left, analysis, parenMeth);
            String rightKind = Utils.getVariableType(right, analysis, parenMeth);

            if(!leftKind.equals("int") && leftKind.equals("int[]"))
            {
                analysis.addReport(left, "\"" + left + "\" invalid type.");
            }
            else if(!rightKind.equals("int") && rightKind.equals("int[]"))
                analysis.addReport(right, "\"" + right + "\" invalid type");
        }


        return true;
    }

    public Boolean visitAndOp(JmmNode node, Analysis analysis)
    {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);
        String parenMeth = Utils.getParentMethodName(node);

        if(left.getKind().equals("DotExpr"))
        {
            String retValMeth = Utils.getReturnValueMethod(left, analysis);
            if(!retValMeth.equals("undefined") && !retValMeth.equals("boolean"))
                analysis.addReport(left, "\"" + left + "\" invalid type. Expecting a boolean.");
        }
        else if(left.getNumChildren() > 0)
        {
            if(!Utils.isBooleanExpression(left.getKind()))
                analysis.addReport(left, "\"" + left + "\" invalid expression. Expecting boolean.");
        }
        else if(!Utils.getVariableType(left, analysis, parenMeth).equals("boolean"))
            analysis.addReport(left, "\"" + left + "\" invalid expression. Expecting boolean.");

        else if (right.getKind().equals("Dot")){
            String returnValueMethod = Utils.getReturnValueMethod(right, analysis);
            if (!returnValueMethod.equals("undefined") && !returnValueMethod.equals("boolean"))
                analysis.addReport(right , "\"" + right + "\" invalid type: expecting an boolean.");
        }
        else if (right.getNumChildren() > 0) {
            if(!Utils.isBooleanExpression(right.getKind())){
                analysis.addReport(right, "\"" + right + "\" invalid expression");
            }
        }
        else if(!Utils.getVariableType(right, analysis, parenMeth).equals("boolean")){
            analysis.addReport(right, "\"" + right + "\" invalid expression");
        }
        return true;
    }

    public Boolean visitNotOp(JmmNode node, Analysis analysis)
    {
        JmmNode child = node.getChildren().get(0);
        String parenMeth = Utils.getParentMethodName(node);

        if(child.getKind().equals("DotExpr"))
        {
            String retValMeth = Utils.getReturnValueMethod(child, analysis);
            if(!retValMeth.equals("undefined") && !retValMeth.equals("boolean"))
                analysis.addReport(child, "\"" + child + "\" invalid type: expecting an boolean.");
        }
        else if(!Utils.isBooleanExpression(child.getKind()) && !Utils.getVariableType(child, analysis, parenMeth).equals("boolean"))
            analysis.addReport(child, "\"" + child + "\" invalid expression");

        return true;
    }

}
