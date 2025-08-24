/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.Node;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;


public class PascalCompiler {

    public uml.structure.Class createProgramClass(Node node) {
        if (!"Program".equals(node.getSymbol())) {
            throw new IllegalArgumentException("Program keyword is missing");
        }
        String programName = getNode(node.getChildren(), "Identifier").content();
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        Node declarationsNode = getNode(node.getChildren(), "Declarations");
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, programName, declarationsNode);
        methods.put(programName, createBody(getNode(getNode(node.getChildren(), "CompoundStatement").getChildren(), "Statements")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methods.get(operation.getName().get()));
    }

    private void addProgramVariables(UmlClassBuilder builder, Node declarations) {
        findNode(declarations.getChildren(), "VariableDeclaration")
            .ifPresent(declaration -> addVariables(builder, declaration));
        findNode(declarations.getChildren(), "Declaration")
            .ifPresent(next -> addVariables(builder, next));
    }

    private void addVariables(UmlClassBuilder builder, Node variableDeclaration) {
        createAttributes(builder, getNode(variableDeclaration.getChildren(), "VariableDeclarationList"));
    }

    private void createAttributes(UmlClassBuilder builder, Node variableDeclarationList) {
        addAttributesFromExpression(builder, getNode(variableDeclarationList.getChildren(), "VariableDeclarationExpression"));
        findNode(variableDeclarationList.getChildren(), "VariableDeclarationList")
            .ifPresent(next -> createAttributes(builder, next));
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, Node variableDeclarationExpression) {
        Type type = createType(getNode(variableDeclarationExpression.getChildren(), "TypeDeclarationExpression"));
        createIdentifiers(getNode(variableDeclarationExpression.getChildren(), "IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        findNode(declarations.getChildren(), "FunctionDeclaration")
            .ifPresent(addFunctionDeclaration(builder));
        findNode(declarations.getChildren(), "Declarations")
            .ifPresent(next -> addPrivateFunctionOperations(builder, next));
    }

    private Consumer<Node> addFunctionDeclaration(UmlClassBuilder builder) {
        return functionDeclaration -> {
            String functionName = getNode(functionDeclaration.getChildren(), "Identifier").content();
            builder.withOperation(functionName, UmlTypeFactory.create(getNode(functionDeclaration.getChildren(), "TypeExpression").content()), Member.Visibility.PRIVATE);
            methods.put(functionName, createBody(getNode(getNode(functionDeclaration.getChildren(), "CompoundStatement").getChildren(), "Statements")));
        };
    }

    private Type createType(Node typeDeclarationExpression) {
        Node expression = typeDeclarationExpression.getChildren().getFirst();
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                UmlTypeFactory.create(expression.getChildren().getFirst().content());
            case "RangeExpression" ->
                UmlTypeFactory.create(rangeString(expression));
            case "\\(" ->
                createEnumerationType(getNode(typeDeclarationExpression.getChildren(), "IdentifierList"));
            case "ARRAY\\b" ->
                createArrayType(getNode(typeDeclarationExpression.getChildren(), "TypeExpression"), typeDeclarationExpression.getChildren().get(5));
            case "RECORD\\b" ->
                createRecordType(typeDeclarationExpression);
            default ->
                throw new IllegalStateException("UnsupportedType " + typeDeclarationExpression.getChildren().getFirst().getSymbol());
        };
    }

    private static Type createEnumerationType(Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static Type createArrayType(Node rangeExpression, Node typeExpression) {
        return UmlTypeFactory.create("ARRAY [" + rangeString(rangeExpression) + "] OF " + typeExpression.content());
    }

    private uml.structure.Class createRecordType(Node typeDeclarationExpression) {
        UmlClassBuilder builder = new UmlClassBuilder(typeDeclarationExpression.content());
        addRecordFields(builder, typeDeclarationExpression.getChildren().get(1));
        return builder.build();
    }

    private void addRecordFields(UmlClassBuilder builder, Node variableDeclarationList) {
        addRecordField(builder, getNode(variableDeclarationList.getChildren(), "VariableDeclarationExpression"));
        findNode(variableDeclarationList.getChildren(), "VariableDeclarationList")
            .ifPresent(next -> addRecordFields(builder, next));
    }

    private void addRecordField(UmlClassBuilder builder, Node variableDeclarationExpression) {
        Type type = createType(getNode(variableDeclarationExpression.getChildren(), "TypeDeclarationExpression"));
        createIdentifiers(getNode(variableDeclarationExpression.getChildren(), "IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type));
    }

    private static String rangeString(Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(getNode(identifierList.getChildren(), "Identifier").content());
        findNode(identifierList.getChildren(), "IdentifierList")
            .ifPresent(next -> identifiers.addAll(createIdentifiers(next)));
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, String programName, Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(Node compoundStatement) {
        Collection<Transition<Event, GuardCondition, Action>> body = new ArrayList<>();
        Collection<TransitionSource> leaves = new ArrayList<>(List.of(UmlStateFactory.getInitialState()));
        createStatementSequence(compoundStatement, body, leaves);
        leaves.forEach(leave -> body.add(UmlTransitionFactory.createTransition(leave, UmlStateFactory.getFinalState())));
        return body;
    }

    private void createStatementSequence(Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        Optional<Node> statementNode = Optional.of(statements);
        while (statementNode.isPresent()) {
            Statement statement = new Statement(getNode(statementNode.get().getChildren(), "Statement"), name -> Optional.ofNullable(methods.get(name)));
            statement.getTransitions(transitions, leaves);
            statementNode = findNode(statementNode.get().getChildren(), "Statements");
        }
    }

    private static Node getNode(List<Node> nodes, String symbol) {
        return findNode(nodes, symbol).orElseThrow(() -> new NoSuchElementException(symbol));
    }

    private static Optional<Node> findNode(List<Node> nodes, String symbol) {
        return nodes.stream()
            .filter(node -> symbol.equals(node.getSymbol()))
            .findFirst();
    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methods = new HashMap<>();

}
