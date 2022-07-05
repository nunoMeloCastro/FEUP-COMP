package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class MathOpVisitor extends PreorderJmmVisitor<Analysis, Boolean>{

    public MathOpVisitor()
    {
        addVisit("AddOp", this::visitMathExpr);
        addVisit("SubOp", this::visitMathExpr);
        addVisit("MultOp", this::visitMathExpr);
        addVisit("DivOp", this::visitMathExpr);
    }

    public Boolean visitMathExpr(JmmNode node, Analysis analysis)
    {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);
        String parentMeth = Utils.getParentMethodName(node);

        checkNodeExpr(left, analysis, parentMeth);
        checkNodeExpr(right, analysis, parentMeth);

        return true;
    }

    private void checkNodeExpr(JmmNode node, Analysis analysis, String parentMethod)
    {
        if(Utils.isMathExpression(node.getKind()))
            return;
        else if(Utils.isBooleanExpression(node.getKind()))
            analysis.addReport(node, "\"" + node + "\" invalid operator. Expecting arithmetic, got boolean instead");
        else if(node.getKind().equals("DotExpr"))
        {
            String retType = Utils.getReturnValueMethod(node,analysis);
            if(!(retType.equals("undefined") || retType.equals("int")))
            {
                analysis.addReport(node, "\"" + node + "\" invalid type. Expecting int or undefined");
            }
        }
        else if(!node.getKind().equals("ArrayAccess") && !(Utils.getVariableType(node, analysis, parentMethod).equals("undefined") || Utils.getVariableType(node, analysis, parentMethod).equals("int")))
        {
            analysis.addReport(node, "\"" + node + "\" invalid type.");
        }
    }
}
