package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.analysis.MySymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;

public class AssignmentVisitor extends PreorderJmmVisitor<Analysis, Boolean>{

    public AssignmentVisitor(){
        addVisit("Assignment", this::visitCheckAssignment);
    }

    public boolean testExtends(String left, String right, Analysis analysis)
    {
        if(analysis.getSymbolTable().getSuper() == null) return false;
        boolean normal = analysis.getSymbolTable().getClassName().equals(right);
        boolean hiper = analysis.getSymbolTable().getSuper().equals(left);
        return (normal && hiper);
    }

    //Very simple function to determine if two classes are imported.
    // If so, two instances of different classes can be assigned to one another
    public boolean testImports(String left, String right, Analysis analysis)
    {
        int count = 0;
        List<String> imp = analysis.getSymbolTable().getImports();

        for(int i = 0; i < imp.size(); i++)
        {
            if(imp.get(i).equals(left) || imp.get(i).equals(right))
                count++;
        }

        return (count == 2);
    }

    public Boolean visitCheckAssignment(JmmNode assignmentNode, Analysis analysis)
    {
        JmmNode leftN = assignmentNode.getChildren().get(0);
        JmmNode rightN = assignmentNode.getChildren().get(1);
        String parentMethodName = Utils.getParentMethodName(assignmentNode);

        String left, right;


        if(leftN.getKind().equals("Id"))
        {
            left = Utils.getVariableType(leftN, analysis, parentMethodName);
        }
        else
        {
            JmmNode arrayId = leftN.getChildren().get(0);
            left = Utils.getVariableType(arrayId, analysis, parentMethodName).split("\\[")[0];
        }


        if(rightN.getChildren().size() == 0)
        {
            right = Utils.getVariableType(rightN, analysis, parentMethodName);
            if(!right.equals(left))
            {
                if (testExtends(left, right, analysis) || testImports(left, right, analysis)) return true;
                else
                    analysis.addReport(rightN, "\"" + rightN + "\" and \"" + leftN + "\" incompatible types: " + right + " " + left);
            }
        }
        else if(Utils.isBooleanExpression(rightN.getKind()) && !left.equals("boolean"))
        {
            analysis.addReport(rightN, "\"" + rightN + "\" and \"" + leftN + "\" incompatible types.");
        }
        else if(Utils.isMathExpression(rightN.getKind()) && !left.equals("int") && !left.equals("int[]"))
        {
            analysis.addReport(rightN, "\"" + rightN + "\" and \"" + leftN + "\" incompatible types.");
        }
        else if(rightN.getKind().equals("DotExpr"))
        {
            String retVal = Utils.getReturnValueMethod(rightN, analysis);
            if(!retVal.equals("undefined") && !retVal.equals(left))
            {
                analysis.addReport(rightN, "\"" + rightN + "\" and \"" + leftN + "\" incompatible types.");
            }
        }
        return true;
    }
}
