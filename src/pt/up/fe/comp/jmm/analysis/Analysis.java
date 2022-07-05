package pt.up.fe.comp.jmm.analysis;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;
import java.util.ArrayList;

public class Analysis implements ReportsProvider {

    MySymbolTable symbolTable;
    List<Report> reports;

    public Analysis() {
        this.symbolTable = new MySymbolTable();
        this.reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    public MySymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void addReport(JmmNode node, String message) {
        Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, -1, message);
        reports.add(report);
    }
}
