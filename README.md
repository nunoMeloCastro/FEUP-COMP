# Compilers Project

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Group: 3B

### Elements:
- Nome: Afonso Monteiro | número UP: up201907284 | Autoavaliação: 18 |Contribuição(%): 35%
- Nome: Nuno Melo | número UP: up202003324 | Autoavaliação: 18 |Contribuição(%): 35%
- Nome: Gabriel Martins | número UP: up201906072 | Autoavaliação: 17 |Contribuição(%): 15%
- Nome: Pedro Azevedo | número UP: up201905966 | Autoavaliação: 17 |Contribuição(%): 15%

##Global project grade (self-assesment):
    17

## Summary

This project consists in a compiler tool to parse the J-- language for java bytecode

##Semantic Analysis:

The goals of the Semantic Analysis are to verify if the program is according to the definitions of the programming language, by reporting the semantic errors with useful messages to the user.

Our compiler implements the following rules:

1. Type verification
- The boolean operation < must be between integers;
- Arithmetic operations +, -, *, / must be between integers;
- The type of the assignee must be equal to the type of the assigned (a_int = b_boolean not allowed);
- Conditional expressions (if e while) result in a boolean value.

2. Array Access Verification
- It is not possible to use array access directly to arithmetic operations;
- Ensure that an array access is done to an array (e.g. true[1] is not allowed);
- Verify if the index of an array access is an integer (e.g. a[true])

3. General Methods verification
- Verifing if the object of a certain class contains the method invoked;
- Case the method invoked is from the current class (call the method using this `this`) and there's no extends, then the code returns an error. Otherwise it's assumed that it's from the super class.
- Case the method is not from a declared class (a importeded class), it's assumed as existent and the exptected types are assumed. (i.e. a = Foo.b(), if a is an integer and Foo is a imported class, it's assumed that the method b is static, since we are accessing a method directly from the class that doesn't have arguments and return an integer);
- Verifying if the number of arguments is equals the number of parameters in the declaration.
- Verifying if the type of parameters are equals the type of the arguments.

4. Array verification
- The size of array should be and integer when creating a new one (e.g. a = new int[0];)

5. Class verification
- The class extended needs to be imported.

## Code Generation
It starts with the parsing of the .jmm file accordingly with the grammar defined. If there aren't any lexical and syntactic errors, an AST is generated for consequent symbol table creation and Semantic Analysis.
Next step is, with the AST and the Symbol table, to generate Ollir code.  
Finally, from the Ollir code we generate Jasmin accepted code.
The code generated starts with the class structure, then the methods with their parameters and return types.
The limit of the stack and local variables is calculated based on the instructions for each method.

## Pros
- Complete and detailed AST
- Robust semantic analysis

## Cons
- Could be more efficient with the use of less lookaheads in the grammar.
- Little optimizations implemented.

## Project setup

There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21 will generate code inside the subfolder ``generated``.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for Windows (``comp2022-00.bat``) and another for Linux (``comp2022-00``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. Resolve grammar conflicts, preferably with lookaheads no greater than 2
3. Include missing information in nodes (i.e. tree annotation). E.g. include the operation type in the operation node.
4. Generate a JSON from the AST

### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/ast/JmmNode.java``. The idea is for you to use this interface along with the Node class that is automatically generated by JavaCC (which can be seen in ``generated``). Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the JavaCC tutorial to see an example of how the interface can be implemented.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/parser/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/pt/up/fe/comp/CalculatorParser.java`` for an example). This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``config.properties``.

### Compilation Stages 

The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).


### config.properties

The testing framework, which uses the class TestUtils located in ``src-lib/pt/up/fe/comp``, has methods to test each of the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage). 

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp.SimpleParser``, associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.
