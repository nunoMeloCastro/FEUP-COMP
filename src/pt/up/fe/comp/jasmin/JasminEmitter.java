package pt.up.fe.comp.jasmin;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class JasminEmitter implements JasminBackend {

    public JasminResult toJasmin(OllirResult ollirResult) {
        String jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).getCode();

        return new JasminResult(ollirResult, jasminCode, Collections.emptyList());

    }
}
