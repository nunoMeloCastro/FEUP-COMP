package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;


public class DotVisitor extends PreorderJmmVisitor<Analysis, Boolean>
{
    public DotVisitor()
    {
        addVisit("DotMethod", this::visitDotMethod);
    }

    public Boolean visitDotMethod(JmmNode node, Analysis analysis)
    {
        String methodName = node.getChildren().get(0).get("name");
        JmmNode param = node.getChildren().get(1);

        //If it does not appear on the symbol table, return
        if(!analysis.getSymbolTable().getMethods().contains(methodName)) return true;

        //Check left side of the dot expression
        JmmNode parent = node.getJmmParent();
        JmmNode left = parent.getChildren().get(0);

        if(!Utils.getNodeType(left, analysis).equals(analysis.getSymbolTable().getClassName())) return true;

        hasRightParams(param, analysis, methodName);

        return true;
    }

    public void hasRightParams(JmmNode node, Analysis analysis, String methodName)
    {
        List<Symbol> params = analysis.getSymbolTable().getParameters(methodName);
        boolean hasSuper = analysis.getSymbolTable().getSuper() != null;
        int argsGiven = node.getNumChildren();
        int argsDemanded = params.size();

        if(argsGiven != argsDemanded)
        {
            if(!hasSuper)
                analysis.addReport(node, "Wrong number of arguments. Provided: " + argsGiven + " Required: " + argsDemanded);
            return;
        }

        for(int i = 0; i < argsDemanded; i++)
        {
            Type type = params.get(i).getType();
            String reqType = type.getName() + (type.isArray() ? "[]" : "");

            String provided = Utils.getNodeType(node.getChildren().get(i), analysis);
            if(!provided.equals(reqType) && !provided.equals("undefined"))
            {
                analysis.addReport(node, "Parameter at position " + i + " has invalid type." +
                        " Provided: " + provided + " Required: " + reqType);
            }
        }
    }
}
