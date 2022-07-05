package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Map;
import java.util.stream.Collectors;

import static org.specs.comp.ollir.AccessModifiers.DEFAULT;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final FunctionClassMap<Instruction, String> instructionMap;
    private int stackCnt;
    private int stackLimit;
    private int locals;
    private int label;
    private Method method;

    public OllirToJasmin(ClassUnit classUnit) {

        this.classUnit = classUnit;

        instructionMap = new FunctionClassMap<>();
        instructionMap.put(CallInstruction.class, this::getCode);
        instructionMap.put(AssignInstruction.class, this::getCode);
        instructionMap.put(GotoInstruction.class, this::getCode);
        instructionMap.put(ReturnInstruction.class, this::getCode);
        instructionMap.put(PutFieldInstruction.class, this::getCode);
        instructionMap.put(GetFieldInstruction.class, this::getCode);
        instructionMap.put(BinaryOpInstruction.class, this::getCode);
        instructionMap.put(OpCondInstruction.class, this::getCode);
        instructionMap.put(SingleOpInstruction.class, this::getCode);
    }

    public String getCode(){

        classUnit.buildVarTables();
        var code = new StringBuilder();

        code.append(".class public ").append(classUnit.getClassName()).append("\n");

        if (classUnit.getSuperClass() != null) {
            var superQualifiedName = getFullyQualifiedName(classUnit.getSuperClass());
            code.append(".super ").append(superQualifiedName).append("\n");
        }

        else
            code.append(".super java/lang/Object\n");

        for (Field f : classUnit.getFields()) {

            switch (f.getFieldAccessModifier()){

                case PUBLIC:
                    code.append("\t.field public ");
                    break;

                case PRIVATE:
                    code.append("\t.field private ");
                    break;

                case PROTECTED:
                    code.append("\t.field protected ");
                    break;

                default:
                    code.append("\t.field ");
                    break;
            }

            code.append(f.getFieldName()).append(" ")
                .append(getJasminType(f.getFieldType()))
                .append("\n");
        }

        this.label = 0;

        for (var method : classUnit.getMethods()){

            this.stackCnt = 0;
            this.stackLimit = 0;
            this.method = method;

            if (method.isConstructMethod()) {

                String superClass;

                if (classUnit.getSuperClass() != null)
                    superClass = getFullyQualifiedName(classUnit.getSuperClass());

                else
                    superClass = "java/lang/Object";

                var methodParamTypes = method.getParams().stream()
                        .map(element -> getJasminType(element.getType()))
                        .collect(Collectors.joining());

                code.append(".method public <init>(")
                    .append(methodParamTypes)
                    .append(")V\n")
                    .append("\t\taload_0\n")
                    .append("\t\tinvokespecial ").append(superClass).append("/<init>()V\n");

                for (var inst : method.getInstructions()){
                    if (!(inst instanceof CallInstruction))
                    code.append(getCode(inst));
                }

                code.append("\t\treturn\n");
                code.append(".end method\n");

                increaseStackCounter();
            }

            else
                code.append(getCode(method));
        }

        return code.toString();
    }

    public String getCode(Method method){

        this.locals = method.getVarTable().size() + 1;
        var code = new StringBuilder();
        var accessMod = method.getMethodAccessModifier();
        var methodParamTypes = method.getParams().stream()
            .map(element -> getJasminType(element.getType()))
            .collect(Collectors.joining());

        if(accessMod == DEFAULT)
            code.append(".method public");

        else
            code.append(".method ").append(accessMod.name().toLowerCase()).append(" ");

        if(method.isStaticMethod()){
            code.append("static ");
            this.locals++;
        }

        code.append(method.getMethodName()).append("(")
            .append(methodParamTypes)
            .append(")")
            .append(getJasminType(method.getReturnType()))
            .append("\n")
            .append("\t.limit stack $STACKLIMIT \n")
            .append("\t.limit locals " + this.locals + "\n");

        var ret = true;
        for (var inst : method.getInstructions()){

            if (inst.getInstType().equals(InstructionType.RETURN))
                ret = false;
            for (Map.Entry<String, Instruction> entry : method.getLabels().entrySet())
                if (entry.getValue().equals(inst))
                    code.append("\t").append(entry.getKey()).append(":\n");

            code.append(getCode(inst));
        }

        if(ret)
            code.append("\t\treturn\n");
        code.append(".end method\n");

        return code.toString().replace("$STACKLIMIT", String.valueOf(this.stackLimit));
    }

    public String getCode(Instruction instruction){
        return instructionMap.apply(instruction);
    }

    public String getCode(CallInstruction instruction){

        var code = new StringBuilder();

        switch (instruction.getInvocationType()){

            case invokestatic:
                return getCodeInvokeStatic(instruction);

            case invokeinterface:
                return getCodeInvokeOther(instruction, "\tinvokeinterface");

            case invokespecial:
                return getCodeInvokeOther(instruction, "\tinvokespecial");

            case invokevirtual:
                return getCodeInvokeOther(instruction, "\tinvokevirtual");

            case arraylength:
                return code.append(loadElement(instruction.getFirstArg()))
                    .append("\n")
                    .append("\t\tarraylength\n")
                    .toString();

            case NEW:
                return getCodeNew(instruction);

            case ldc:
                code.append("\t\tldc ").append(this.stackCnt).append("\n");
                increaseStackCounter();
                return code.toString();

            default:
                throw new NotImplementedException(instruction.getInvocationType());
        }
    }

    public String getCode(ReturnInstruction instruction){

        var code = new StringBuilder();

        if(instruction.hasReturnValue()){
            var elType = instruction.getOperand().getType().getTypeOfElement();

            switch (elType){

                case VOID:
                    code.append("\t\treturn\n");
                    break;

                case INT32:
                case BOOLEAN:
                    code.append(loadElement(instruction.getOperand())).append("\n").append("\t\tireturn\n");
                    decreaseStackCounter();
                    break;

                case ARRAYREF:
                case OBJECTREF:
                    code.append(loadElement(instruction.getOperand())).append("\n").append("\t\tareturn\n");
                    decreaseStackCounter();
                    break;

                default:
                    throw new NotImplementedException(elType);
            }

            return code.toString();
        }

        else
            return code.append("\t\treturn\n").toString();
    }

    public String getCode(GotoInstruction instruction){
        return "\t\tgoto " + instruction.getLabel() + "\n";
    }

    public String getCode(AssignInstruction instruction){

        var code = new StringBuilder();
        Operand operand = (Operand) instruction.getDest();

        if (operand instanceof ArrayOperand){

            ArrayOperand arrayOperand = (ArrayOperand) operand;

            if(this.stackCnt >= 0 && this.stackCnt <= 3)
                code.append("\t\taload_").append(this.stackCnt).append("\n");

            else
                code.append("\t\taload ").append(this.stackCnt).append("\n");

            increaseStackCounter();

            code.append(loadElement(arrayOperand.getIndexOperands().get(0))).append("\n");
        }

        code.append(getCode(instruction.getRhs()));

        if(! (operand.getType().getTypeOfElement().equals(ElementType.OBJECTREF)
                && instruction.getRhs() instanceof CallInstruction))
            code.append(storeElement(operand)).append("\n");

        return code.toString();
    }

    public String getCode(BinaryOpInstruction instruction){

        var code = new StringBuilder();

        switch (instruction.getOperation().getOpType()){

            case ADD:
                code.append(loadElement(instruction.getLeftOperand()))
                    .append("\n")
                    .append(loadElement(instruction.getRightOperand()))
                    .append("\n")
                    .append("\t\tiadd\n");
                break;

            case SUB:
                code.append(loadElement(instruction.getLeftOperand()))
                    .append("\n")
                    .append(loadElement(instruction.getRightOperand()))
                    .append("\n")
                    .append("\t\tisub\n");
                break;

            case MUL:
                code.append(loadElement(instruction.getLeftOperand()))
                    .append("\n")
                    .append(loadElement(instruction.getRightOperand()))
                    .append("\n")
                    .append("\t\timul\n");
                break;

            case DIV:
                code.append(loadElement(instruction.getLeftOperand()))
                    .append("\n")
                    .append(loadElement(instruction.getRightOperand()))
                    .append("\n")
                    .append("\t\tidiv\n");
                break;

            case LTH:
                code.append(loadElement(instruction.getLeftOperand())).append("\n")
                    .append(loadElement(instruction.getRightOperand())).append("\n")
                    .append("\t\tif_icmpge ").append("ifTrue").append(this.label).append("\n")
                    .append("\t\ticonst_1\n")
                    .append("\t\tgoto ").append("End").append(this.label).append("\n")
                    .append("\tifTrue").append(this.label).append(":\n")
                    .append("\t\ticonst_0\n")
                    .append("\tEnd").append(this.label).append(":\n");

                this.label++;
                System.out.println(this.label);
                break;

            case GTE:
                code.append(loadElement(instruction.getLeftOperand())).append("\n")
                    .append(loadElement(instruction.getRightOperand())).append("\n")
                    .append("\t\tif_icmplt ").append("ifTrue").append(this.label).append("\n")
                    .append("\t\ticonst_1\n")
                    .append("\t\tgoto ").append("End").append(this.label).append("\n")
                    .append("\tifTrue").append(this.label).append(":\n")
                    .append("\t\ticonst_0\n")
                    .append("\tEnd").append(this.label).append(":\n");

                decreaseStackCounter();
                this.label++;
                System.out.println(this.label);
                break;

            case ANDB:
                code.append(loadElement(instruction.getLeftOperand()))
                    .append("\n")
                    .append("\t\tifeq ")
                    .append("ifTrue")
                    .append(this.label)
                    .append("\n");

                decreaseStackCounter();

                code.append(loadElement(instruction.getRightOperand()))
                    .append("\n")
                    .append("\t\tifeq ")
                    .append("ifTrue")
                    .append(this.label)
                    .append("\n");

                decreaseStackCounter();

                code.append("\t\ticonst_1\n")
                    .append("\t\tgoto ").append("End").append(this.label).append("\n")
                    .append("\tifTrue").append(this.label).append(":\n")
                    .append("\t\ticonst_0\n")
                    .append("\tEnd").append(this.label).append(":\n");

                this.label++;
                System.out.println(this.label);
                increaseStackCounter();
                increaseStackCounter();
                break;

            case NOTB:
                code.append(loadElement(instruction.getLeftOperand())).append("\n")
                    .append("\t\tifne ").append("ifTrue").append(this.label).append("\n")
                    .append("\t\ticonst_1\n")
                    .append("\t\tgoto ").append("End").append(this.label).append("\n")
                    .append("\tifTrue").append(this.label).append(":\n")
                    .append("\t\ticonst_0\n")
                    .append("\tEnd").append(this.label).append(":\n");

                this.label++;
                System.out.println(this.label);
                increaseStackCounter();
                increaseStackCounter();
                break;

            default:
                throw new NotImplementedException(instruction.getOperation().getOpType());
        }

        this.decreaseStackCounter();
        return code.toString();
    }

    public String getCode(PutFieldInstruction instruction){

        var code = new StringBuilder();
        Operand first = (Operand) instruction.getFirstOperand();
        Operand second = (Operand) instruction.getSecondOperand();
        Element third = instruction.getThirdOperand();

        code.append(loadElement(first))
            .append("\n")
            .append(loadElement(third))
            .append("\n");

        code.append("\t\tputfield ")
            .append(classUnit.getClassName())
            .append("/")
            .append(second.getName())
            .append(" ")
            .append(getJasminType(second.getType()))
            .append("\n");

        this.decreaseStackCounter();
        this.decreaseStackCounter();
        return code.toString();
    }

    public String getCode(GetFieldInstruction instruction){

        var code = new StringBuilder();
        Operand first = (Operand) instruction.getFirstOperand();
        Operand second = (Operand) instruction.getSecondOperand();

        code.append(loadElement(first)).append("\n");

        code.append("\t\tgetfield ")
            .append(classUnit.getClassName())
            .append("/")
            .append(second.getName())
            .append(" ")
            .append(getJasminType(second.getType()))
            .append("\n");

        return code.toString();
    }

    public String getCode(OpCondInstruction instruction){

        var code = new StringBuilder();
        switch (instruction.getCondition().getOperation().getOpType()){

            case GTE:
                code.append(loadElement(instruction.getOperands().get(0))).append("\n")
                    .append(loadElement(instruction.getOperands().get(1))).append("\n")
                    .append("\t\tif_icmplt ").append(instruction.getLabel()).append(this.label++).append("\n")
                    .append("\t\tgoto ").append(instruction.getLabel()).append(this.label--).append("\n")
                    .append("\t").append(instruction.getLabel()).append(this.label++).append(":\n")
                    .append("\t\tgoto ").append(instruction.getLabel()).append("\n")
                    .append("\t").append(instruction.getLabel()).append(this.label).append(":\n");

                decreaseStackCounter();
                break;

            case ANDB:
                code.append(loadElement(instruction.getOperands().get(0)))
                    .append("\n")
                    .append("\t\tifeq ")
                    .append(instruction.getLabel())
                    .append(this.label)
                    .append("\n");

                decreaseStackCounter();

                code.append(loadElement(instruction.getOperands().get(1)))
                    .append("\n")
                    .append("\t\tifeq ")
                    .append(instruction.getLabel())
                    .append(this.label++)
                    .append("\n");

                decreaseStackCounter();

                code.append("\t\tgoto ").append(instruction.getLabel()).append(this.label--).append("\n")
                    .append("\t").append(instruction.getLabel()).append(this.label++).append(":\n")
                    .append("\t\tgoto ").append(instruction.getLabel()).append("\n")
                    .append("\t").append(instruction.getLabel()).append(this.label).append(":\n");

                increaseStackCounter();
                increaseStackCounter();
                break;

            case EQ:
                code.append(loadElement(instruction.getOperands().get(0))).append("\n")
                    .append(loadElement(instruction.getOperands().get(1))).append("\n")
                    .append("\t\tif_icmpne ").append(instruction.getLabel()).append(this.label++).append("\n")
                    .append("\t\tgoto ").append(instruction.getLabel()).append(this.label--).append("\n")
                    .append("\t").append(instruction.getLabel()).append(this.label++).append(":\n")
                    .append("\t\tgoto ").append(instruction.getLabel()).append("\n")
                    .append("\t").append(instruction.getLabel()).append(this.label).append(":\n");

                decreaseStackCounter();
                break;

            case LTH:
                code.append(loadElement(instruction.getOperands().get(0))).append("\n")
                        .append(loadElement(instruction.getOperands().get(1))).append("\n")
                        .append("\t\tif_icmpge ").append(instruction.getLabel()).append(this.label++).append("\n")
                        .append("\t\tgoto ").append(instruction.getLabel()).append(this.label--).append("\n")
                        .append("\t").append(instruction.getLabel()).append(this.label++).append(":\n")
                        .append("\t\tgoto ").append(instruction.getLabel()).append("\n")
                        .append("\t").append(instruction.getLabel()).append(this.label).append(":\n");


            default:
                throw new NotImplementedException(instruction.getCondition().getOperation().getOpType());
        }

        return code.toString();
    }

    public String getCode(SingleOpInstruction instruction){
        return loadElement(instruction.getSingleOperand()) + "\n";
    }

    private String getCodeInvokeStatic(CallInstruction instruction){

        var code = new StringBuilder();
        String methodClass;

        for (Element el : instruction.getListOfOperands())
            code.append(loadElement(el)).append("\n");

        code.append("\t\tinvokestatic ");

        if (instruction.getFirstArg().getType().toString().equals("OBJECTREF")){
            var aux = getJasminType((instruction.getFirstArg()).getType());
            methodClass = aux.substring(1, aux.length() - 1);
        }

        else
            methodClass = ((Operand) instruction.getFirstArg()).getName();

        var calledMethod =((LiteralElement) instruction.getSecondArg()).getLiteral();

        code.append(getObjectClassName(methodClass))
            .append("/")
            .append(calledMethod, 1, calledMethod.length() - 1)
            .append("(");

        for (var operand : instruction.getListOfOperands())
            code.append(getCodeArg(operand));

        code.append(")")
            .append(getJasminType(instruction.getReturnType()))
            .append("\n");

        decreaseNStackCounter(instruction.getListOfOperands().size());

        return code.toString();
    }

    private String getCodeInvokeOther(CallInstruction instruction, String invokeType) {

        var code = new StringBuilder();
        var calledMethod =((LiteralElement) instruction.getSecondArg()).getLiteral();
        String methodClass;

        if (!((LiteralElement) instruction.getSecondArg()).getLiteral().equals("\"<init>\""))
            code.append(loadElement(instruction.getFirstArg())).append("\n");

        for (Element el : instruction.getListOfOperands()){
            code.append(loadElement(el)).append("\n");
        }

        decreaseNStackCounter(instruction.getListOfOperands().size());

        if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID)
            increaseStackCounter();

        code.append(invokeType).append(" ");

        if (instruction.getFirstArg().getType().toString().equals("OBJECTREF")){
            var aux = getJasminType((instruction.getFirstArg()).getType());
            methodClass = aux.substring(1, aux.length() - 1);
        }

        else
            methodClass = ((Operand) instruction.getFirstArg()).getName();

        code.append(getObjectClassName(methodClass))
            .append("/")
            .append(calledMethod, 1, calledMethod.length() - 1)
            .append("(");

        var aux = new StringBuilder();

        for (var operand : instruction.getListOfOperands())
            aux.append(getCodeArg(operand)).append(", ");

        if (instruction.getListOfOperands().size() != 0)
            code.append(aux.substring(0, aux.length() - 2));

        code.append(")")
            .append(getJasminType(instruction.getReturnType()))
            .append("\n");

        if (((LiteralElement) instruction.getSecondArg()).getLiteral().equals("\"<init>\"")
                && !((Operand) instruction.getFirstArg()).getName().equals("this"))
            code.append(storeElement(instruction.getFirstArg())).append("\n");

        return code.toString();
    }

    public String getCodeNew(CallInstruction instruction){

        var element = instruction.getFirstArg();
        var code = new StringBuilder();
        var elType = element.getType().getTypeOfElement();

        switch (elType){

            case ARRAYREF:
                return code.append(loadElement(instruction.getListOfOperands().get(0)))
                        .append("\n")
                        .append("\t\tnewarray int\n")
                        .toString();

            case OBJECTREF:
                increaseStackCounter();
                increaseStackCounter();

                return code.append("\t\tnew ")
                        .append(getObjectClassName(((Operand) element).getName()))
                        .append("\n\t\tdup\n").toString();

        }
        throw new NotImplementedException(elType);
    }

    private String getCodeArg(Element operand) {
        return getJasminType(operand.getType());
    }

    public String getJasminType(Type type){

        var code = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();

        if(type instanceof ArrayType){
            elementType = ((ArrayType)type).getArrayType();
            code.append("[");
        }

        switch (elementType){

            case STRING:
                return code.append("Ljava/lang/String;").toString();

            case VOID:
                return code.append("V").toString();

            case CLASS:
                return code.append("CLASS").toString();

            case INT32:
                return code.append("I").toString();

            case BOOLEAN:
                return code.append("Z").toString();

            case OBJECTREF:

                String className;
                String aux;

                if (type instanceof ClassType) {
                    className = ((ClassType) type).getName();
                    return code.append("L").append(getObjectClassName(className)).append(";").toString();
                }

                else if (type instanceof ArrayType) {
                    aux = ((ArrayType) type).getElementClass().getClass().toString();
                    className = aux.replace("class ", "");
                    return code.append("L").append(className).append(";").toString();
                }

            default:
                throw new NotImplementedException(type);
        }
    }

    public String getFullyQualifiedName(String className){

        for (var importString : classUnit.getImports()){

            var splittedImport = importString.split("\\.");
            String lastName;

            if(splittedImport.length == 0)
                lastName = importString;

            else
                lastName = splittedImport[splittedImport.length - 1];

            if(lastName.equals(className))
                return importString.replace(".", "/");
        }

        throw new RuntimeException("Could not find import for class" + className);
    }

    private String getObjectClassName(String className) {

        for (String _import : classUnit.getImports())
            if (_import.endsWith("." + className))
                return _import.replaceAll("\\.", "/");

        return className;
    }

    private String loadElement(Element element){

        var code = new StringBuilder();

        if (element instanceof LiteralElement){

            var el = ((LiteralElement) element).getLiteral();
            var num = Integer.parseInt(el);

            if (num >= 0 && num <= 5)
                code.append("\t\ticonst_").append(num);

            else if (num == -1)
                code.append("\t\ticonst_m1");

            else
                code.append("\t\tldc ").append(num);

            this.increaseStackCounter();
            return code.toString();
        }

        var name = ((Operand) element).getName();
        var varIndex = this.method.getVarTable().get(name).getVirtualReg();
        var elType = element.getType().getTypeOfElement();

        if (element instanceof ArrayOperand){
            ArrayOperand operand = (ArrayOperand) element;

            if(varIndex >= 0 && varIndex <= 3)
                code.append("\t\taload_").append(varIndex).append("\n");
            else
                code.append("\t\taload ").append(varIndex).append("\n");

            return code.append(loadElement(operand.getIndexOperands().get(0)))
                        .append("\n")
                        .append("\t\tiaload")
                        .toString();
        }

        else {

            switch (elType){

                case THIS:
                    increaseStackCounter();
                    return code.append("\t\taload_0").toString();

                case INT32:
                case BOOLEAN:
                    if (varIndex >= 0 && varIndex <= 3)
                        code.append("\t\tiload_").append(varIndex);
                    else
                        code.append("\t\tiload ").append(varIndex);
                    break;

                case ARRAYREF:
                case OBJECTREF:
                case CLASS:
                case STRING:
                    if(varIndex >= 0 && varIndex <= 3)
                        code.append("\t\taload_").append(varIndex);
                    else
                        code.append("\t\taload ").append(varIndex);
                    break;

                default:
                    throw new NotImplementedException(elType);
            }

            this.increaseStackCounter();
            return code.toString();
        }
    }

    private String storeElement(Element element){

        var name= ((Operand) element).getName();
        var varIndex = this.method.getVarTable().get(name).getVirtualReg();
        var code = new StringBuilder();
        var elType = element.getType().getTypeOfElement();

        if (element instanceof ArrayOperand)
            return code.append("\t\tiastore").toString();

        switch (elType){

            case INT32:
            case BOOLEAN:
                if(varIndex >= 0 && varIndex <= 3)
                    code.append("\t\tistore_").append(varIndex);
                else
                    code.append("\t\tistore ").append(varIndex);
                break;

            case OBJECTREF:
            case ARRAYREF:
            case STRING:
                if(varIndex >= 0 && varIndex <= 3)
                    code.append("\t\tastore_").append(varIndex);
                else
                    code.append("\t\tastore ").append(varIndex);
                break;

            default:
                throw new NotImplementedException(elType);
        }

        this.decreaseStackCounter();
        return code.toString();
    }

    private void increaseStackCounter(){
        this.stackCnt++;
        //System.out.println(stackCnt);
        if (this.stackCnt > this.stackLimit)
            this.stackLimit = stackCnt;
    }

    private void decreaseStackCounter(){
        this.stackCnt--;
    }

    private void decreaseNStackCounter(int num){
        this.stackCnt -= num;
    }
}
