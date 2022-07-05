package pt.up.fe.comp.ollir;

import pt.up.fe.comp.SemanticAnalysis;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class OllirVisitor extends AJmmVisitor<Object, String> {

    private final JmmSemanticsResult semanticsResult;
    int tempvarscont;
    int ifcounter;
    int elsecounter;
    int loopcounter;

    StringBuilder builder = new StringBuilder();
    StringBuilder builderConstruct = new StringBuilder();//builder for construct
    StringBuilder builderMethod = new StringBuilder();//builder for method
    StringBuilder builderDotExpression = new StringBuilder();//builder for dot
    StringBuilder builderArrayA = new StringBuilder();//builder for dot



    private final SymbolTable sybolTabel;

    private Hashtable<String, String> htvars = new Hashtable<String, String>();
    private List<String> limports=new ArrayList<String>();
    private List<String> lmethpara=new ArrayList<String>();


    public OllirVisitor(JmmSemanticsResult semanticsResult) {
        this.semanticsResult = semanticsResult;

        // se for posivel implementar
        this.sybolTabel = this.semanticsResult.getSymbolTable();

        addVisit("AddOp", this::visitAddOp);
        addVisit("SubOp", this::visitSubOp);
        addVisit("MultOp", this::visitMultOp);
        addVisit("DivOp", this::visitDivOp);
        addVisit("LessOp", this::visitLessOp);
        addVisit("NotOp", this::visitNotOp);
        addVisit("AndOp", this::visitAndOp);

        addVisit("Number", this::visitNumber);
        addVisit("Correct", this::visitCorrect);
        addVisit("Incorrect", this::visitIncorrect);
        addVisit("Id", this::visitId);
        addVisit("Type", this::visitType);
        addVisit("Len", this::visitLen);
        addVisit("StringArray", this::visitStringArray);
        addVisit("NewIntArray", this::visitNewIntArray);
        addVisit("ArrayAccess", this::visitArrayAccess);
        addVisit("ArrayAssignment", this::visitArrayAssignment);
        addVisit( "ArrayExpression", this::visitArrayExpression);

        addVisit("Assignment", this::visitAssignment);
        addVisit("DotExpr", this::visitDotExpr);
        addVisit("DotMethod", this::visitDotMethod);
        addVisit("ObjectMethodParam", this::visitObjectMethodParam);
        addVisit("NewObject", this::visitNewObject);

        addVisit("Importation", this::visitImportation);
        addVisit("ImportedClasses", this::visitImportedClasses);
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("ClassName", this::visitClassName);
        addVisit("ClassFields", this::visitClassFields);
        addVisit("VarDeclaration", this::visitVarDeclaration);
        addVisit("VarDeclarations", this::visitVarDeclarations);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("MethodGeneric", this::visitMethodGeneric);
        addVisit("MethodMain", this::visitMethodMain);

        addVisit("MethodParameters", this::visitMethodParameters);
        addVisit("MethodParameter", this::visitMethodParameter);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("ReturnStatement", this::visitReturnStatement);

        addVisit("MainParameter", this::visitMainParameter);

        addVisit("ifStatement", this::visitIfStatement);
        addVisit("IfBlock", this::visitIfBlock);
        addVisit("ElseBlock", this::visitElseBlock);
        addVisit("WhileStatement", this::visitWhileStatement);
        addVisit("WhileBody", this::visitWhileBody);
    }

    private String visitNumber(JmmNode node, Object o) {
        String str = node.get("value") + ".i32";
        // builderMethod.append(str);
        return str;
    }

    private String visitCorrect(JmmNode node, Object o) {
        String str = "1.bool";
        //builderMethod.append(str);
        return str;
    }

    private String visitIncorrect(JmmNode node, Object o) {
        String str = "0.bool";
        //builderMethod.append(str);
        return str;
    }

    private String visitAddOp(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        System.out.println("lchild.getKind()::\n" + lchild.getKind()
                + "\nlchild.getKind().equals(\"AddOp\") ::" + lchild.getKind().equals("AddOp") + "\n---");

        String str = "";
        if (lchild.getNumChildren() > 0 && !(lchild.getKind().compareTo("ArrayAccess") == 0)) {
            tempvarscont++;
            str = "t" + tempvarscont + ".i32 +.i32 " + visit(rchild);
            // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
            builderMethod.append("t" + tempvarscont + ".i32 :=.i32 " + visit(lchild) + ";\n");
            // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
        } else {
            if (htvars.containsKey(visit(lchild)) && htvars.containsKey(visit(rchild))) {
                str = (visit(lchild) + ".i32 +.i32 " + visit(rchild) + ".i32");
            } else if (htvars.containsKey(visit(lchild))){
                str = (visit(lchild) + ".i32 +.i32 " + visit(rchild));
            }else if (htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + " +.i32 " + visit(rchild) + ".i32");
            }else{
                str = (visit(lchild) + " +.i32 " + visit(rchild));
            }
        }

        return str;
    }

    private String visitSubOp(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        String str = "";
        if (lchild.getNumChildren() > 0) {
            if(lchild.getKind().compareTo("DotExpr")== 0){
                tempvarscont++;
                str = "t" + tempvarscont + ".i32 -.i32 " + visit(rchild);
                builderDotExpression.append("t" + tempvarscont + ".i32 :=.i32 " + visit(lchild) + ";\n");
            }
            else {
                tempvarscont++;
                str = "t" + tempvarscont + ".i32 -.i32 " + visit(rchild);
                // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
                builderMethod.append("t" + tempvarscont + ".i32 :=.i32 " + visit(lchild) + ";\n");
                // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
            }
        } else {
            if (htvars.containsKey(visit(lchild)) && htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + ".i32 -.i32 " + visit(rchild) + ".i32");}
            else if (htvars.containsKey(visit(lchild))){
                str = (visit(lchild) + ".i32 -.i32 " + visit(rchild));}
            else if (htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + " -.i32 " + visit(rchild) + ".i32");}
            else{
                str = (visit(lchild) + " -.i32 " + visit(rchild));}
        }

        return str;
    }

    private String visitMultOp(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        String str = "";
        if (lchild.getNumChildren() > 0) {
            tempvarscont++;
            str = "t" + tempvarscont + ".i32 *.i32 " + visit(rchild);
            // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
            builderMethod.append("t" + tempvarscont + ".i32 :=.i32 " + visit(lchild) + ";\n");
            // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
        } else {
            if (htvars.containsKey(visit(lchild)) && htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + ".i32 *.i32 " + visit(rchild) + ".i32");}
            else if (htvars.containsKey(visit(lchild))){
                str = (visit(lchild) + ".i32 *.i32 " + visit(rchild));}
            else if (htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + " *.i32 " + visit(rchild) + ".i32");}
            else{
                str = (visit(lchild) + " *.i32 " + visit(rchild));}
        }

        return str;
    }

    private String visitDivOp(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        String str = "";
        if (lchild.getNumChildren() > 0) {
            tempvarscont++;
            str = "t" + tempvarscont + ".i32 /.i32 " + visit(rchild);
            // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
            builderMethod.append("t" + tempvarscont + ".i32 :=.i32 " + visit(lchild) + ";\n");
            // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
        } else {
            if (htvars.containsKey(visit(lchild)) && htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + ".i32 /.i32 " + visit(rchild) + ".i32");}
            else if (htvars.containsKey(visit(lchild))) {
                str = (visit(lchild) + ".i32 /.i32 " + visit(rchild));
            }else if (htvars.containsKey(visit(rchild))) {
                str = (visit(lchild) + " /.i32 " + visit(rchild) + ".i32");
            }else {
                str = (visit(lchild) + " /.i32 " + visit(rchild));
            }
        }

        return str;
    }

    private String visitLessOp(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        String str = "";
        if (lchild.getNumChildren() > 0) {
            tempvarscont++;
            str = "t" + tempvarscont + ".i32 >=.bool " + visit(rchild);
            // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
            builderMethod.append("t" + tempvarscont + ".i32 :=.i32 " + visit(lchild) + ";\n");
            // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
        } else {
            if (htvars.containsKey(visit(lchild)) && htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + ".i32 >=.bool " + visit(rchild) + ".i32");}
            else if (htvars.containsKey(visit(lchild))){
                str = (visit(lchild) + ".i32 >=.bool " + visit(rchild));}
            else if (htvars.containsKey(visit(rchild))){
                str = (visit(lchild) + " >=.bool " + visit(rchild) + ".i32");}
            else {
                str = (visit(lchild) + " >=.bool " + visit(rchild));
            }
        }
        return str;
        /*
        if (lchild.getKind() != "int") {
            tempvarscont++;
            builderMethod.append("t" + tempvarscont + "<.bool");
            visit(rchild);
            builderMethod.append("\n" + "t" + tempvarscont + ".i32 :=.i32 ");
            visit(lchild);
        } else {
            visit(lchild);
            builderMethod.append("<.bool");
            visit(rchild);
        }
        return builderMethod.toString();
         */
    }

    private String visitNotOp(JmmNode node, Object o) {

        JmmNode lchild = node.getJmmChild(0);

        if (lchild.getKind() != "int") {
            tempvarscont++;
            builderMethod.append("!.bool" + "t" + tempvarscont);
            builderMethod.append("\n" + "t" + tempvarscont + ".bool :=.bool ");
            visit(lchild);
            return builderMethod.toString();
        } else {

            builderMethod.append("!.boll");
            visit(lchild);
        }
        return "";
    }

    private String visitAndOp(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        String str = "";
        if (lchild.getNumChildren() > 0) {
            tempvarscont++;
            str = "t" + tempvarscont + ".bool &&.bool " + visit(rchild);
            // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
            builderMethod.append("t" + tempvarscont + ".bool :=.bool " + visit(lchild) + ";\n");
            // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
        } else {
            System.out.println("visit(rchild)::"+visit(rchild));
                str = visit(lchild) + " &&.bool " + visit(rchild);
        }

        return str;
        /*
        if (lchild.getKind() != "int") {
            tempvarscont++;
            builderMethod.append("t" + tempvarscont + "&&.bool");
            visit(rchild);
            builderMethod.append("\n" + "t" + tempvarscont + ".bool :=.bool ");
            visit(lchild);
        } else {
            visit(lchild);
            builderMethod.append("&&.bool");
            visit(rchild);
        }
        return builderMethod.toString();
        */
    }

    private String visitAssignment(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        String str = "";
        if (htvars.containsKey(visit(rchild))) {
            if (htvars.get(visit(rchild)).compareTo("int") == 0){
                str += visit(lchild) + ".i32 :=.i32 " + visit(rchild) + ".i32;\n";}
            else if (htvars.get(visit(rchild)).compareTo("boolean") == 0){
                str += visit(lchild) + ".bool :=.bool " + visit(rchild) + ".bool;\n";}
            else{
                str += visit(lchild) + "." + htvars.get(visit(rchild))+" :=."+htvars.get(visit(rchild)) +" "+ visit(rchild) + "." +htvars.get(visit(rchild))+";\n";}
        }
        else if(rchild.getKind().compareTo("DotExpr") == 0){
            str += visit(lchild) + ".i32 :=.i32 " + visit(rchild) + "\n";
            builderDotExpression.setLength(0);
        }
        else if(rchild.getKind().compareTo("ArrayAccess") == 0){
            builderMethod.append(builderArrayA);
            str += visit(lchild) + ".i32 :=.i32 " + visit(rchild) + ";\n";
            builderArrayA.setLength(0);
        }
        else if (htvars.containsKey(visit(lchild)) && rchild.getKind().compareTo("NewObject") != 0) {
            if (htvars.get(visit(lchild)).compareTo("int") == 0) {
                str += visit(lchild) + ".i32 :=.i32 " + visit(rchild) + ";\n";
            } else if (htvars.get(visit(lchild)).compareTo("boolean") == 0){
                str += visit(lchild) + ".bool :=.bool " + visit(rchild) + ".bool;\n";
            }else if (rchild.getKind().compareTo("NewIntArray")==0){
                System.out.println("BOOOOOOOOOOOOOBIIIIIIIES:" + htvars);
                str += visit(lchild) + ".array.i32"+" :=.array.i32 " + visit(rchild) + ";\n";
            }
        }
        else {
            if( rchild.getKind().compareTo("NewObject") == 0){
                var type = visit(rchild.getJmmChild(0));
                str += visit(lchild) + "."+type+" :=."+type+ " " + visit(rchild) + ";\n";
                str += "invokespecial("+visit(lchild) + "."+type+",\"<init>\").V;\n";
            }
            else {str += visit(lchild) + ".i32 :=.i32 " + visit(rchild) + ";\n";}
        }
        builderMethod.append(str);
        // System.out.println("visitAssignment ::"+str);
        return "";
    }

    private String visitId(JmmNode node, Object o) {
        // System.out.println( "visitId : " + node.get("name"));
        return node.get("name");
    }

    private String visitType(JmmNode node, Object o) {
        String str = "";
        var compaare = node.get("typeName");
        if (compaare.compareTo("int") == 0) {
            str += ".i32";
            // System.out.println(".i32");
        } else if (compaare.compareTo("boolean") == 0) {
            str += ".bool";
            // System.out.println(".bool");
        }else if( compaare.compareTo("int[]") == 0){
            str += ".array.i32";
        } else if(compaare.compareTo("Void") == 0){
            str += ".V";
        }else {
            str += "."+compaare;
        }
        return str;
    }
    private String visitLen(JmmNode node, Object o) {
        return ".array.i32).i32 ";
    }
    private String visitStringArray(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        // System.out.println( "visitStringArray : "+ visit(lchild) +".array.String");
        return visit(lchild) + ".array.String";
    }

    private String visitNewIntArray(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        return  "new(array,"+visit(lchild)+")"+ ".array.i32";
    }

    private String visitArrayAccess(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        String num = visit(rchild).replaceAll(".i32","");
        String str ="";
        if(rchild.getKind().compareTo("Number")==0){
            str = "$"+num+"."+visit(lchild)+"[i.i32].i32";
        }
        else{
            if(htvars.containsKey(visit(rchild))){
                builderArrayA.append("t"+".i32 :=.i32 " + visit(rchild) + ".i32;\n");
            }
            else{ builderArrayA.append("t"+".i32 :=.i32 " + visit(rchild) + ";\n");}

            builderDotExpression.append(builderArrayA);
            str =  visit(lchild)+"[t"+".i32].i32";
        }

        return str;
    }

    private String visitArrayAssignment(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        String str = visit(lchild) + visit(rchild);
        return str;
    }

    private String visitArrayExpression(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        builderMethod.append("i.i32 :=.i32 "+visit(lchild)+";\n");
        return "["+"i.i32"+"]";
    }
    private String visitDotExpr(JmmNode node, Object o) {
        String str = "";
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        var i = 0;
        if (limports.contains(lchild.get("name"))) {
            str += "invokestatic( "+  visit(lchild)+ ", " +visit(rchild) + ").V;\n";
        } else if(rchild.getKind().compareTo("Len") == 0){
            i++;
            //System.out.println("visitDotExpr:: "+"arraylength($1."+visit(lchild)+visit(rchild));
            builderDotExpression.append("tL"+i+".i32 :=.i32 arraylength($1."+visit(lchild)+".array.i32).i32;\n");
            return "tL"+i+".i32";

        }else{
            //builderMethod.append("invokevirtual( ");
                str += "invokevirtual( "+  visit(lchild)+"."+htvars.get(visit(lchild))+ ", " +visit(rchild) + ").V;\n";
        }
        builderDotExpression.append(str);
        return str;
    }

    private String visitDotMethod(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        String str = "\"" + visit(lchild) + "\"";
        if(rchild.getNumChildren()!=0) {
            str +=  ", " + visit(rchild);
        }

        return str;
    }

    private String visitObjectMethodParam(JmmNode node, Object o) {
        //System.out.println("visitObjectMethodParam ::"+ builderMethod.toString());

        String str = "";
        for (int i = 0; i < node.getNumChildren(); i++) {
            if(node.getJmmChild(i).getKind().compareTo("Number")==0){
                str += visit(node.getJmmChild(i));
                System.out.println("visitObjectMethodParam ::"+str);
            } else if( node.getJmmChild(i).getKind().compareTo("DotExpr")==0 ){
                str = visit(node.getJmmChild(i));
            } else if(node.getJmmChild(i).getKind().compareTo("ArrayAccess")==0){
                builderDotExpression.append("t"+tempvarscont+".i32 :=.i32 " + visit(node.getJmmChild(i))+ ";\n");
                str = "t"+tempvarscont+".i32";
                tempvarscont++;
                builderArrayA.setLength(0);
            }else if(htvars.size()>i){
                str += visit(node.getJmmChild(i));
                if (htvars.get(visit(node.getJmmChild(i))).compareTo("int") == 0) {
                    str += visit(node.getJmmChild(i)) + ".i32" + ",";
                }
                else if (htvars.get(visit(node.getJmmChild(i))).compareTo("boolean") == 0) {
                    str += visit(node.getJmmChild(i)) + ".bool" + ",";
                }
                else {
                    str += visit(node.getJmmChild(i)) + "." + htvars.get(visit(node.getJmmChild(i))) +",";
                }
            } else{
                str += visit(node.getJmmChild(i)) + ".bool" + ",";
            }
        }
        str = (str == null || str.length() == 0)
                ? null
                : (str.substring(0, str.length() - 1));
        return str;
    }

    private String visitNewObject(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        var returnType =visit(lchild);

        if(builderConstruct.indexOf(returnType) < 0){
            String strConstruct = "\t.construct "+returnType+"().V {\n" +
                    "\t\tinvokespecial(this, \"<init>\").V;\n" +
                    "\t}\n";
            builderConstruct.append(strConstruct);
        }

        String str ="new("+returnType+")."+returnType;
        return str;
    }

    private String visitImportation(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        builder.append("import ");
        String str = visit(lchild);
        limports.add(str);
        builder.append(";\n");
        // System.out.println( "Importation : \n"+"import "+str+";\n");
        return "import " + str + ";\n";
    }

    private String visitImportedClasses(JmmNode node, Object o) {
        String str = "";
        for (int i = 0; i < node.getNumChildren(); i++) {
            str += visit(node.getJmmChild(i)) + ".";
        }
        str = (str == null || str.length() == 0)
                ? null
                : (str.substring(0, str.length() - 1));

        builder.append(str);
        return str;
    }

    private String visitStart(JmmNode node, Object o) {

        for (var child : node.getChildren()) {
            visit(child);
        }
        System.out.println("+++++++++\nvisitStart:\n+++++++++\n" + builder.toString()
                + "\n ---------------\nfim\n--------------\n");
        return builder.toString();
    }

    private String visitClassDeclaration(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);

        // builder.append(sybolTabel.getClassName());
        builder.append("public " + visit(lchild));

        var superClass = sybolTabel.getSuper();
        if (superClass != null) {
            builder.append(" extends ").append(superClass);
        }

        builder.append(" {\n");
        for (int i = 1; i < node.getNumChildren(); i++) {
            visit(node.getJmmChild(i));
        }
        builder.append("}\n");
        // System.out.println( "class :\n"+builder.toString()+"\n fim \n\n");
        return builder.toString();
    }

    private String visitClassName(JmmNode node, Object o) {
        String str = visit(node.getJmmChild(0));
        // builder.append(sybolTabel.getClassName())
        return str;
    }

    private String visitClassFields(JmmNode node, Object o) {
        for (var child : node.getChildren()) {
            builder.append(".field " + visit(child));
        }
        return "";
    }

    private String visitVarDeclaration(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        htvars.put(rchild.get("name"), lchild.get("typeName"));
        return visit(rchild) + visit(lchild) + ";\n";
    }

    private String visitVarDeclarations(JmmNode node, Object o) {
        String str = "";
        for (var child : node.getChildren()) {
            // builderMethod.append( visit(child) );
            str += visit(child);
        }
        return str;
    }

    private String visitMethodDeclaration(JmmNode node, Object o) {
        builderMethod.delete(0, builderMethod.length());
        builderMethod.append( "\t.method public " );
        System.out.println( "builderMethod ="+builderMethod.toString()+"\n");

        String str = visit(node.getJmmChild(0));
        builder.append(builderConstruct.toString()+"\n"+builderMethod.toString());
        return "\t.method public " + str;
    }

    private String visitMethodGeneric(JmmNode node, Object o) {

        String nodeName = visit(node.getJmmChild(1));
        String nodeTypeOllir = visit(node.getJmmChild(0));

        builderMethod.append(nodeName + "( ");
        String nodeParameters = visit(node.getJmmChild(2));
        builderMethod.append(")" + nodeTypeOllir + "{\n");

        String methodBody = "";
        for (int i = 3; i < node.getNumChildren(); i++) {
            //builderMethod.append("\t");
            methodBody += visit(node.getJmmChild(i));
        }
        builderMethod.append("}\n");
        // System.out.println( "visitMethodDeclaration :"+builderMethod.toString()+"\n");
        return nodeName + "( " + nodeParameters + "){\n" + methodBody + "\n}\n";
    }

    private String visitMethodMain(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);

        builderMethod.append("static main( ");
        String str1 = visit(lchild);
        builderMethod.append(" ).V {\n");
        String str2 = visit(rchild);
        builderMethod.append("}\n");
        // System.out.println( "static main( "+str1+" ).V {\n"+str2+"}\n" );
        return "static main( " + str1 + " ).V {\n" + str2 + "\n}\n";
    }

    private String visitMethodParameters(JmmNode node, Object o) {
        var i = 0;
        for (; i + 1 < node.getNumChildren(); i++) {
            builderMethod.append(visit(node.getJmmChild(i)) + ", ");
        }
        if (i < node.getNumChildren()) {
            builderMethod.append(visit(node.getJmmChild(i)));
        }
        return "";
    }

    private String visitMethodParameter(JmmNode node, Object o) {
        JmmNode lchild = node.getJmmChild(0);
        JmmNode rchild = node.getJmmChild(1);
        String str = "";
        str += visit(rchild);
        str += visit(lchild);
        htvars.put(visit(rchild), lchild.get("typeName"));
        return str;
    }

    private String visitMethodBody(JmmNode node, Object o) {
        String str = "";
        for (var child : node.getChildren()) {
            if(child.getKind().compareTo("DotExpr") == 0){
                visit(child);
                builderMethod.append(builderDotExpression);
                builderDotExpression.setLength(0);
            }
            else str += visit(child);
        }
        return str;
    }

    private String visitReturnStatement(JmmNode node, Object o) {

        var type = node.getJmmParent().getJmmChild(0);
        String strResolt = "ret" + visit(type) + " ";
        // String str1 = sybolTabel.getReturnType(
        // node.getJmmParent().getJmmChild(1).get("name")).toString();
        // System.out.println("parameters : "+ parameters);
        // System.out.println("node.getJmmChild(0).getKind().equals(\"Id\")"+node.getJmmChild(0).getKind().equals("Id"));
        if (node.getJmmChild(0).getKind().equals("Id")) {
            String str = visit(node.getJmmChild(0));
            strResolt += str;

            var parameters = sybolTabel.getParameters(node.getJmmParent().getJmmChild(1).get("name"));
            var localVariables = sybolTabel.getLocalVariables(node.getJmmParent().getJmmChild(1).get("name"));

            System.out.println("localVariables : "+ localVariables);
            for (var i = 0; i < localVariables.size(); i++) {
                System.out.println("localVariable "+i+" : "+ localVariables.get(i).getName()+"| str::"+str);
                if (localVariables.get(i).getName().compareTo(str) == 0) {
                    System.out.println("localVariable "+i+" type : "+ localVariables.get(i).getType().getName());
                    if (localVariables.get(i).getType().getName().compareTo("int") == 0) {
                        strResolt += ".i32";
                    } else {
                        strResolt += ".bool";
                    }
                }
            }
            // System.out.println("parameters : "+ parameters);
            for (var j = 0; j < parameters.size(); j++) {
                if (parameters.get(j).getName().compareTo(str) == 0) {
                    if (parameters.get(j).getType().getName().compareTo("int") == 0) {

                        strResolt += ".i32";
                    } else {
                        strResolt += ".bool";
                    }
                }
            }
        } else if (node.getJmmChild(0).getKind().equals("Number")) {
            String str = visit(node.getJmmChild(0));
            strResolt += str;
        } else if (node.getJmmChild(0).getKind().equals("Correct")) {
            String str = visit(node.getJmmChild(0));
            strResolt += str;
        } else if (node.getJmmChild(0).getKind().equals("Incorrect")) {
            String str = visit(node.getJmmChild(0));
            strResolt += str;
        } else {
            tempvarscont++;
            strResolt += "t" + tempvarscont + visit(type);
            // str += "t" + tempvarscont + "+.i32 " +visit(rchild);
            builderMethod.append(
                    "t" + tempvarscont + visit(type) + " :=" + visit(type) + " " + visit(node.getJmmChild(0)) + ";\n");
            // str += "\n" + "t" + tempvarscont + ".i32 :=.i32 " + visit(lchild);
        }
        builderMethod.append(strResolt + ";");
        return "";
    }

    private String visitMainParameter(JmmNode node, Object o) {
        // falta acabar
        /*
         * var i = 0;
         * for (;i+1 < node.getNumChildren() ;i++){
         * builderMethod.append(visit( node.getJmmChild(i)) + ", ");
         * }
         * if(i < node.getNumChildren()){
         * builderMethod.append(visit( node.getJmmChild(i)));
         * }
         */
        builderMethod.append("args.array.String ");
        return "args.array.String ";
    }

    private String visitIfStatement(JmmNode node, Object o) {
        if( node.getNumChildren() == 3){
            if(htvars.containsKey(visit(node.getJmmChild(0)))){
                if(htvars.get(visit(node.getJmmChild(0))).compareTo("int")==0){
                    builderMethod.append("if (" + visit(node.getJmmChild(0)) + ".i32) goto If" + ifcounter + ";\n" );
                }
                else {
                    builderMethod.append("if (" + visit(node.getJmmChild(0)) + ".bool) goto If" + ifcounter + ";\n" );
                }
            }
            else{
                builderMethod.append("if (" + visit(node.getJmmChild(0)) + ") goto If" + ifcounter + ";\n" );
            }
            builderMethod.append("goto Else" + elsecounter + ";\n");
            builderMethod.append("If"+ ifcounter + ":\n");
            ifcounter++;
            visit(node.getJmmChild(1));
            builderMethod.append("Else"+ elsecounter + ":\n");
            elsecounter++;
            visit(node.getJmmChild(2));
        }

        return "";
    }

    private String visitIfBlock(JmmNode node, Object o) {
        for (var child : node.getChildren()) {
            builderMethod.append("    ");
            if(child.getKind().compareTo("DotExpr") == 0){
                builderMethod.append(visit(child));
            }
            visit(child);
        }
        return "";
    }

    private String visitElseBlock(JmmNode node, Object o) {
        for (var child : node.getChildren()) {
            builderMethod.append("    ");
            if(child.getKind().compareTo("DotExpr") == 0){
                builderMethod.append(visit(child));
            }
            visit(child);
        }
        return "";
    }

    private String visitWhileStatement(JmmNode node, Object o) {
        builderMethod.append("Loop"+ loopcounter + ":\n");
        builderMethod.append("    if (" + visit(node.getJmmChild(0)) + ") goto End" + loopcounter + ";\n" );
            visit(node.getJmmChild(1));
        builderMethod.append("    goto Loop" + loopcounter + ";\n");
        builderMethod.append("End" + loopcounter + ":\n");
            loopcounter++;
        return "";
    }

    private String visitWhileBody(JmmNode node, Object o) {
        for (var child : node.getChildren()) {
            builderMethod.append("    ");
            if(child.getKind().compareTo("DotExpr") == 0){
                builderMethod.append(visit(child));
            }
            visit(child);
        }
        return "";
    }
}
