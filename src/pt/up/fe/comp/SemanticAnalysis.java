package pt.up.fe.comp;

import java.util.Arrays;
import pt.up.fe.comp.jmm.analysis.Analysis;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.ast.ArrayVisitor;
import pt.up.fe.comp.jmm.ast.AssignmentVisitor;
import pt.up.fe.comp.jmm.ast.BoolOpVisitor;
import pt.up.fe.comp.jmm.ast.DotVisitor;
import pt.up.fe.comp.jmm.ast.FunctionVisitor;
import pt.up.fe.comp.jmm.ast.MathOpVisitor;
import pt.up.fe.comp.jmm.ast.SymbolTableVisitor;
import pt.up.fe.comp.jmm.ast.UndefinedVisitor;
import pt.up.fe.comp.jmm.ast.Utils;
import pt.up.fe.comp.jmm.ast.WhileIfVisitor;

public class SemanticAnalysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        if(TestUtils.getNumErrors(parserResult.getReports()) > 0)
        {
            var errRep = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are errors from previous stage");

            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errRep));
        }

        if(parserResult.getRootNode() == null)
        {
            var errRep = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are is no AST root node");

            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errRep));
        }

        Analysis analysis = new Analysis();

        JmmNode root = parserResult.getRootNode();
        new SymbolTableVisitor().visit(root, analysis);
        new UndefinedVisitor().visit(root, analysis);
        new FunctionVisitor().visit(root, analysis);
        new DotVisitor().visit(root, analysis);
        new ArrayVisitor().visit(root, analysis);
        new AssignmentVisitor().visit(root, analysis);
        new MathOpVisitor().visit(root, analysis);
        new BoolOpVisitor().visit(root, analysis);
        new WhileIfVisitor().visit(root, analysis);

        return new JmmSemanticsResult(parserResult, analysis.getSymbolTable(), analysis.getReports());
    }
}
