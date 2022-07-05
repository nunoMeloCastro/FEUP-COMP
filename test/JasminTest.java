import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class JasminTest {
    @Test
    public void helloWorld(){
        var jasminResult = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/jasmin/HelloWorld.ollir"), Collections.emptyMap()));
        TestUtils.noErrors(jasminResult);
        var otj = new JasminEmitter();
        System.out.println(otj.toJasmin(new OllirResult(SpecsIo.getResource("fixtures/public/jasmin/HelloWorld.ollir"), Collections.emptyMap())).getJasminCode());

        //jasminResult.compile();
        jasminResult.run();
    }
    @Test
    public void fac(){
        var jasminResult = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir"), Collections.emptyMap()));
        TestUtils.noErrors(jasminResult);
        var otj = new JasminEmitter();
        System.out.println(otj.toJasmin(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir"), Collections.emptyMap())).getJasminCode());

        //jasminResult.compile();
        jasminResult.run();
    }
    @Test
    public void myclass1(){
        var jasminResult = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass1.ollir"), Collections.emptyMap()));
        TestUtils.noErrors(jasminResult);
        var otj = new JasminEmitter();
        System.out.println(otj.toJasmin(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass1.ollir"), Collections.emptyMap())).getJasminCode());

        //jasminResult.compile();
        jasminResult.run();
    }
    @Test
    public void myclass2(){
        var jasminResult = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass2.ollir"), Collections.emptyMap()));
        TestUtils.noErrors(jasminResult);
        var otj = new JasminEmitter();
        System.out.println(otj.toJasmin(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass2.ollir"), Collections.emptyMap())).getJasminCode());

        //jasminResult.compile();
        jasminResult.run();
    }

    @Test
    public void myclass3(){
        var jasminResult = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir"), Collections.emptyMap()));
        TestUtils.noErrors(jasminResult);
        var otj = new JasminEmitter();
        System.out.println(otj.toJasmin(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir"), Collections.emptyMap())).getJasminCode());

        //jasminResult.compile();
        jasminResult.run();
    }

    @Test
    public void myclass4(){
        var jasminResult = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass4.ollir"), Collections.emptyMap()));
        TestUtils.noErrors(jasminResult);
        var otj = new JasminEmitter();
        System.out.println(otj.toJasmin(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass4.ollir"), Collections.emptyMap())).getJasminCode());

        //jasminResult.compile();
        jasminResult.run();
    }
}
