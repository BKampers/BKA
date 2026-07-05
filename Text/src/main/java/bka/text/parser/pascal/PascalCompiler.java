/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.*;
import run.Expression;
import uml.statechart.*;
import uml.structure.*;

/**
 * Creates an executable state machine from a pascal parse tree.
 */
public class PascalCompiler {

    public static final Type STRING = UmlTypeFactory.create("string");
    public static final Type CHAR = UmlTypeFactory.create("char");
    public static final Type REAL = UmlTypeFactory.create("real");
    public static final Type INTEGER = UmlTypeFactory.create("integer");
    public static final Type BOOLEAN = UmlTypeFactory.create("boolean");

    public PascalCompiler() {
        declaredTypes.add(BOOLEAN);
        declaredTypes.add(INTEGER);
        declaredTypes.add(REAL);
        declaredTypes.add(CHAR);
        declaredTypes.add(STRING);
    }

    public uml.structure.Class createProgramClass(Node node) {
        if (!"Program".equals(node.getSymbol())) {
            throw new IllegalArgumentException("Program keyword is missing");
        }
        String programName = identifier(node);
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        Node declarationsNode = node.getChild("Declarations");
        createTypes(declarationsNode);
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, declarationsNode);
        Operation mainOperation = builder.withMainOperation();
        methods.put(mainOperation, createCompoundStatement(mainOperation, node.getChild("CompoundStatement")));
        methodBodies.put(mainOperation, createBody(mainOperation, node.getChild("CompoundStatement")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methodBodies.get(operation));
    }

    public void createTypes(Node declarations) {
        declarations.findChild("TypeDeclaration")
            .ifPresent(typeDeclaration -> declaredTypes.add(createType(typeDeclaration)));
        declarations.findChild("Declarations")
            .ifPresent(next -> createTypes(next));
    }

    private Type createType(Node typeDeclaration) {
        return createDeclaredType(identifier(typeDeclaration), typeDeclaration.getChild("TypeDeclarationExpression"), Member.Visibility.PUBLIC);
    }

    private void addProgramVariables(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("VariableDeclaration")
            .ifPresent(declaration -> addVariables(builder, declaration));
        declarations.findChild("Declarations")
            .ifPresent(next -> addProgramVariables(builder, next));
    }

    private void addVariables(UmlClassBuilder builder, Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChild("VariableDeclarationList"), Member.Visibility.PRIVATE);
    }

    private void createAttributes(UmlClassBuilder builder, Node variableDeclarationList, Member.Visibility visibility) {
        addAttributesFromExpression(builder, variableDeclarationList.getChild("VariableDeclarationExpression"), visibility);
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> createAttributes(builder, next, visibility));
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, Node variableDeclarationExpression, Member.Visibility visibility) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), visibility);
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> {
                builder.withAttribute(name, type, visibility);
                globals.add(UmlObjectFactory.create(name, type));
            });
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("ProcedureDeclaration")
            .ifPresent(procedureDeclaration -> addMethod(builder, VOID_TYPE, procedureDeclaration));
        declarations.findChild("FunctionDeclaration")
            .ifPresent(functionDeclaration -> addMethod(builder, getType(functionDeclaration.getChild("TypeExpression")), functionDeclaration));
        declarations.findChild("Declarations")
            .ifPresent(next -> addPrivateFunctionOperations(builder, next));
    }

    private void addMethod(UmlClassBuilder builder, Type type, Node declaration) {
        String methodName = identifier(declaration);
        List<Parameter> parameters = createParameterList(declaration.getChild("ParameterDeclaration"));
        Operation operation = builder.withOperation(methodName, parameters, type, Member.Visibility.PRIVATE);
        methodParameters.put(operation, parameters);
        methodLocals.put(operation, createLocals(declaration.getChild("Declarations")));
        methodBodies.put(operation, createBody(operation, declaration.getChild("CompoundStatement")));
        methods.put(operation, createCompoundStatement(operation, declaration.getChild("CompoundStatement")));
    }


    private Collection<uml.structure.Object> createLocals(Node declarations) {
        if (declarations.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        return createVariables(declarations.getChild("VariableDeclaration").getChild("VariableDeclarationList"));
    }

    private List<Parameter> createParameterList(Node parameterDeclaration) {
        if (parameterDeclaration.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        List<Parameter> parameters = new ArrayList<>();
        Node parameterList = parameterDeclaration.getChild("ParameterList");
        do {
            parameters.add(createParameter(parameterList));
            parameterList = parameterList.findChild("ParameterList").orElse(null);
        } while (parameterList != null);
        return parameters;
    }

    private Parameter createParameter(Node parameterList) {
        Node parameterExpression = parameterList.getChild("ParameterExpression");
        return UmlParameterFactory.create(
            (parameterExpression.findChild("VAR\\b").isPresent()) ? Parameter.Direction.INOUT : Parameter.Direction.IN,
            createParameterType(parameterExpression.getChild("ParameterTypeExpression")),
            identifier(parameterExpression));
    }

    private Type createParameterType(Node parameterTypeExpression) {
        if (parameterTypeExpression.findChild("ARRAY\\b").isPresent()) {
            return new ArrayType(createType(parameterTypeExpression.getChild("TypeExpression")), 0, 0);
        }
        return getType(parameterTypeExpression.getChild("TypeExpression"));
    }

    private Type createType(Node typeDeclarationExpression, Member.Visibility visibility) {
        Node expression = head(typeDeclarationExpression);
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                getType(expression);
            case "RangeExpression" ->
                UmlTypeFactory.create(rangeString(expression));
            case "\\(" ->
                createEnumerationType(typeDeclarationExpression.getChild("IdentifierList"));
            case "ARRAY\\b" ->
                createArrayType(null, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeDeclarationExpression"));
            case "RECORD\\b" ->
                createDeclaredType(null, typeDeclarationExpression, visibility);
            default ->
                throw new IllegalStateException("UnsupportedType " + expression.getSymbol());
        };
    }

    private static Predicate<Type> typeNameEquals(Node identifier) {
        return type -> type.getName().get().equalsIgnoreCase(identifier.content());
    }

    private static Type createEnumerationType(Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static int intValue(Node node) {
        return switch (node.getSymbol()) {
            case "IntegerLiteral" ->
                Integer.parseInt(node.content());
            case "Identifier" ->
                throw new IllegalStateException("Const not supported yet"); // TODO evaluate consts fitrst
            default ->
                throw new IllegalStateException(node.content() + " is cannot be evaluated to an integer.");
        };
    }

    private Type createDeclaredType(String name, Node typeDeclarationExpression, Member.Visibility visibility) {
        if (typeDeclarationExpression.startsWith("RECORD\\b")) {
            UmlClassBuilder builder = (name == null) ? new UmlClassBuilder() : new UmlClassBuilder(name);
            addRecordFields(builder, typeDeclarationExpression.getChild("VariableDeclarationList"), visibility);
            return builder.build();
        }
        if (typeDeclarationExpression.startsWith("ARRAY\\b")) {
            return createArrayType(name, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeDeclarationExpression"));
        }
        if (typeDeclarationExpression.startsWith("TypeExpression")) {
            return getType(typeDeclarationExpression.getChild("TypeExpression"));
        }
        throw new IllegalStateException("Invalid type declaration expression: " + typeDeclarationExpression.content());
    }

    private Type getType(Node typeExpression) {
        return declaredTypes.stream()
            .filter(typeNameEquals(typeExpression))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No such type: " + typeExpression.content()));
    }

    private Type createArrayType(String identifier, Node rangeExpression, Node typeExpression) {
        if (identifier == null) {
            return new ArrayType(
                createDeclaredType(null, typeExpression, Member.Visibility.PUBLIC),
                intValue(head(rangeExpression)),
                intValue(tail(rangeExpression)));
        }
        return new ArrayType(
            identifier,
            createDeclaredType(null, typeExpression, Member.Visibility.PUBLIC),
            intValue(head(rangeExpression)),
            intValue(tail(rangeExpression)));
    }

    private void addRecordFields(UmlClassBuilder builder, Node variableDeclarationList, Member.Visibility visibility) {
        addRecordField(builder, variableDeclarationList.getChild("VariableDeclarationExpression"), visibility);
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> addRecordFields(builder, next, visibility));
    }

    private void addRecordField(UmlClassBuilder builder, Node variableDeclarationExpression, Member.Visibility visibility) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), visibility);
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type, visibility));
    }

    private Collection<uml.structure.Object> createVariables(Node variableDeclarationList) {
        Collection<uml.structure.Object> variables = new ArrayList<>();
        variables.addAll(createObjects(variableDeclarationList.getChild("VariableDeclarationExpression")));
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> variables.addAll(createVariables(next)));
        return variables;
    }

    private Collection<uml.structure.Object> createObjects(Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), Member.Visibility.PRIVATE);
        return createIdentifiers(variableDeclarationExpression.getChild("IdentifierList")).stream()
            .map(identifier -> UmlObjectFactory.create(identifier, type))
            .collect(Collectors.toList());
    }

    private static String rangeString(Node rangeExpression) {
        return head(rangeExpression).content() + " .. " + tail(rangeExpression).content();
    }

    private static List<String> createIdentifiers(Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifier(identifierList));
        identifierList.findChild("IdentifierList")
            .ifPresent(next -> identifiers.addAll(createIdentifiers(next)));
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, Node declarations) {
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(Operation operation, Node compoundStatement) {
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        createStatementSequence(operation, compoundStatement.getChild("Statements"), diagram);
        diagram.addFinalState();
        return diagram.getTransitions();
    }

    private void createStatementSequence(Operation operation, Node statements, ActivityDiagramBuilder diagram) {
        Node statementNode = statements;
        do {
            Statement statement = new Statement(statementNode.getChild("Statement"), new MethodProperties(operation));
            statement.createTransitions(diagram);
            statementNode = statementNode.findChild("Statements").orElse(null);
        } while (statementNode != null);
    }

    private run.Statement createStatement(Operation scope, Node statementNode) {
        if (!"Statement".equals(statementNode.getSymbol())) {
            throw new IllegalStateException("Not a statement: " + statementNode.content());
        }
        if (statementNode.getChildren().isEmpty()) {
            return NO_OPERATION;
        }
        return switch (head(statementNode).getSymbol()) {
            case "Assignable" ->
                new ExpressionStatement(
                    createAssignableExpression(scope, head(statementNode)),
                    createExpression(scope, tail(statementNode)));
            case "Call" ->
                new ExpressionStatement(createCallExpression(scope, head(statementNode)));
            case "Identifier" ->
                new ExpressionStatement(createIdentifierExpression(scope, head(statementNode)));
            case "CompoundStatement" ->
                createCompoundStatement(scope, head(statementNode));
            case "IF\\b" ->
                createIfStatement(scope, statementNode);
            case "FOR\\b" ->
                createForLoop(scope, statementNode);
            case "WHILE\\b" ->
                LoopStatement.whileLoop(
                    createExpression(scope, statementNode.getChild("Expression")), 
                    createStatement(scope, statementNode.getChild("Statement")));
            case "REPEAT\\b" ->
                LoopStatement.untilLoop(
                    createExpression(scope, statementNode.getChild("Expression")), 
                    createCompoundStatement(scope, statementNode));
            default ->
                throw new IllegalStateException("Unsupported statement: " + statementNode.content());
        };
    }
    
    private PascalExpression createAssignableExpression(Operation scope, Node assignableNode) {
        return createAccessExpression(
            scope, 
            createIdentifierExpression(scope, assignableNode.getChild("Identifier")),
            assignableNode.getChild("AccessExtension"));
 
    }

    private PascalExpression createExpression(Operation scope, Node expressionNode) {
        if (!"Expression".equals(expressionNode.getSymbol())) {
            throw new IllegalArgumentException(expressionNode.getSymbol());
        }
        Optional<Node> operatorNode = expressionNode.findChild("RelationalOperator");
        if (operatorNode.isPresent()) {
            return createRelationalOperationExpression(
                createComparableExpression(scope, head(expressionNode)),
                operatorNode.get(),
                createComparableExpression(scope, tail(expressionNode)));
        }
        return createComparableExpression(scope, head(expressionNode));
    }

    private PascalExpression createComparableExpression(Operation scope, Node comparableNode) {
        if (!"Comparable".equals(comparableNode.getSymbol())) {
            throw new IllegalArgumentException(comparableNode.getSymbol());
        }
        return createAdditiveExpression(
            scope,
            createTermExpression(scope, comparableNode.getChild("Term")),
            comparableNode.getChild("AdditiveOperation"));
    }

    private PascalExpression createTermExpression(Operation scope, Node termNode) {
        return createMultiplicativeExpression(
            scope,
            createFactorExpression(scope, termNode.getChild("Factor")),
            termNode.getChild("MultiplicativeOperation"));
    }

    private PascalExpression createFactorExpression(Operation scope, Node factorNode) {
        return switch (head(factorNode).getSymbol()) {
            case "Call" ->
                createAccessExpression(
                    scope,
                    createCallExpression(scope, head(factorNode)),
                    factorNode.getChild("AccessExtension"));
            case "Identifier" ->
                createAccessExpression(
                    scope,
                    createIdentifierExpression(scope, head(factorNode)),
                    factorNode.getChild("AccessExtension"));
            case "Literal" ->
                createLiteralExpression(head(factorNode));
            case "\\(" ->
                createExpression(scope, factorNode.getChild("Expression"));
            case "UnaryOperator" ->
                createUnaryExpression(
                    head(factorNode),
                    createFactorExpression(scope, factorNode.getChild("Factor")));
            default ->
                throw new IllegalStateException("Unsupported factor: " + head(factorNode).getSymbol());
        };
    }

    private PascalExpression createAccessExpression(Operation scope, PascalExpression referenceExpression, Node targetNode) {
        if (targetNode.getChildren().isEmpty()) {
            return referenceExpression;
        }
        return switch (head(targetNode).getSymbol()) {
            case "\\." ->
                memberExpression(scope, referenceExpression, targetNode.getChild("Identifier"), targetNode.getChild("AccessExtension"));
            case "\\[" ->
                indexedExpression(scope, referenceExpression, targetNode.getChild("Expression"), targetNode.getChild("AccessExtension"));
            default ->
                throw new IllegalStateException("Unsupported access: " + head(targetNode).content());
        };
    }

    private PascalExpression memberExpression(Operation scope, PascalExpression receiver, Node identifierNode, Node accessExtensionNode) {
        PascalExpression expression = new MemberAccessExpression(receiver, identifierNode.content());
        if (accessExtensionNode.getChildren().isEmpty()) {
            return expression;
        }
        return createAccessExpression(scope, expression, accessExtensionNode);
    }

    private PascalExpression indexedExpression(Operation scope, PascalExpression base, Node indexNode, Node accessExtensionNode) {
        PascalExpression expression = new IndexAccessExpression(base, createExpression(scope, indexNode));
        if (accessExtensionNode.getChildren().isEmpty()) {
            return expression;
        }
        return createAccessExpression(scope, expression, accessExtensionNode);
    }

    private PascalExpression createAdditiveExpression(Operation scope, PascalExpression leftExpression, Node additiveOperationNode) {
        if (additiveOperationNode.getChildren().isEmpty()) {
            return leftExpression;
        }
        PascalExpression operationExpression = new OperatorExpression(leftExpression, additiveOperationNode.getChild("AdditiveOperator"), createTermExpression(scope, additiveOperationNode.getChild("Term")));
        return createAdditiveExpression(scope, operationExpression, additiveOperationNode.getChild("AdditiveOperation"));
    }

    private PascalExpression createMultiplicativeExpression(Operation scope, PascalExpression leftExpression, Node multiplicativeOperationNode) {
        if (multiplicativeOperationNode.getChildren().isEmpty()) {
            return leftExpression;
        }
        PascalExpression operationExpression = new OperatorExpression(leftExpression, multiplicativeOperationNode.getChild("MultiplicativeOperator"), createFactorExpression(scope, multiplicativeOperationNode.getChild("Factor")));
        return createMultiplicativeExpression(scope, operationExpression, multiplicativeOperationNode.getChild("MultiplicativeOperation"));
    }

    private PascalExpression createUnaryExpression(Node operatorNode, PascalExpression expression) {
        return new PascalExpression() {
            @Override
            public Optional<Type> getType() {
                return expression.getType();
            }

            @Override
            public java.lang.Object evaluate() {
                return switch (operatorNode.content().toUpperCase()) {
                    case "-" ->
                        minus((Number) expression.evaluate());
                    case "NOT" ->
                        !(Boolean) expression.evaluate();
                    default ->
                        throw new IllegalStateException("Unsupported unary operator: " + operatorNode.content());
                };
            }

            private Number minus(Number value) throws IllegalStateException {
                if (value instanceof Integer) {
                    return -value.intValue();
                }
                if (value instanceof Float) {
                    return -value.floatValue();
                }
                throw new IllegalStateException();
            }

            @Override
            public String toString() {
                return String.format("%s %s", operatorNode.content(), expression);
            }
        };
    }

    private PascalExpression createRelationalOperationExpression(PascalExpression leftExpression, Node operatorNode, PascalExpression rightExpression) {
        return new OperatorExpression(leftExpression, operatorNode, rightExpression);
    }

    private PascalExpression createCallExpression(Operation scope, Node callNode) {
        Operation operation = getOperation(callNode.getChild("Identifier"));
        return pascalExpression(new CallExpression(operation, createArgumentMap(scope, operation.getParameters(), callNode.getChild("ArgumentList"))));
    }

    private Map<Parameter, Expression> createArgumentMap(Operation scope, List<Parameter> parameters, Node argumentList) {
        Map<Parameter, Expression> arguments = new HashMap<>();
        populateArgumentMap(scope, parameters, argumentList, arguments);
        return arguments;
    }

    private void populateArgumentMap(Operation scope, List<Parameter> parameters, Node argumentList, Map<Parameter, Expression> arguments) {
        if (arguments.size() >= parameters.size()) {
            throw new IllegalStateException("Too many arguments in list");
        }
        Parameter parameter = parameters.get(arguments.size());
        PascalExpression argument = createExpression(scope, argumentList.getChild("Expression"));
        requireTypeMatch(parameter.getType().get(), argument.getType().get());
        arguments.put(parameters.get(arguments.size()), createExpression(scope, argumentList.getChild("Expression")));
        Optional<Node> remainder = argumentList.findChild("ArgumentList");
        remainder.ifPresent(tail -> populateArgumentMap(scope, parameters, tail, arguments));
    }

    private PascalExpression createIdentifierExpression(Operation scope, Node identifierNode) {
        String identifier = identifierNode.content();
        if (scope.getName().isPresent() && identifier.equalsIgnoreCase(scope.getName().get())) {
            return new PascalExpression() {
                @Override
                public Optional<Type> getType() {
                    return scope.getType();
                }

                @Override
                public java.lang.Object evaluate() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String toString() {
                    return "@ReturnValue (" + identifier + ")";
                }
            };
        }
        Optional<Parameter> parameter = scope.getParameters().stream().filter(p -> identifier.equalsIgnoreCase(p.getName().get())).findAny();
        if (parameter.isPresent()) {
            return new PascalExpression() {
                @Override
                public Optional<Type> getType() {
                    return parameter.get().getType();
                }
                @Override
                public java.lang.Object evaluate() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String toString() {
                    return identifier;
                }
            };
        }
        Optional<uml.structure.Object> variable = findVariable(scope, identifier);
        if (variable.isPresent()) {
            return new ProgramVariableExpression(identifier, variable.get().getType().get());
        }
        return pascalExpression(new CallExpression(getOperation(identifierNode)));
    }
    
    private Optional<uml.structure.Object> findVariable(Operation scope, String identifier) {
        Optional<uml.structure.Object> variable = Optional.empty();
        if (methodLocals.containsKey(scope)) {
            variable = methodLocals.get(scope).stream()
                .filter(local -> identifier.equalsIgnoreCase(local.getName().get()))
                .findAny();
        }
        if (variable.isPresent()) {
            return variable;
        }
        return globals.stream()
            .filter(global -> identifier.equalsIgnoreCase(global.getName().get()))
            .findAny();
    }
    
    private CompoundStatement createCompoundStatement(Operation scope, Node compoundStatementNode) {
        return new CompoundStatement(createStatementSequence(scope, compoundStatementNode.getChild("Statements")));
    }

    private List<run.Statement> createStatementSequence(Operation scope, Node statementsNode) {
        List<run.Statement> statements = new ArrayList<>();
        Node next = statementsNode;
        do {
            Node statementNode = next.getChild("Statement");
            statements.add(createStatement(scope, statementNode));
            next = next.findChild("Statements").orElse(null);
        } while (next != null);
        return statements;
    }
    
    private BranchStatement createIfStatement(Operation scope, Node statementNode) {
        Optional<Node> elseClause = statementNode.getChild("ElseClause").findChild("Statement");
        if (elseClause.isPresent()) {
            return BranchStatement.ifStatement(
                createExpression(scope, statementNode.getChild("Expression")), 
                createStatement(scope, statementNode.getChild("Statement")), 
                createStatement(scope, elseClause.get()));
        }
        return BranchStatement.ifStatement(
            createExpression(scope, statementNode.getChild("Expression")), 
            createStatement(scope, statementNode.getChild("Statement")));
    }
    
    private run.Statement createForLoop(Operation scope, Node statementNode) {
        ExpressionStatement initialization = createExpressionStatement(
            createIdentifierExpression(scope, statementNode.getChild("Identifier")),
            createExpression(scope, statementNode.getChildren().get(3)));
        PascalExpression condition = new PascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(BOOLEAN);
            }
            @Override
            public java.lang.Object evaluate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return statementNode.getChild("Identifier").content() + " <= " + statementNode.getChildren().get(5).content();
            }
        };
        ExpressionStatement incrementAction = createExpressionStatement(
            createIdentifierExpression(
                scope,
                statementNode.getChild("Identifier")),
            new PascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(INTEGER);
            }

            @Override
            public java.lang.Object evaluate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return "@Inc(" + statementNode.getChild("Identifier").content() + ")";
            }
        }
        );
        LoopStatement loop = LoopStatement.forLoop(
            condition, 
            createStatement(scope, statementNode.getChild("Statement")), 
            incrementAction);
        return new CompoundStatement(List.of(initialization, loop));
    }

    private ExpressionStatement createExpressionStatement(PascalExpression assignable, PascalExpression expression) {
        requireTypeMatch(assignable.getType().get(), expression.getType().get());
        return new ExpressionStatement(assignable, expression);
    }

    private static void requireTypeMatch(Type assignable, Type expression) throws IllegalArgumentException {
        if (!compatible(assignable, expression)) {
            throw new IllegalArgumentException("Type mismatch " + assignable + " != " + expression);
        }
    }

    private static boolean compatible(Type left, Type right) {
        if (left.equals(right)) {
            return true;
        }
        if (REAL.equals(left) && INTEGER.equals(right)) {
            return true;
        }
        if (left instanceof ArrayType leftArray && right instanceof ArrayType rightArray) {
            return leftArray.getElementType().equals(rightArray.getElementType())
                && leftArray.getLowerBound() == rightArray.getLowerBound()
                && leftArray.getUpperBound() == rightArray.getUpperBound();
        }
        return false;
    }

    private PascalExpression createLiteralExpression(Node literalNode) {
        return switch (head(literalNode).getSymbol()) {
            case "RealLiteral" ->
                new PascalExpression() {
                    @Override
                    public Optional<Type> getType() {
                        return Optional.of(REAL);
                    }

                    @Override
                    public java.lang.Object evaluate() {
                        try {
                            return Float.valueOf(literalNode.content());
                        }
                        catch (NumberFormatException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                    @Override
                    public String toString() {
                        return literalNode.content();
                    }
                };
            case "IntegerLiteral" ->
                new PascalExpression() {
                    @Override
                    public Optional<Type> getType() {
                        return Optional.of(INTEGER);
                    }
                    @Override
                    public java.lang.Object evaluate() {
                        try {
                            return Integer.valueOf(literalNode.content());
                        }
                        catch (NumberFormatException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }

                    @Override
                    public String toString() {
                        return literalNode.content();
                    }
                };
            case "\\'" ->
                new PascalExpression() {
                    @Override
                    public Optional<Type> getType() {
                        return Optional.of(STRING);
                    }
                    @Override
                    public java.lang.Object evaluate() {
                        String content = literalNode.content();
                        return content.substring(1, content.length() - 1);
                    }

                    @Override
                    public String toString() {
                        return literalNode.content();
                    }
                };
            case "FALSE\\b", "TRUE\\b" ->
                new PascalExpression() {
                    @Override
                    public Optional<Type> getType() {
                        return Optional.of(BOOLEAN);
                    }
                    @Override
                    public java.lang.Object evaluate() {
                        return Boolean.valueOf(literalNode.content());
                    }

                    @Override
                    public String toString() {
                        return literalNode.content();
                    }
                };
            default ->
                throw new IllegalStateException("Unsupported literal: " + head(literalNode).getSymbol());

        };
    }

    private Operation getOperation(Node identifierNode) {
        String identifier = identifierNode.content();
        return methodBodies.keySet().stream()
            .filter(o -> o.getName().isPresent() && identifier.equalsIgnoreCase(o.getName().get()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No such operation: " + identifier));
    }

    private static Node head(Node node) {
        return node.getChildren().getFirst();
    }

    private static Node tail(Node node) {
        return node.getChildren().getLast();
    }

    private Type typeOf(Operation operation, String identifier, List<Node> accessExtensions) {
        Type type = null;
        if (operation.getName().isPresent() && identifier.equalsIgnoreCase(operation.getName().get())) {
            type = operation.getType().get();
        }
        if (type == null && methodParameters.containsKey(operation)) {
            Optional<Parameter> parameter = methodParameters.get(operation).stream()
                .filter(p -> identifier.equalsIgnoreCase(p.getName().get()))
                .findAny();
            if (parameter.isPresent()) {
                type = parameter.get().getType().get();
            }
        }
        if (type == null && methodLocals.containsKey(operation)) {
            Optional<uml.structure.Object> local = methodLocals.get(operation).stream()
                .filter(object -> object.getName().get().equalsIgnoreCase(identifier))
                .findAny();
            if (local.isPresent()) {
                type = local.get().getType().get();
            }
        }
        if (type == null) {
            type = globals.stream()
                .filter(global -> global.getName().get().equalsIgnoreCase(identifier))
                .map(global -> global.getType().get())
                .findAny().get();
        }
        for (Node extension : accessExtensions) {
            type = switch (extension.getSymbol()) {
                case "Identifier" ->
                    ((uml.structure.Class) type).getAttributes().stream().filter(attribute -> attribute.getName().get().equalsIgnoreCase(extension.content())).findAny().get().getType().get();
                case "Expression" ->
                    ((ArrayType) type).getElementType();
                default ->
                    throw new IllegalStateException("Invalid access extension");
            };
        }
        return type;
    }

    private static List<Node> getAccessExtensions(Node expression) {
        List<Node> extensions = new ArrayList<>();
        Optional<Node> next = expression.findChild("AccessExtension");
        while (next.isPresent()) {
            Node accessExtension = next.get();
            if (accessExtension.getChildren().isEmpty()) {
                next = Optional.empty();
            }
            else {
                if (accessExtension.startsWith("\\.")) {
                    extensions.add(accessExtension.getChild("Identifier"));
                }
                else if (accessExtension.startsWith("\\[")) {
                    extensions.add(accessExtension.getChild("Expression"));
                }
                else {
                    throw new IllegalStateException("Unsupported access extension");
                }
                next = accessExtension.findChild("AccessExtension");
            }
        }
        return extensions;
    }

    private static String identifier(Node node) {
        return node.getChild("Identifier").content().toLowerCase();
    }

    //TODO Move this to a separate class. This does not belong to the compiler's responsibilities
    public void execute(uml.structure.Class programClass) {
        Operation mainOperation = programClass.getOperations().stream()
            .filter(operation -> operation.getStereotypes().stream().anyMatch(stereotype -> "Main".equals(stereotype.getName())))
            .findAny().get();
        Map<Attribute, Expression> attributeValues = programClass.getAttributes().stream()
            .collect(Collectors.toMap(Function.identity(), attribute -> uninitializedExpression(attribute.getType().get())));
        programObject = new MutableObject(
            programClass.getName().orElse(null),
            programClass,
            attributeValues);
        methods.get(mainOperation).getStatements().forEach(this::execute);
        programObject.getAttributeValues().forEach((attribute, expression) -> {
            System.out.println(attribute.getName().get() + " = " + ((PascalExpression) expression).evaluate());
        });
    }

//    public MutableObject getProgramObject() {
//        return programObject;
//    }

    private void execute(run.Statement statement) {
        if (statement.equals(NO_OPERATION)) {
            return;
        }
        switch (statement) {
            case CompoundStatement compound ->
                compound.getStatements().forEach(this::execute);
            case ExpressionStatement expressionStatement -> {
                if (expressionStatement.getAssignable().isPresent()) {
                    assign(expressionStatement.getAssignable().get(), expressionStatement.getExpression());
                }
                else {
                    evaluate(expressionStatement.getExpression());
                }
            }
            default ->
                throw new IllegalStateException("Unsupported statement: " + statement.getClass().getName());
        }
    }

    private void assign(Expression assignable, Expression valueExpression) {
        if (!(assignable instanceof ProgramVariableExpression variable)) {
            throw new IllegalStateException("Unsupported assignable: " + assignable);
        }
        java.lang.Object value = evaluate(valueExpression);
        programObject.set(variable.getAttribute(), literalExpression(variable.getType().get(), value));
    }

    private Attribute findProgramAttribute(String name) {
        return programObject.getAttributes().stream()
            .filter(attribute -> attribute.getName().isPresent() && name.equalsIgnoreCase(attribute.getName().get()))
            .findAny()
            .orElseThrow(() -> new NoSuchElementException("No such program variable: " + name));
    }

    private java.lang.Object evaluate(Expression expression) {
        if (expression instanceof PascalExpression pascalExpression) {
            return pascalExpression.evaluate();
        }
        throw new IllegalStateException("Unsupported expression type" + expression.getClass());
    }

    public void dumpMethods() {
        methods.forEach((operation, statement) -> {
            System.out.println(operation.getName());
            System.out.println(statement);
            System.out.println("-".repeat(40));
        });
    }


    private PascalExpression literalExpression(Type type, java.lang.Object value) {
        return new PascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(type);
            }

            @Override
            public java.lang.Object evaluate() {
                return value;
            }
        };
    }

    private PascalExpression uninitializedExpression(Type type) {
        return new PascalExpression() {
            @Override
            public Optional<Type> getType() {
                return Optional.of(type);
            }

            @Override
            public java.lang.Object evaluate() {
                return StateMachine.UNINITIALIZED;
            }
        };
    }

    private PascalExpression pascalExpression(Expression expression) {
        return new PascalExpression() {
            @Override
            public java.lang.Object evaluate() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public Optional<Type> getType() {
                return expression.getType();
            }

        };
    }


    private abstract class PascalExpression extends Expression {

        public abstract java.lang.Object evaluate();

    }

    private class ProgramVariableExpression extends PascalExpression {

        ProgramVariableExpression(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        Attribute getAttribute() {
            return findProgramAttribute(name);
        }

        @Override
        public Optional<Type> getType() {
            return Optional.of(type);
        }

        @Override
        public java.lang.Object evaluate() {
            return ((PascalExpression) programObject.get(getAttribute())).evaluate();
        }

        @Override
        public String toString() {
            return name;
        }

        private final String name;
        private final Type type;

    }

    private class MemberAccessExpression extends PascalExpression {

        public MemberAccessExpression(PascalExpression receiver, String member) {
            this.receiver = Objects.requireNonNull(receiver);
            this.member = Objects.requireNonNull(member);
        }

        @Override
        public Optional<Type> getType() {
            uml.structure.Class targetClass = (uml.structure.Class) receiver.getType().get();
            return targetClass.getAttributes()
                .stream().filter(attribute -> attribute.getName().isPresent() && member.equalsIgnoreCase(attribute.getName().get()))
                .findAny().get().getType();
        }

        @Override
        public java.lang.Object evaluate() {
            uml.structure.Object value = (uml.structure.Object) receiver.evaluate();
            return ((PascalExpression) value.getAttributeValues().get(member)).evaluate();
        }

        @Override
        public String toString() {
            return receiver + "." + member;
        }

        private final PascalExpression receiver;
        private final String member;

    }


    private class IndexAccessExpression extends PascalExpression {

        public IndexAccessExpression(PascalExpression base, PascalExpression index) {
            this.base = Objects.requireNonNull(base);
            this.index = Objects.requireNonNull(index);
        }

        @Override
        public Optional<Type> getType() {
            return Optional.of(arrayType().getElementType());
        }

        private ArrayType arrayType() {
            return (ArrayType) base.getType().get();
        }

        @Override
        public java.lang.Object evaluate() {
            java.lang.Object[] value = (java.lang.Object[]) base.evaluate();
            return value[(Integer) index.evaluate()];
        }

        @Override
        public String toString() {
            return base + "[" + index + "]";
        }

        private final PascalExpression base;
        private final PascalExpression index;

    }


    private final class OperatorExpression extends PascalExpression {

        public OperatorExpression(PascalExpression left, Node operator, PascalExpression right) {
            if (left.getType().isEmpty() || right.getType().isEmpty()) {
                throw new IllegalArgumentException();
            }
            this.left = Objects.requireNonNull(left);
            this.operator = Objects.requireNonNull(operator);
            this.right = Objects.requireNonNull(right);
        }

        @Override
        public Optional<Type> getType() {
            return switch (head(operator).getSymbol()) {
                case "\\=", "\\<", "\\>", "\\<\\=", "\\>\\=", "\\<\\>" ->
                    Optional.of(BOOLEAN);
                case "AND\\b", "OR\\b", "XOR\\b" ->
                    left.getType();
                case "\\*", "\\+", "\\-" ->
                    (INTEGER.equals(left.getType().get()) && INTEGER.equals(right.getType().get())) ? Optional.of(INTEGER) : Optional.of(REAL);
                case "\\/" ->
                    Optional.of(REAL);
                case "DIV\\b", "MOD\\b" ->
                    Optional.of(INTEGER);
                default ->
                    throw new IllegalStateException(String.format("Cannot determine type of %s %s %s", left.getType().get(), operator.content(), right.getType().get()));
            };
        }

        @Override
        public java.lang.Object evaluate() {
            return switch (head(operator).getSymbol()) {
                case "\\=" ->
                    left.evaluate().equals(right.evaluate());
                case "\\<" ->
                    ((Number) left.evaluate()).doubleValue() < ((Number) right.evaluate()).doubleValue();
                case "\\>" ->
                    ((Number) left.evaluate()).doubleValue() > ((Number) right.evaluate()).doubleValue();
                case "\\<\\=" ->
                    ((Number) left.evaluate()).doubleValue() <= ((Number) right.evaluate()).doubleValue();
                case "\\>\\=" ->
                    ((Number) left.evaluate()).doubleValue() >= ((Number) right.evaluate()).doubleValue();
                case "\\<\\>" ->
                    !left.evaluate().equals(right.evaluate());
                case "AND\\b" ->
                    and(left.evaluate(), right.evaluate());
                case "OR\\b" ->
                    or(left.evaluate(), right.evaluate());
                case "XOR\\b" ->
                    xor(left.evaluate(), right.evaluate());
                case "\\*" ->
                    product(left.evaluate(), right.evaluate());
                case "\\/" ->
                    ((Number) left.evaluate()).floatValue() / ((Number) (right.evaluate())).floatValue();
                case "DIV\\b" ->
                    (Integer) left.evaluate() / (Integer) right.evaluate();
                case "MOD\\b" ->
                    (Integer) left.evaluate() % (Integer) right.evaluate();
                case "\\+" ->
                    sum(left.evaluate(), right.evaluate());
                case "\\-" ->
                    difference(left.evaluate(), right.evaluate());
                default ->
                    throw new IllegalStateException();
            };
        }

        private java.lang.Object and(java.lang.Object left, java.lang.Object right) {
            if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
                return leftBoolean && rightBoolean;
            }
            if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
                return leftInteger & rightInteger;
            }
            throw new IllegalStateException();
        }

        private java.lang.Object or(java.lang.Object left, java.lang.Object right) {
            if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
                return leftBoolean || rightBoolean;
            }
            if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
                return leftInteger | rightInteger;
            }
            throw new IllegalStateException();
        }

        private java.lang.Object xor(java.lang.Object left, java.lang.Object right) {
            if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
                return leftBoolean ^ rightBoolean;
            }
            if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
                return leftInteger ^ rightInteger;
            }
            throw new IllegalStateException();
        }

        private java.lang.Object product(java.lang.Object left, java.lang.Object right) {
            if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
                return leftInteger * rightInteger;
            }
            if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
                return leftNumber.floatValue() * rightNumber.floatValue();
            }
            throw new IllegalStateException();
        }

        private java.lang.Object sum(java.lang.Object left, java.lang.Object right) {
            if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
                return leftInteger + rightInteger;
            }
            if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
                return leftNumber.floatValue() - rightNumber.floatValue();
            }
            throw new IllegalStateException();
        }

        private java.lang.Object difference(java.lang.Object left, java.lang.Object right) {
            if (left instanceof Integer leftInteger && right instanceof Integer rightInteger) {
                return leftInteger - rightInteger;
            }
            if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
                return leftNumber.floatValue() - rightNumber.floatValue();
            }
            throw new IllegalStateException();
        }

        @Override
        public String toString() {
            return "{" + left + " " + operator.content() + " " + right + "}";
        }

        private final PascalExpression left;
        private final Node operator;
        private final PascalExpression right;

    }

    public final class MethodProperties {

        private MethodProperties(Operation scope) {
            this.scope = scope;
        }

        public Type determineTypeOf(Node expression) {
            return typeOf(scope, identifier(expression), getAccessExtensions(expression));
        }

        public Type determineTypeOf(String identifier) {
            return typeOf(scope, identifier, Collections.emptyList());
        }

        public Collection<Transition<Event, GuardCondition, Action>> getBody(String name) {
            return methodBodies.get(getOperation(name));
        }

        public List<Parameter> getParameters(String name) {
            return Collections.unmodifiableList(methodParameters.get(getOperation(name)));
        }

        public Type getType(String name) {
            return getOperation(name).getType().get();
        }

        public Collection<uml.structure.Object> getLocals(String name) {
            return Collections.unmodifiableCollection(methodLocals.get(getOperation(name)));
        }

        public boolean isVoid(Type type) {
            return VOID_TYPE.equals(type);
        }

        public boolean isProcedure(String name) {
            return VOID_TYPE.equals(getOperation(name).getType().get());
        }

        private Operation getOperation(String name) {
            return methodBodies.keySet().stream()
                .filter(operation -> operation.getName().isPresent())
                .filter(operation -> name.equalsIgnoreCase(operation.getName().get()))
                .findAny().orElse(null);
        }

        private final Operation scope;

    }

    private final Collection<uml.structure.Object> globals = new ArrayList<>();
    private final Map<Operation, Collection<Transition<Event, GuardCondition, Action>>> methodBodies = new HashMap<>();
    private final Map<Operation, List<Parameter>> methodParameters = new HashMap<>();
    private final Map<Operation, Collection<uml.structure.Object>> methodLocals = new HashMap<>();
    private final Collection<Type> declaredTypes = new ArrayList<>();
    private final Map<Operation, CompoundStatement> methods = new HashMap<>();
    private MutableObject programObject;

    private static final run.Statement NO_OPERATION = new run.Statement(){
        @Override
        public String toString() {
            return "@No operation";   
        }
    };
    
    private static final Type VOID_TYPE = UmlTypeFactory.create("void");

}
