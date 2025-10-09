/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
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
        String programName = node.getChild("Identifier").content();
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        Node declarationsNode = node.getChild("Declarations");
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, programName, declarationsNode);
        methods.put(programName, createBody(node.getChild("CompoundStatement").getChild("Statements")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methods.get(operation.getName().get()));
    }

    private void addProgramVariables(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("VariableDeclaration")
            .ifPresent(declaration -> addVariables(builder, declaration));
        declarations.findChild("Declaration")
            .ifPresent(next -> addVariables(builder, next));
    }

    private void addVariables(UmlClassBuilder builder, Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChild("VariableDeclarationList"));
    }

    private void createAttributes(UmlClassBuilder builder, Node variableDeclarationList) {
        addAttributesFromExpression(builder, variableDeclarationList.getChild("VariableDeclarationExpression"));
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> createAttributes(builder, next));
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"));
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("ProcedureDeclaration")
            .ifPresent(addProcedureDeclaration(builder));
        declarations.findChild("FunctionDeclaration")
            .ifPresent(addFunctionDeclaration(builder));
        declarations.findChild("Declarations")
            .ifPresent(next -> addPrivateFunctionOperations(builder, next));
    }

    private Consumer<Node> addProcedureDeclaration(UmlClassBuilder builder) {
        return procedureDeclaration -> {
            String procedureName = procedureDeclaration.getChild("Identifier").content();
            builder.withOperation(procedureName, UmlTypeFactory.create("Void"), Member.Visibility.PRIVATE);
            methods.put(procedureName, createBody(procedureDeclaration.getChild("CompoundStatement").getChild("Statements")));
        };
    }

    private Consumer<Node> addFunctionDeclaration(UmlClassBuilder builder) {
        return functionDeclaration -> {
            String functionName = functionDeclaration.getChild("Identifier").content();
            builder.withOperation(functionName, UmlTypeFactory.create(functionDeclaration.getChild("TypeExpression").content()), Member.Visibility.PRIVATE);
            methods.put(functionName, createBody(functionDeclaration.getChild("CompoundStatement").getChild("Statements")));
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
                createEnumerationType(typeDeclarationExpression.getChild("IdentifierList"));
            case "ARRAY\\b" ->
                createArrayType(typeDeclarationExpression.getChild("TypeExpression"), typeDeclarationExpression.getChild("TypeExpression"));
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
        addRecordFields(builder, typeDeclarationExpression.getChild("VariableDeclarationList"));
        return builder.build();
    }

    private void addRecordFields(UmlClassBuilder builder, Node variableDeclarationList) {
        addRecordField(builder, variableDeclarationList.getChild("VariableDeclarationExpression"));
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> addRecordFields(builder, next));
    }

    private void addRecordField(UmlClassBuilder builder, Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"));
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type));
    }

    private static String rangeString(Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifierList.getChild("Identifier").content());
        identifierList.findChild("IdentifierList")
            .ifPresent(next -> identifiers.addAll(createIdentifiers(next)));
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, String programName, Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(Node compoundStatement) {
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        createStatementSequence(compoundStatement, diagram);
        diagram.addFinalState();
        return diagram.getTransitions();
    }

    private void createStatementSequence(Node statements, ActivityDiagramBuilder diagram) {
        Optional<Node> statementNode = Optional.of(statements);
        while (statementNode.isPresent()) {
            Statement statement = new Statement(statementNode.get().getChild("Statement"), name -> Optional.ofNullable(methods.get(name)));
            statement.getTransitions(diagram);
            statementNode = statementNode.get().findChild("Statements");
        }
    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methods = new HashMap<>();

}
