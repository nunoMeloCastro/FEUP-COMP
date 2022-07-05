package pt.up.fe.comp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.ollir.ToOllir;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) throws IOException {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // read the input code
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }
        File inputFile = new File(args[0]);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[0] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(input, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // ... add remaining stages

        //Semantic stage
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        JmmSemanticsResult semanticsResult =  semanticAnalysis.semanticAnalysis(parserResult);

        //ollir stage
        var toOllir = new ToOllir();
        var ollirResult = toOllir.toOllir(semanticsResult);

        //jasmin stage
        var jasminEmitter = new JasminEmitter();
        var jasminResult = jasminEmitter.toJasmin(ollirResult);

        Path path = Paths.get(ollirResult.getSymbolTable().getClassName() + "/");
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        jasminResult.compile(path.toFile());
        jasminResult.run();
    }

}
