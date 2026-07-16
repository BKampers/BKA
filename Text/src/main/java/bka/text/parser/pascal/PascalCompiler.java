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
import run.pascal.*;
import uml.statechart.*;
import uml.structure.*;

/**
 * Creates an executable state machine from a pascal parse tree.
 */
public final class PascalCompiler {

    public PascalCompiler() {
        declaredTypes.add(PascalTypes.BOOLEAN);
        declaredTypes.add(PascalTypes.INTEGER);
        declaredTypes.add(PascalTypes.REAL);
        declaredTypes.add(PascalTypes.CHAR);
        declaredTypes.add(PascalTypes.STRING);
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

    public Map<Operation, CompoundStatement> getMethods() {
        return Collections.unmodifiableMap(methods);
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
        Collection<uml.structure.Object> locals = createLocals(declaration.getChild("Declarations"));
        Operation operation = builder.withOperation(methodName, parameters, type, Member.Visibility.PRIVATE);
        methodLocals.put(operation, locals);
        methodBodies.put(operation, createBody(operation, declaration.getChild("CompoundStatement")));
        methods.put(operation, createCompoundStatement(operation, declaration.getChild("CompoundStatement"), locals));
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
    
    private AbstractPascalExpression createAssignableExpression(Operation scope, Node assignableNode) {
        return createAccessExpression(
            scope, 
            createIdentifierExpression(scope, assignableNode.getChild("Identifier")),
            assignableNode.getChild("AccessExtension"));
 
    }

    private AbstractPascalExpression createExpression(Operation scope, Node expressionNode) {
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

    private AbstractPascalExpression createComparableExpression(Operation scope, Node comparableNode) {
        if (!"Comparable".equals(comparableNode.getSymbol())) {
            throw new IllegalArgumentException(comparableNode.getSymbol());
        }
        return createAdditiveExpression(
            scope,
            createTermExpression(scope, comparableNode.getChild("Term")),
            comparableNode.getChild("AdditiveOperation"));
    }

    private AbstractPascalExpression createTermExpression(Operation scope, Node termNode) {
        return createMultiplicativeExpression(
            scope,
            createFactorExpression(scope, termNode.getChild("Factor")),
            termNode.getChild("MultiplicativeOperation"));
    }

    private AbstractPascalExpression createFactorExpression(Operation scope, Node factorNode) {
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
                    UnaryOperatorExpression.UnaryOperator.lookup(head(head(factorNode)).getSymbol()),
                    createFactorExpression(scope, factorNode.getChild("Factor")));
            default ->
                throw new IllegalStateException("Unsupported factor: " + head(factorNode).getSymbol());
        };
    }

    private AbstractPascalExpression createAccessExpression(Operation scope, AbstractPascalExpression referenceExpression, Node targetNode) {
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

    private AbstractPascalExpression memberExpression(Operation scope, AbstractPascalExpression receiver, Node identifierNode, Node accessExtensionNode) {
        AbstractPascalExpression expression = new MemberAccessExpression(receiver, identifierNode.content());
        if (accessExtensionNode.getChildren().isEmpty()) {
            return expression;
        }
        return createAccessExpression(scope, expression, accessExtensionNode);
    }

    private AbstractPascalExpression indexedExpression(Operation scope, AbstractPascalExpression base, Node indexNode, Node accessExtensionNode) {
        AbstractPascalExpression expression = new IndexAccessExpression(base, createExpression(scope, indexNode));
        if (accessExtensionNode.getChildren().isEmpty()) {
            return expression;
        }
        return createAccessExpression(scope, expression, accessExtensionNode);
    }

    private AbstractPascalExpression createAdditiveExpression(Operation scope, AbstractPascalExpression leftExpression, Node additiveOperationNode) {
        if (additiveOperationNode.getChildren().isEmpty()) {
            return leftExpression;
        }
        return createAdditiveExpression(
            scope, 
            new OperatorExpression(
                leftExpression, 
                Operator.lookup(head(additiveOperationNode.getChild("AdditiveOperator")).getSymbol()), 
                createTermExpression(scope, additiveOperationNode.getChild("Term"))), 
            additiveOperationNode.getChild("AdditiveOperation"));
    }

    private AbstractPascalExpression createMultiplicativeExpression(Operation scope, AbstractPascalExpression leftExpression, Node multiplicativeOperationNode) {
        if (multiplicativeOperationNode.getChildren().isEmpty()) {
            return leftExpression;
        }
        return createMultiplicativeExpression(
            scope, 
            new OperatorExpression(
                leftExpression, 
                Operator.lookup(head(multiplicativeOperationNode.getChild("MultiplicativeOperator")).getSymbol()), 
                createFactorExpression(scope, multiplicativeOperationNode.getChild("Factor"))), 
            multiplicativeOperationNode.getChild("MultiplicativeOperation"));
    }

    private AbstractPascalExpression createUnaryExpression(UnaryOperatorExpression.UnaryOperator operator, AbstractPascalExpression expression) {
        return new UnaryOperatorExpression(operator, expression);
    }

    private AbstractPascalExpression createRelationalOperationExpression(AbstractPascalExpression leftExpression, Node operatorNode, AbstractPascalExpression rightExpression) {
        return new OperatorExpression(leftExpression, Operator.lookup(head(operatorNode).getSymbol()), rightExpression);
    }

    private AbstractPascalExpression createCallExpression(Operation scope, Node callNode) {
        Operation operation = getOperation(callNode.getChild("Identifier"));
        return new MethodCallExpression(operation, createArgumentMap(scope, operation.getParameters(), callNode.getChild("ArgumentList")));
    }

    private Map<Parameter, run.Expression> createArgumentMap(Operation scope, List<Parameter> parameters, Node argumentList) {
        Map<Parameter, run.Expression> arguments = new HashMap<>();
        populateArgumentMap(scope, parameters, argumentList, arguments);
        return arguments;
    }

    private void populateArgumentMap(Operation scope, List<Parameter> parameters, Node argumentList, Map<Parameter, run.Expression> arguments) {
        if (arguments.size() >= parameters.size()) {
            throw new IllegalStateException("Too many arguments in list");
        }
        Parameter parameter = parameters.get(arguments.size());
        AbstractPascalExpression argument = createExpression(scope, argumentList.getChild("Expression"));
        requireTypeMatch(parameter.getType().get(), argument.getType().get());
        arguments.put(parameters.get(arguments.size()), createExpression(scope, argumentList.getChild("Expression")));
        Optional<Node> remainder = argumentList.findChild("ArgumentList");
        remainder.ifPresent(tail -> populateArgumentMap(scope, parameters, tail, arguments));
    }

    private AbstractPascalExpression createIdentifierExpression(Operation scope, Node identifierNode) {
        String identifier = identifierNode.content();
        if (scope.getName().isPresent() && identifier.equalsIgnoreCase(scope.getName().get()) && scope.getType().isPresent() && !isProcedure(scope)) {
            return new ScopeVariableExpression(identifier, scope.getType().get());
        }
        Optional<Parameter> parameter = scope.getParameters().stream().filter(p -> identifier.equalsIgnoreCase(p.getName().get())).findAny();
        if (parameter.isPresent()) {
            return new ScopeVariableExpression(identifier, parameter.get().getType().get());
        }
        Optional<uml.structure.Object> local = findLocal(scope, identifier);
        if (local.isPresent()) {
            return new ScopeVariableExpression(identifier, local.get().getType().get());
        }
        Optional<uml.structure.Object> variable = globals.stream()
            .filter(global -> identifier.equalsIgnoreCase(global.getName().get()))
            .findAny();
        if (variable.isPresent()) {
            return new ScopeVariableExpression(identifier, variable.get().getType().get());
        }
        return new MethodCallExpression(getOperation(identifierNode), Collections.emptyMap());
    }
    
    private boolean isProcedure(Operation operation) {
        return operation.getType().map(VOID_TYPE::equals).orElse(true);
    }
    
    private Optional<uml.structure.Object> findLocal(Operation scope, String identifier) {
        return methodLocals.getOrDefault(scope, Collections.emptyList()).stream()
            .filter(local -> identifier.equalsIgnoreCase(local.getName().get()))
            .findAny();
    }
    
    private CompoundStatement createCompoundStatement(Operation scope, Node compoundStatementNode) {
        return createCompoundStatement(scope, compoundStatementNode, Collections.emptyList());
    }

    private CompoundStatement createCompoundStatement(Operation scope, Node compoundStatementNode, Collection<uml.structure.Object> locals) {
        return new CompoundStatement(createStatementSequence(scope, compoundStatementNode.getChild("Statements")), locals);
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
        AbstractPascalExpression loopVariable = createIdentifierExpression(scope, statementNode.getChild("Identifier"));
        AbstractPascalExpression condition = new OperatorExpression(
            loopVariable,
            Operator.LESS_EQUAL,
            createExpression(scope, statementNode.getChildren().get(5)));
        ExpressionStatement incrementAction = createExpressionStatement(
            loopVariable,
            new OperatorExpression(loopVariable, Operator.ADDITION, PascalValues.intLiteral(1)));
        LoopStatement loop = LoopStatement.forLoop(
            condition, 
            createStatement(scope, statementNode.getChild("Statement")), 
            incrementAction);
        return new CompoundStatement(List.of(initialization, loop));
    }

    private ExpressionStatement createExpressionStatement(AbstractPascalExpression assignable, AbstractPascalExpression expression) {
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
        if (PascalTypes.REAL.equals(left) && PascalTypes.INTEGER.equals(right)) {
            return true;
        }
        if (left instanceof ArrayType leftArray && right instanceof ArrayType rightArray) {
            return leftArray.getElementType().equals(rightArray.getElementType())
                && leftArray.getLowerBound() == rightArray.getLowerBound()
                && leftArray.getUpperBound() == rightArray.getUpperBound();
        }
        return false;
    }

    private AbstractPascalExpression createLiteralExpression(Node literalNode) {
        return switch (head(literalNode).getSymbol()) {
            case "RealLiteral" ->
                PascalValues.realLiteral(Float.parseFloat(literalNode.content()));
            case "IntegerLiteral" ->
                PascalValues.intLiteral(parseIntegerLiteral(head(literalNode)));
            case "\\'" ->
                PascalValues.stringLiteral(literalNode.getChildren().get(1).content());
            case "FALSE\\b", "TRUE\\b" ->
                PascalValues.booleanLiteral(Boolean.parseBoolean(literalNode.content()));
            default ->
                throw new IllegalStateException("Unsupported literal: " + head(literalNode).getSymbol());
        };
    }

    private static Integer parseIntegerLiteral(Node integerLiteral) {
        Node head = head(integerLiteral);
        return switch (head.getSymbol()) {
            case "\\d+" ->
                Integer.valueOf(head.content());
            case "\\$[0-9A-F]+" ->
                Integer.valueOf(head.content().substring(1), 0x10);
            default ->
                throw new IllegalStateException("Invalid integer literal: " + integerLiteral.content());
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
        if (type == null) {
            Optional<Parameter> parameter = operation.getParameters().stream()
                .filter(p -> identifier.equalsIgnoreCase(p.getName().get()))
                .findAny();
            if (parameter.isPresent()) {
                type = parameter.get().getType().get();
            }
        }
        if (type == null) {
            Optional<uml.structure.Object> local = findLocal(operation, identifier);
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

    public void dumpMethods() {
        methods.forEach((operation, statement) -> {
            System.out.println(operation.getName());
            System.out.println(statement);
            System.out.println("-".repeat(40));
        });
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
            return Collections.unmodifiableList(getOperation(name).getParameters());
        }

        public Type getType(String name) {
            return getOperation(name).getType().get();
        }

        public Collection<uml.structure.Object> getLocals(String name) {
            return Collections.unmodifiableCollection(methodLocals.getOrDefault(getOperation(name), Collections.emptyList()));
        }

        public boolean isVoid(Type type) {
            return VOID_TYPE.equals(type);
        }

        public boolean isProcedure(String name) {
            return isVoid(getOperation(name).getType().get());
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
    private final Map<Operation, Collection<uml.structure.Object>> methodLocals = new HashMap<>();
    private final Collection<Type> declaredTypes = new ArrayList<>();
    private final Map<Operation, CompoundStatement> methods = new HashMap<>();

    private static final run.Statement NO_OPERATION = run.Statement.NO_OPERATION;
    
    private static final Type VOID_TYPE = PascalTypes.VOID;

}
