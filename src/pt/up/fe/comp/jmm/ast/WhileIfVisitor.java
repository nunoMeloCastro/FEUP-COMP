package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class WhileIfVisitor extends PreorderJmmVisitor<Analysis, Boolean>
{
    public WhileIfVisitor()
    {
        addVisit("ifStatement", this::visitConditional);
        addVisit("WhileStatement", this::visitConditional);
    }

    public Boolean visitConditional(JmmNode node, Analysis analysis)
    {
        JmmNode condNode = node.getChildren().get(0);
        String parentMethod = Utils.getParentMethodName(node);

        if(Utils.isBooleanExpression(condNode.getKind())) return true;
        if(Utils.isMathExpression(condNode.getKind()))
            analysis.addReport(condNode, "\"" + condNode + "\" expecting a boolean expression");
        else if(condNode.getKind().equals("Dot"))
        {
            String retValMeth = Utils.getReturnValueMethod(condNode, analysis);
            if(!retValMeth.equals("undefined") && !retValMeth.equals("boolean"))
                analysis.addReport(condNode, "\"" + condNode + "\" expecting a boolean expression");
        }
        else if(!Utils.getVariableType(condNode, analysis, parentMethod).equals("boolean"))
            analysis.addReport(condNode, "\"" + condNode + "\" expecting a boolean expression");

        return true;
    }
}
