import java.io.Console;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


class SymbolTable {
    private static class Symbol {
        public String name;
        public String type;
        public int length;
        public String scope;
        public int index;
        public Node node;
        public int numUse;

        Symbol(String name, String type, int length, String scope, int index, Node node) {
            this.name = name;
            this.type = type;
            this.length = length;
            this.scope = scope;
            this.index = index;
            this.node = node;
            this.numUse = 0;
        }

        @Override
        public String toString() {
            return String.format("Name: %s, Type: %s, Length: %d, Scope: %s, Index %d, numUse  %d", name, type, length, scope, index, numUse);
        }

    }

    private final Map<String, Symbol> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void addSymbol(String name, String type, int length, String scope, int index, Node node) {
        Symbol symbol = new Symbol(name, type, length, scope, index, node);
        table.put(name, symbol);
    }

    public void addNumUse(String name) {
        table.get(name).numUse += 1;
        System.out.println(table.get(name).numUse);
    }

    public Symbol getSymbol(String name) {
        return table.get(name);
    }

    public String getSymbolScope(String name) {
        Symbol symbol = table.get(name);
        if (symbol != null) {
            return symbol.scope;
        } else {
            return null;
        }
    }

    public Node getSymbolNode(String name) {
        Symbol symbol = table.get(name);
        if (symbol != null) {
            return symbol.node;
        } else {
            return null;
        }
    }


    public int getSymbolLength(String name) {
        Symbol symbol = table.get(name);
        if (symbol != null) {
            return symbol.length;
        } else {
            return -1;
        }
    }


    public int getSymbolUseNum(String name) {
        Symbol symbol = table.get(name);
        if (symbol != null) {
            return symbol.numUse;
        } else {
            return -1;
        }
    }

    public String getSymbolByIndex(String scope, int index) {
        for (Symbol symbol : table.values()) {
            if (symbol.scope.equals(scope) && symbol.index == index) {
                return symbol.name;
            }
        }
        return null;
    }


    public void removeSymbol(String name) {
        table.remove(name);
    }

    public void printTable() {
        table.values().forEach(System.out::println);
    }
}

abstract class Node {
    // Список дочерних узлов
    public List<Node> children = new ArrayList<>();

    // Добавление дочернего узла
    public void addChild(Node child) {
        children.add(child);
    }

    public FunctionDeclarationNode isFunction(IdentifierNode name) {
        // Применяем поиск в текущем узле
        if (!this.children.isEmpty()) {
            return searchFunction(this.children.get(0), name);
        }
        return null;
    }

    public abstract void execute(Environment environment);

    public VariableDeclarationNode isVariable(IdentifierNode name) {
        // Применяем поиск в текущем узле
        if (!this.children.isEmpty()) {
            for (int i = 0; i < this.children.size(); i++) {
                VariableDeclarationNode re = searchVariable(this.children.get(i), name);
                if (re != null)
                    return re;
            }
        }
        return null;
    }

    // Метод для красивого вывода дерева
    public void printTree(String indent, boolean last) {
        if (this == null) {
            return; // Предотвращаем вызов на null
        }
        System.out.print(indent);
        boolean lastFlag = true;
        for (Node child : getChildren()) {
            if (child != null) {
                lastFlag = false;
            }
        }
        if (lastFlag) {
            System.out.print("└── ");
            indent += "    ";
        } else {
            System.out.print("├── ");
            indent += "│   ";
        }
        System.out.println(this);
        if (getChildren() != null) {
            for (Node child : getChildren()) {
                if (child != null) {
                    child.printTree(indent, false);
                }
            }
        }
    }

    private FunctionDeclarationNode searchFunction(Node current, IdentifierNode name) {
        if (current == null) {
            return null;
        }

        if (current instanceof FunctionDeclarationNode) {
            FunctionDeclarationNode functionNode = (FunctionDeclarationNode) current;
            if (functionNode.getName().getName().equals(name.getName())) {
                return functionNode;
            }
        }


        for (Node child : current.getChildren()) {
            FunctionDeclarationNode result = searchFunction(child, name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private VariableDeclarationNode searchVariable(Node current, IdentifierNode name) {
        if (current == null) {
            return null;
        }
        System.out.println("H: ");
        System.out.println(current);
        System.out.println(" ");

        if (current instanceof VariableDeclarationNode) {
            VariableDeclarationNode variableNode = (VariableDeclarationNode) current;

            if (variableNode.getName() != null && variableNode.getName().getName().equals(name.getName())) {
                return variableNode;
            }
        }


        for (Node child : current.getChildren()) {
            VariableDeclarationNode result = searchVariable(child, name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public abstract String toString();

    public List<Node> getChildren() {
        return children;
    }
    public boolean isConstant() {
        return false;
    }
}


class ProgramNode extends Node {
    public List<Node> statements;

    public ProgramNode() {
        this.statements = new ArrayList<>();
    }

    public void set(List<Node> statements) {
        this.statements = statements;
    }
    public void addStatement(Node statement) {
        this.statements.add(statement);
        addChild(statement);
    }

    @Override
    public String toString() {
        return "Program";
    }

    @Override
    public void execute(Environment environment) {
        // Выполняем все дочерние узлы программы
        for (Node child : children) {
            child.execute(environment); // Рекурсивно вызываем execute для каждого дочернего узла
        }
    }
}
class Interpreter {
    private Environment environment;

    public Interpreter(Environment initEnvironment) {
        environment = initEnvironment;
    }

    // Метод interpret, который принимает корневой узел программы
    public void interpret(ProgramNode programNode) {
        // Проходим по всем дочерним узлам программы
        for (Node child : programNode.getChildren()) {
            child.execute(environment);
        }
    }
}
class ExpressionNode extends Node {
    private Node leftOperand;
    private final TokenCode operator;
    private Node rightOperand;

    public ExpressionNode(Node leftOperand, TokenCode operator, Node rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;

        addChild(leftOperand);
        addChild(rightOperand);
    }

    public boolean isConstant() {
        if (!operator.equals(TokenCode.NOT)) {
            return leftOperand.isConstant() && rightOperand.isConstant();
        }
        return false;
    }

    @Override
    public String toString() {
        if (operator == null) {
            return "Identifier";
        }
        return "Expression: " + operator.toString();
    }

    @Override
    public void execute(Environment environment) {
        executeExpressions(environment);
    }
    public Node executeExpressions(Environment environment) {
        Node rightEvaluated = rightOperand instanceof ExpressionNode
                ? ((ExpressionNode) rightOperand).executeExpressions(environment)
                : resolveValue(rightOperand, environment, "global");
        Node leftEvaluated =  null;
        if (operator != TokenCode.NOT) {
            leftEvaluated = leftOperand instanceof ExpressionNode
                    ? ((ExpressionNode) leftOperand).executeExpressions(environment)
                    : resolveValue(leftOperand, environment, "global");
        }
        if (rightEvaluated instanceof LiteralNode &&
                (leftEvaluated == null || leftEvaluated instanceof LiteralNode)) {
            // Выполняем проверку типов и вычисление результата
            Node result = checkTypesExecute(leftEvaluated, rightEvaluated);
            // Устанавливаем флаг оптимизации
            Optimizer.flag = true;
            return result;
        }
        return this;
    }

    public Node getLeftOp() {
        return this.leftOperand;
    }

    public Node getRightOp() {
        return this.rightOperand;
    }

    public TokenCode getOperator() {
        return this.operator;
    }

    public void setLeft(Node left) {
        this.leftOperand = left;
    }


    public void setRight(Node right) {
        this.rightOperand = right;
    }

    public Node evaluate() {
        if (leftOperand instanceof LiteralNode && rightOperand instanceof LiteralNode) {
            Node result = checkTypes();
            Optimizer.flag = true;
            return result;
        }
        return this;
    }

    public Node checkTypes() {
        String leftType = ((LiteralNode) leftOperand).getType();
        String rightType = ((LiteralNode) rightOperand).getType();

        switch (operator) {
            case PLUS:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) + Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) + Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                } else if (leftType.equals("string") && rightType.equals("string")) {
                    String result = ((LiteralNode) leftOperand).getValue().toString() + ((LiteralNode) rightOperand).getValue().toString();
                    return new LiteralNode((Object) result, "string");
                }
//                if (leftType.equals("dictionary") && rightType.equals("dictionary")) return;
//                if (leftType.equals("list") && rightType.equals("list")) return;
                break;

            case MINUS:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) - Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) - Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                }
                break;
            case MULTIPLY:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) * Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) * Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                }
                break;
            case DIVIDE:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) / Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) / Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                }
                break;

            case LESS:
            case GREATER:
            case LESS_EQUAL:
            case GREATER_EQUAL:
            case EQUAL:
            case NOT_EQUAL:
                if ((leftType.equals("int") || leftType.equals("real")) &&
                        (rightType.equals("int") || rightType.equals("real"))) {
                    boolean result;
                    double leftValue = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString());
                    double rightValue = Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    switch (operator) {
                        case LESS:
                            result = leftValue < rightValue;
                            break;
                        case GREATER:
                            result = leftValue > rightValue;
                            break;
                        case LESS_EQUAL:
                            result = leftValue <= rightValue;
                            break;
                        case GREATER_EQUAL:
                            result = leftValue >= rightValue;
                            break;
                        case EQUAL:
                            result = leftValue == rightValue;
                            break;
                        case NOT_EQUAL:
                            result = leftValue != rightValue;
                            break;
                        default:
                            throw new ParseException("Invalid comparison operation.");
                    }
                    return new LiteralNode((Object) result, "boolean");
                }
                break;
            case AND:
            case OR:
            case XOR:
                if (leftType.equals("boolean") && rightType.equals("boolean")) {
                    boolean leftValue = Boolean.parseBoolean(((LiteralNode) leftOperand).getValue().toString());
                    boolean rightValue = Boolean.parseBoolean(((LiteralNode) rightOperand).getValue().toString());
                    boolean result;

                    switch (operator) {
                        case AND:
                            result = leftValue && rightValue;
                            break;
                        case OR:
                            result = leftValue || rightValue;
                            break;
                        case XOR:
                            result = leftValue ^ rightValue;
                            break;
                        default:
                            throw new ParseException("Unsupported operation: " + operator);
                    }
                    return new LiteralNode((Object) result, "boolean");
                }
                break;

            case NOT:
                if (rightType.equals("boolean")) {
                    boolean rightValue = Boolean.parseBoolean(((LiteralNode) rightOperand).getValue().toString());
                    boolean result = !rightValue;
                    return new LiteralNode((Object) result, "boolean");
                }
                break;

            default:
                throw new RuntimeException("Unsupported operation for types: " + leftType + " and " + rightType);
        }
        throw new RuntimeException("Invalid operand types for operation: " + leftType + " and " + rightType);
    }

    private Node resolveValue(Node node, Environment environment, String scopeType) {
        if (node instanceof LiteralNode) {
            // Если узел — литерал, возвращаем его как есть
            return node;
        } else if (node instanceof IdentifierNode) {
            // Если узел — переменная, извлекаем значение из окружения
            String varName = ((IdentifierNode) node).getName();
            Object value = environment.getVariable(varName, scopeType).getValue();
            if (value == null) {
                throw new RuntimeException("Variable '" + varName + "' is not defined in the current scope.");
            }
            String type = determineType(environment.getVariable(varName, "global").getValue());
            return new LiteralNode(value, type);
        }
        throw new RuntimeException("Unsupported node type: " + node.getClass());
    }
    private String determineType(Object value) {
        if (value instanceof Integer) {
            return "int";
        } else if (value instanceof Double || value instanceof Float) {
            return "real";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof String) {
            return "string";
        }
        throw new RuntimeException("Unsupported value type: " + value.getClass());
    }


    public Node checkTypesExecute(Node leftOperand, Node rightOperand) {
        String leftType = ((LiteralNode) leftOperand).getType();
        String rightType = ((LiteralNode) rightOperand).getType();

        switch (operator) {
            case PLUS:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) + Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) + Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                } else if (leftType.equals("string") && rightType.equals("string")) {
                    String result = ((LiteralNode) leftOperand).getValue().toString() + ((LiteralNode) rightOperand).getValue().toString();
                    return new LiteralNode((Object) result, "string");
                }
//                if (leftType.equals("dictionary") && rightType.equals("dictionary")) return;
//                if (leftType.equals("list") && rightType.equals("list")) return;
                break;

            case MINUS:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) - Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) - Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                }
                break;
            case MULTIPLY:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) * Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) * Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                }
                break;
            case DIVIDE:
                if (leftType.equals("int") && rightType.equals("int")) {
                    int result = Integer.parseInt(((LiteralNode) leftOperand).getValue().toString()) / Integer.parseInt(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "int");
                } else if ((leftType.equals("int") && rightType.equals("real")) ||
                        (leftType.equals("real") && rightType.equals("int")) ||
                        (leftType.equals("real") && rightType.equals("real"))) {
                    double result = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString()) / Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    return new LiteralNode((Object) result, "real");
                }
                break;

            case LESS:
            case GREATER:
            case LESS_EQUAL:
            case GREATER_EQUAL:
            case EQUAL:
            case NOT_EQUAL:
                if ((leftType.equals("int") || leftType.equals("real")) &&
                        (rightType.equals("int") || rightType.equals("real"))) {
                    boolean result;
                    double leftValue = Double.parseDouble(((LiteralNode) leftOperand).getValue().toString());
                    double rightValue = Double.parseDouble(((LiteralNode) rightOperand).getValue().toString());
                    switch (operator) {
                        case LESS:
                            result = leftValue < rightValue;
                            break;
                        case GREATER:
                            result = leftValue > rightValue;
                            break;
                        case LESS_EQUAL:
                            result = leftValue <= rightValue;
                            break;
                        case GREATER_EQUAL:
                            result = leftValue >= rightValue;
                            break;
                        case EQUAL:
                            result = leftValue == rightValue;
                            break;
                        case NOT_EQUAL:
                            result = leftValue != rightValue;
                            break;
                        default:
                            throw new ParseException("Invalid comparison operation.");
                    }
                    return new LiteralNode((Object) result, "boolean");
                }
                break;
            case AND:
            case OR:
            case XOR:
                if (leftType.equals("boolean") && rightType.equals("boolean")) {
                    boolean leftValue = Boolean.parseBoolean(((LiteralNode) leftOperand).getValue().toString());
                    boolean rightValue = Boolean.parseBoolean(((LiteralNode) rightOperand).getValue().toString());
                    boolean result;

                    switch (operator) {
                        case AND:
                            result = leftValue && rightValue;
                            break;
                        case OR:
                            result = leftValue || rightValue;
                            break;
                        case XOR:
                            result = leftValue ^ rightValue;
                            break;
                        default:
                            throw new ParseException("Unsupported operation: " + operator);
                    }
                    return new LiteralNode((Object) result, "boolean");
                }
                break;

            case NOT:
                if (rightType.equals("boolean")) {
                    boolean rightValue = Boolean.parseBoolean(((LiteralNode) rightOperand).getValue().toString());
                    boolean result = !rightValue;
                    return new LiteralNode((Object) result, "boolean");
                }
                break;

            default:
                throw new RuntimeException("Unsupported operation for types: " + leftType + " and " + rightType);
        }
        throw new RuntimeException("Invalid operand types for operation: " + leftType + " and " + rightType);
    }
}



abstract class DeclarationNode extends Node {
    // Тип декларации: переменная, функция, тип
    public enum DeclarationType {
        VARIABLE,
        FUNCTION
    }

    protected DeclarationType declarationType;

    public DeclarationType getDeclarationType() {
        return declarationType;
    }


    @Override
    public abstract String toString();

    @Override
    public abstract void execute(Environment environment);
}

abstract class StatementNode extends Node {
    private final String statementType;

    public StatementNode(String statementType) {
        this.statementType = statementType;
    }
    public String getStatementType() {
        return statementType;
    }
    @Override
    public String toString() {
        return "statement";
    }
}


class VariableDeclarationNode extends DeclarationNode {
    IdentifierNode variableName;
    Node initializer;
    String type;

    VariableDeclarationNode(IdentifierNode variableName, Node initializer, String type) {
        this.variableName = variableName;
        this.initializer = initializer;
        this.type = type;
        addChild(initializer);
    }

    public IdentifierNode getName() {
        return variableName;
    }

    @Override
    public String toString() {
        return "Variable: " + variableName +" | type: " + this.type;
    }
    @Override
    public void execute(Environment environment) {
        if (this.initializer instanceof LiteralNode) {
            environment.addVariable(this.variableName.getName(), ((LiteralNode) this.initializer).getValue(), "global");
        } else if (this.initializer instanceof ListNode) {
            environment.addVariable(this.variableName.getName(), ((ListNode) this.initializer).toValueList(), "global");
        } else if (this.initializer instanceof ExpressionNode) {
            environment.addVariable(this.variableName.getName(), ((LiteralNode) ((ExpressionNode) this.initializer).executeExpressions(environment)).getValue(), "global");
        }
        System.out.println(environment.getVariable(this.variableName.getName(), "global"));
    }
}

class IdentifierNode extends Node {
    private final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    public int getValue() {
        return Integer.parseInt(this.name);
    }

    @Override
    public String toString() {
        return "Identifier: " + name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void execute(Environment environment) {
    }
}

class BlockNode extends Node {
    private final List<Node> statements;
    private String name;

    public BlockNode(List<Node> statements, String name) {
        this.name = name;
        this.statements = statements;
        for (Node statement : statements) {
            addChild(statement);
        }
    }
    public IdentifierNode getName() {
        return (IdentifierNode) this.statements.get(0);
    }

    public int size() {
        return statements.size();
    }

    public Node get(int index) {
        return statements.get(index);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public void execute(Environment environment) {
    }
}


class FunctionDeclarationNode extends DeclarationNode {
    BlockNode header;
    Node functionBody;

    FunctionDeclarationNode(BlockNode header, Node functionBody) {
        this.header = header;
        this.functionBody = functionBody;

        addChild(header);

//        addChild(parameters); // Добавляем параметры в дочерние узлы

        addChild(functionBody); // Добавляем body в дочерние узлы


    }

    @Override
    public void execute(Environment environment) {
    }

    public IdentifierNode getName() {
        return header.getName();
    }
    @Override
    public String toString() {
        return "Function: ";
    }
}


class IfNode extends StatementNode {
    private final Node condition;
    private final BlockNode thenBody;
    private final BlockNode elseBody;

    public IfNode(Node condition, BlockNode thenBody, BlockNode elseBody) {
        super("if");
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;

        addChild(condition); // Добавляем condition в дочерние узлы
        addChild(thenBody); // Добавляем thenBody
        if (elseBody != null) {
            addChild(elseBody); // Добавляем elseBody, если он есть
        }
    }

    @Override
    public String toString() {
        return "If";
    }

    @Override
    public void execute(Environment environment) {
    }
}



class WhileLoopNode extends StatementNode {
    private final Node condition;
    private final Node body;

    public WhileLoopNode(Node condition, Node body) {
        super("while");
        this.condition = condition;
        this.body = body;

        addChild(condition); // Добавляем condition в дочерние узлы
        addChild(body); // Добавляем body в дочерние узлы
    }

    @Override
    public String toString() {
        return "While";
    }

    @Override
    public void execute(Environment environment) {
    }
}

class ForLoopNode extends StatementNode {
    private final String name;
    private final Node start;
    private final Node end;
    private final Node body;

    public ForLoopNode(String name, Node start, Node end, Node body) {
        super("for");
        this.name = name;
        this.start = start;
        this.end = end;
        this.body = body;

//        addChild(name); // Добавляем инициализацию в дочерние узлы
        addChild(start); // Добавляем start в дочерние узлы
        addChild(end); // Добавляем end в дочерние узлы
        addChild(body); // Добавляем body в дочерние узлы
    }

    @Override
    public String toString() {
        return "For";
    }

    @Override
    public void execute(Environment environment) {
    }
}

class ReturnNode extends StatementNode {
    private final Node expression;

    public ReturnNode(Node expression) {
        super("return");
        this.expression = expression;

        addChild(expression); // Добавляем expression в дочерние узлы
    }

    @Override
    public String toString() {
        return "Return";
    }

    @Override
    public void execute(Environment environment) {
    }
}

class PrintNode extends StatementNode {
    private final Node expression;

    public PrintNode(Node expression) {
        super("print");
        this.expression = expression;

        addChild(expression); // Добавляем expression в дочерние узлы
    }

    @Override
    public String toString() {
        return "Print";
    }

    @Override
    public void execute(Environment environment) {
    }
}


class ListNode extends VariableDeclarationNode {
    private final BlockNode elements;
    private final IdentifierNode name;

    public ListNode(BlockNode elements, IdentifierNode name) {
        super(name, null, null);
        this.elements = elements;
        this.name = name;
        addChild(name); // Добавляем все элементы в дочерние
        addChild(elements); // Добавляем все элементы в дочерние
    }

    public IdentifierNode getName() {
        return variableName;
    }

    public Node get(int index) {
        return elements.get(index);
    }


    public int size() {
        int size = 0;
        if (elements != null) {
            size += elements.size();
        }
        return size;
    }

    @Override
    public String toString() {
        return "List: ";
    }

    public List<Object> toValueList() {
        List<Object> valueList = new ArrayList<>();
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i) instanceof LiteralNode) {
                    LiteralNode element = (LiteralNode) elements.get(i);
                    valueList.add(element.getValue());
                } else if(elements.get(i) instanceof ListNode) {
                    valueList.add(((ListNode) elements.get(i)).toValueList());
                }
            }
        }
        return valueList;
    }

    @Override
    public void execute(Environment environment) {

    }
}

class DictionaryNode extends VariableDeclarationNode {
    private final BlockNode entriesBlock;
    private final IdentifierNode name;

    public DictionaryNode(BlockNode entries, IdentifierNode name) {
        super(name, null, null);
        this.name = name;
        this.entriesBlock = entries;
        addChild(name); // Добавляем BlockNode как дочерний узел
        addChild(entriesBlock); // Добавляем BlockNode как дочерний узел
    }

    public IdentifierNode getName() {
        return variableName;
    }

    @Override
    public String toString() {
        return "Dictionary: " + entriesBlock.toString();
    }
}

class DictionaryEntryNode extends Node {
    private final Node key;
    private final Node value;

    public DictionaryEntryNode(Node key, Node value) {
        this.key = key;
        this.value = value;

        addChild(key); // Добавляем ключ в дочерние узлы
        addChild(value); // Добавляем значение в дочерние узлы
    }

    public Node getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "Entry:";
    }

    @Override
    public void execute(Environment environment) {
    }
}



class LiteralNode extends Node {
    private final Object value;
    private String type;

    public LiteralNode(Object value, String type) {
//        super(null, null, null);
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return this.value;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "Literal: " + value.toString() + " | type: " + type;
    }
    public boolean isConstant() {
        return true; // Литералы всегда являются константами
    }
    @Override
    public void execute(Environment environment) {
    }
}


class FunctionCall extends Node {
    private final Node funcIdentifier;
    private final Node param;

    public FunctionCall(Node funcIdentifier, Node param) {
        this.funcIdentifier = funcIdentifier;
        this.param = param;

        addChild(funcIdentifier);
        addChild(param);
    }

    @Override
    public String toString() {
        return "Function call: ";
    }

    @Override
    public void execute(Environment environment) {
    }
}

class ParseException extends RuntimeException {
    private final String message;
    private final Token token;

    public ParseException(String message) {
        super(message);
        this.message = message;
        this.token = null; // Неизвестный токен
    }

    public ParseException(String message, Token token) {
        super(message + " at " + token.span);
        this.message = message;
        this.token = token;
    }

    @Override
    public String getMessage() {
        if (token != null) {
            return message + " [Line: " + token.span.lineNum + ", Position: " + token.span.getPosBegin() + "]";
        }
        return message;
    }
}





class Parser {
    private List<Token> tokens;
    private int current;
    private ProgramNode program;
    private SymbolTable symbolTable;
    private String scope;
    private int len;


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
        this.program = new ProgramNode();
        this.symbolTable = new SymbolTable();
        this.scope = "global";
        this.len = 0;
    }

    private Token getCurrentToken() {
        return tokens.get(current);
    }

    private void advance() {
        current++;
    }

    private void rewind() {
        current--;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public ProgramNode parseProgram() {
        while (current < tokens.size()) {
            this.scope = "global";
            System.out.println(getCurrentToken().code);
            if (getCurrentToken().code == TokenCode.VAR) {
                program.addStatement(parseDeclaration());
            } else if (getCurrentToken().code == TokenCode.IDENTIFIER) {
                program.addStatement(parseDeclaration());
            } else if (getCurrentToken().code == TokenCode.PRINT) {
                program.addStatement(parsePrint());
            } else if (getCurrentToken().code == TokenCode.FOR) {
                program.addStatement(parseFor());
            } else if (getCurrentToken().code == TokenCode.WHILE) {
                program.addStatement(parseWhile());
            } else if (getCurrentToken().code == TokenCode.IF) {
                program.addStatement(parseIf());
            } else if (getCurrentToken().code == TokenCode.FUNC) {
                program.addStatement(parseFunction());
            } else if (getCurrentToken().code == TokenCode.READ_REAL ||
                    getCurrentToken().code == TokenCode.READ_INT ||
                    getCurrentToken().code == TokenCode.READ_STRING) {
                program.addStatement(parsePrimary());
            } else if (getCurrentToken().code == TokenCode.RETURN) {
                throw new ParseException("ERROR in line: " + getCurrentToken().span.lineNum + ". Return must be used inside the function.");
            }  else if (getCurrentToken().code == TokenCode.ELSE) {
                throw new ParseException("ELSE should be used together IF: " + getCurrentToken().span.lineNum);
            } else if (getCurrentToken().code == TokenCode.EOF) {
                current++;
            } else if (getCurrentToken().code == TokenCode.COMMENT) {
                current++;
            }  else if (getCurrentToken().code == TokenCode.SEMICOLON) {
                current++;
            } else {
                throw new ParseException("Incorrect use of " + getCurrentToken().code +  " in line: " + getCurrentToken().span.lineNum);
            }
//            }else if (getCurrentToken().code == TokenCode.END) {
//                throw new ParseException("Incorrect use of END: " + getCurrentToken().span.lineNum);
//            }  else if (getCurrentToken().code == TokenCode.LOOP) {
//                throw new ParseException("Incorrect use of LOOP: " + getCurrentToken().span.lineNum);
//            }  else if (getCurrentToken().code == TokenCode.THEN) {
//                throw new ParseException("Incorrect use of LOOP: " + getCurrentToken().span.lineNum);
//            }

//            else {
//                current++;
////                throw new ParseException("Unexpected token: " + getCurrentToken().code);
//            }
        }
        this.symbolTable.printTable();
        return program;
    }


    private VariableDeclarationNode parseDeclaration() {
        boolean flagVarDeclare = false;
        if (getCurrentToken().code == TokenCode.VAR) {
            flagVarDeclare = true;
            advance();// Пропускаем 'var'
        }
        if (getCurrentToken().code == TokenCode.ASSIGN) {
            throw new ParseException("The name of the variable was omitted in line: " + getCurrentToken().span.lineNum);
        } else if (getCurrentToken().code != TokenCode.IDENTIFIER) {
            throw new ParseException("Incorrect use of " + getCurrentToken().code +  " in line: " + getCurrentToken().span.lineNum);
        }
        Identifier variableName = (Identifier) getCurrentToken();
        advance(); // Пропускаем идентификатор
//        System.out.println(this.symbolTable.getSymbol(variableName.identifier + "_" + this.scope) != null);
//        if (!flagVarDeclare && this.symbolTable.getSymbol(variableName.identifier + "_" + this.scope) != null) {
//            this.symbolTable.addNumUse(variableName.identifier + "_" + this.scope);
//        }
        if (getCurrentToken().code == TokenCode.ASSIGN) {
            advance();// Пропускаем ':='
            System.out.println(getCurrentToken().code);
            if (flagVarDeclare && this.symbolTable.getSymbol(variableName.identifier + "_" + this.scope) != null) {
                throw new ParseException("Line: " + getCurrentToken().span.lineNum + " | The variable named '" + variableName.identifier +  "' has already been declared");
            }

//            if (getCurrentToken().code == TokenCode.LBRACKET) {
//                BlockNode elem = parseList();
//                return new ListNode(elem, new IdentifierNode(variableName.identifier));
//            }
//            else if (getCurrentToken().code == TokenCode.LBRACE) {
//                BlockNode dictElem = parseDictionary();
//                return new DictionaryNode(dictElem, new IdentifierNode(variableName.identifier));
//            }

//            if (getCurrentToken().code == TokenCode.DOT) {
//                advance();
//                Node elem = parseLogicalExpression();
//                IdentifierNode variableIdentifier = new IdentifierNode(variableName.identifier);
//                Node initializer = new DictionaryEntryNode(variableIdentifier , elem);
//                return new VariableDeclarationNode(variableIdentifier, initializer, null);
//            }

            if (getCurrentToken().code == TokenCode.FUNC) {
                advance(); // Пропускаем 'func'
                this.symbolTable.addSymbol(variableName.identifier + "_" + this.scope, "function", 0, this.scope, -1, null);

                this.scope = variableName.identifier;

                IdentifierNode init = new IdentifierNode(variableName.identifier);
//            String functionName = functionToken.identifier;


                if (getCurrentToken().code != TokenCode.LPAREN) {
                    throw new ParseException("Expected '(', found: " + getCurrentToken());
                }
                advance(); // Пропускаем '('


                List<Node> parameters = new ArrayList<>();
                if (getCurrentToken().code != TokenCode.RPAREN) {
                    VariableDeclarationNode re = parseParameter();
                    this.symbolTable.addSymbol(re.variableName.getName() + "_" + this.scope, "param", 0, variableName.identifier, -1, null);
                    parameters.add(re);

                    while (getCurrentToken().code == TokenCode.COMMA) {
                        advance(); // Пропускаем запятую
                        re = parseParameter();
                        this.symbolTable.addSymbol(re.variableName.getName() + "_" + this.scope, "param", 0, variableName.identifier, -1, null);
                        parameters.add(re);
                    }
                }


                BlockNode param = new BlockNode(parameters, "param");
                if (getCurrentToken().code != TokenCode.RPAREN) {
                    throw new ParseException("Expected ')', found: " + getCurrentToken());
                }
                advance(); // Пропускаем ')'


                if (getCurrentToken().code == TokenCode.IMPLICATION) {
                    advance(); // Пропускаем '=>'
                    Node functionBody = parseStatement();
                    if (getCurrentToken().code != TokenCode.SEMICOLON) {
                        throw new ParseException("Expected ';', found: " + getCurrentToken());
                    }
                    List<Node> headerL = new ArrayList<>();
                    headerL.add(init);
                    headerL.add(param);
                    BlockNode headerBlock = new BlockNode(headerL, "head");

                    List<Node> bodyBlock = new ArrayList<>();
                    bodyBlock.add(functionBody);
                    BlockNode body = new BlockNode(bodyBlock, "body");
                    advance(); // Пропускаем 'end'
                    FunctionDeclarationNode fincRe = new FunctionDeclarationNode(headerBlock, body);
                    IdentifierNode variableIdentifier = new IdentifierNode(variableName.identifier);
                    if (flagVarDeclare) {
                        this.symbolTable.addSymbol(variableName.identifier + "_" + this.scope, "var", 0, this.scope, -1, null);
                    }
                    return new VariableDeclarationNode(variableIdentifier, fincRe, "function");
                } else {
                    throw new ParseException("Expected '>=', found: " + getCurrentToken().code);
                }
            }
            boolean flag = false;
            String last = "";
            if (getCurrentToken().code == TokenCode.LBRACE) {
                flag = true;
                last = this.scope;
                this.scope = variableName.identifier;
            }
            Node initializer = (Node) parseLogicalExpression();
            if (flag) {
                this.scope = last;
            }
            IdentifierNode variableIdentifier = new IdentifierNode(variableName.identifier);
            if (flagVarDeclare) {
                this.symbolTable.addSymbol(variableName.identifier + "_" + this.scope, "var", this.len, this.scope, -1, initializer);
            }
            String type = "expression";
            if (initializer instanceof ListNode) {
                type = "list";
            } else if (initializer instanceof LiteralNode) {
                type = ((LiteralNode) initializer).getType();
            } else if (initializer instanceof DictionaryNode) {
                type = "dictionary";
            }
            return new VariableDeclarationNode(variableIdentifier, initializer, type);
        } else {
            IdentifierNode variableIdentifier = new IdentifierNode(variableName.identifier);
            return new VariableDeclarationNode(variableIdentifier, null, "empty");
        }
    }

    // Разбор цикла for
    private ForLoopNode parseFor() {
        advance(); // Пропускаем 'for'
        Identifier variableName = (Identifier) getCurrentToken();
        advance(); // Пропускаем идентификатор

        if (getCurrentToken().code == TokenCode.IN) {
            advance(); // Пропускаем 'in'
            List<Node> statements = new ArrayList<>();
            statements.add(parseExpression());
            BlockNode rangeStart = new BlockNode(statements, "start");
            if (getCurrentToken().code == TokenCode.TWO_DOT) {
                advance();
//                Node rangeEnd = parseExpression();
                List<Node> statementsEnd = new ArrayList<>();
                statementsEnd.add(parseExpression());
                BlockNode rangeEnd = new BlockNode(statementsEnd, "end");
                if (getCurrentToken().code == TokenCode.LOOP) {
                    advance();
                    List<Node> bodyArr = new ArrayList<>();
                    while (getCurrentToken().code != TokenCode.END) {
                        if (getCurrentToken().code == TokenCode.SEMICOLON) {
                            advance();
                        } else {
                            bodyArr.add(parseStatement());
                        }
                    }
//                    Node body = parseStatement();
                    BlockNode body = new BlockNode(bodyArr, "Body");
                    advance();
                    return new ForLoopNode(variableName.identifier, rangeStart, rangeEnd, body);
                }
                throw new ParseException("Expected 'loop', found: " + getCurrentToken());
            } else if (getCurrentToken().code == TokenCode.LOOP) {
                List<Node> bodyArr = new ArrayList<>();
                advance();
                while (getCurrentToken().code != TokenCode.END) {
                    if (getCurrentToken().code == TokenCode.SEMICOLON) {
                        advance();
                    } else {
                        bodyArr.add(parseStatement());
                    }
                }

                BlockNode body = new BlockNode(bodyArr, "Body");

//                Node body = parseStatement();
                advance();
                return new ForLoopNode(variableName.identifier, rangeStart, null, body);
            }
            throw new ParseException("Expected 'to', found: " + getCurrentToken());
        }
        throw new ParseException("Expected 'in', found: " + getCurrentToken());
    }

    private Node parseFunction() {
        if (getCurrentToken().code == TokenCode.FUNC) {
            advance(); // Пропускаем 'func'

            if (getCurrentToken().code != TokenCode.IDENTIFIER) {
                throw new ParseException("Expected function name, found: " + getCurrentToken());
            }
            Identifier functionToken = (Identifier) getCurrentToken();
            if (this.symbolTable.getSymbol(functionToken.identifier + "_" + this.scope) != null) {
                throw new ParseException("Line: " + getCurrentToken().span.lineNum + " | The function named '" + functionToken.identifier +  "' has already been declared");
            }
            this.symbolTable.addSymbol(functionToken.identifier + "_" + this.scope, "function", 0, "global", -1, null);
            this.scope = functionToken.identifier;


            IdentifierNode init = new IdentifierNode(functionToken.identifier);
//            String functionName = functionToken.identifier;
            advance(); // Пропускаем название функции

            if (getCurrentToken().code != TokenCode.LPAREN) {
                throw new ParseException("Expected '(', found: " + getCurrentToken());
            }
            advance(); // Пропускаем '('


            List<Node> parameters = new ArrayList<>();
            if (getCurrentToken().code != TokenCode.RPAREN) {
                VariableDeclarationNode re = parseParameter();
                this.symbolTable.addSymbol(re.variableName.getName() + "_" + this.scope, "param", 0, functionToken.identifier, -1, null);
                parameters.add(re);

                while (getCurrentToken().code == TokenCode.COMMA) {
                    advance(); // Пропускаем запятую
                    re = parseParameter();
                    this.symbolTable.addSymbol(re.variableName.getName() + "_" + this.scope, "param", 0, functionToken.identifier, -1, null);
                    parameters.add(re);
                }
            }


            BlockNode param = new BlockNode(parameters, "param");
            if (getCurrentToken().code != TokenCode.RPAREN) {
                throw new ParseException("Expected ')', found: " + getCurrentToken());
            }
            advance(); // Пропускаем ')'


            if (getCurrentToken().code == TokenCode.IS) {
                advance(); // Пропускаем 'is'

                Node functionBody = parseBlock();

                if (getCurrentToken().code != TokenCode.END) {
                    throw new ParseException("Expected 'end', found: " + getCurrentToken());
                }
                advance(); // Пропускаем 'end'

                List<Node> headerL = new ArrayList<>();
                headerL.add(init);
                headerL.add(param);
                BlockNode headerBlock = new BlockNode(headerL, "head");

                return new FunctionDeclarationNode(headerBlock, functionBody);
            } else if (getCurrentToken().code == TokenCode.IMPLICATION) {
                advance(); // Пропускаем '=>'
                Node functionBody = parseStatement();
                if (getCurrentToken().code != TokenCode.SEMICOLON) {
                    throw new ParseException("Expected ';', found: " + getCurrentToken());
                }
                List<Node> headerL = new ArrayList<>();
                headerL.add(init);
                headerL.add(param);
                BlockNode headerBlock = new BlockNode(headerL, "head");

                List<Node> bodyBlock = new ArrayList<>();
                bodyBlock.add(functionBody);
                BlockNode body = new BlockNode(bodyBlock, "body");
                advance(); // Пропускаем 'end'
                return new FunctionDeclarationNode(headerBlock, body);
            } else {
                throw new ParseException("Expected 'is' or '>=', found: " + getCurrentToken().code);
            }
        }

        throw new ParseException("Expected 'func', found: " + getCurrentToken());
    }


    private VariableDeclarationNode parseParameter() {
        if (getCurrentToken().code != TokenCode.IDENTIFIER) {
            throw new ParseException("Expected parameter name, found: " + getCurrentToken());
        }

        // Считываем идентификатор параметра
        Identifier param = (Identifier) getCurrentToken();
        String paramName = param.identifier;
        advance(); // Пропускаем идентификатор

        ExpressionNode defaultValue = null;

        // Проверяем, есть ли присвоение значения (':=')
        if (getCurrentToken().code == TokenCode.ASSIGN) {
            advance(); // Пропускаем ':='

            // Разбираем выражение, которое является значением по умолчанию
            Node expressionNode = parseExpression();

            // Приводим результат к ExpressionNode
            if (!(expressionNode instanceof ExpressionNode)) {
                throw new ParseException("Expected an expression as default value, found: " + expressionNode);
            }

            defaultValue = (ExpressionNode) expressionNode;
        }
        IdentifierNode identifierNode = new IdentifierNode(paramName);
        return new VariableDeclarationNode(identifierNode, defaultValue, null);
    }




    private Node parseBlock() {
        List<Node> statements = new ArrayList<>();
        while (getCurrentToken().code != TokenCode.END) {
            statements.add(parseStatement());
        }

        return new BlockNode(statements, "Body");
    }


    // Разбор цикла while
//    private WhileLoopNode parseWhile() {
//        advance(); // Пропускаем 'while'
//        Node condition = parseExpression(); // Разбор условия
//
//        if (getCurrentToken().code == TokenCode.LOOP) {
//            advance(); // Пропускаем 'loop'
//            Node body = parseStatement(); // Разбор тела цикла
//            advance(); // Пропускаем 'end'
//            return new WhileLoopNode(condition, body);
//        }
//        throw new ParseException("Expected 'loop', found: " + getCurrentToken());
//    }

    private WhileLoopNode parseWhile() {
        advance(); // Пропускаем 'if'

        Node condition = parseCondition();

        if (getCurrentToken().code == TokenCode.LOOP) {
            advance(); // Пропускаем 'then'

            List<Node> thenBodyStatements = new ArrayList<>();
            while (getCurrentToken().code != TokenCode.END && getCurrentToken().code != TokenCode.ELSE) {
                if (getCurrentToken().code == TokenCode.SEMICOLON) {
                    advance();
                } else {
                    thenBodyStatements.add(parseStatement()); // Разбор тела
                }
            }

            BlockNode thenBody = new BlockNode(thenBodyStatements, "Body");

            if (getCurrentToken().code == TokenCode.END) {
                advance(); // Пропускаем 'end'
            }

            return new WhileLoopNode(condition, thenBody);
        }

        throw new ParseException("Expected 'then', found: " + getCurrentToken().code);
    }

    private IfNode parseIf() {
        advance(); // Пропускаем if

        Node condition = parseCondition();

        if (getCurrentToken().code == TokenCode.THEN) {
            advance(); // Пропускаем then

            List<Node> thenBodyStatements = new ArrayList<>();
            while (getCurrentToken().code != TokenCode.END && getCurrentToken().code != TokenCode.ELSE) {
                if (getCurrentToken().code == TokenCode.SEMICOLON) {
                    advance();
                } else {
                    thenBodyStatements.add(parseStatement());
                }
            }

            BlockNode thenBody = new BlockNode(thenBodyStatements, "Body");

            if (getCurrentToken().code == TokenCode.END) {
                advance(); // Пропускаем end
            }

            List<Node> elseBodyStatements = new ArrayList<>();
            BlockNode elseBody = null;
            if (getCurrentToken().code == TokenCode.ELSE) {
                advance(); // Пропускаем else
                while (getCurrentToken().code != TokenCode.END) {
                    if (getCurrentToken().code == TokenCode.SEMICOLON) {
                        advance();
                    } else {
                        elseBodyStatements.add(parseStatement());
                    }
                }
                elseBody = new BlockNode(elseBodyStatements, "Else");

                if (getCurrentToken().code == TokenCode.END) {
                    advance(); // Пропускаем end
                } else {
                    throw new ParseException("Expected 'end', found: " + getCurrentToken());
                }
            }

            return new IfNode(condition, thenBody, elseBody);
        }

        throw new ParseException("Expected 'then', found: " + getCurrentToken().code);
    }

    // Разбор команды print
    private PrintNode parsePrint() {
        advance(); // Пропускаем 'print'
        List<Node> expressions = new ArrayList<>(); // Список для хранения выражений

        // Парсим первое выражение
        expressions.add(parseLogicalExpression());

        // Продолжаем разбирать, пока есть запятые
        while (getCurrentToken().code == TokenCode.COMMA) {
            advance(); // Пропускаем запятую
            expressions.add(parseLogicalExpression()); // Парсим следующее выражение
        }

        // Ожидаем ';' в конце
        if (getCurrentToken().code != TokenCode.SEMICOLON) {
            throw new ParseException("Expected ';' at the end of print statement, found: " + getCurrentToken().code);
        }
        advance(); // Пропускаем ';'

        BlockNode exp = new BlockNode(expressions, "expressions");

        return new PrintNode(exp); // Возвращаем узел Print с выражениями
    }

    // Разбор команды print
    private ReturnNode parseReturn() {
        advance(); // Пропускаем 'return'
        Node expression = parseExpression();
        if (getCurrentToken().code == TokenCode.SEMICOLON){
            advance();
        }
        while (getCurrentToken().code != TokenCode.SEMICOLON && getCurrentToken().code != TokenCode.END && getCurrentToken().code != TokenCode.ELSE) {
            advance();
        }
        return new ReturnNode(expression);
    }

    private Node parseIdentifier() {
        if (getCurrentToken().code == TokenCode.IDENTIFIER) {
            Identifier identifierToken = (Identifier) getCurrentToken();
            String identifierName = identifierToken.identifier;
//            if ()
            advance();

            return new IdentifierNode(identifierName);
        }

        throw new ParseException("Expected identifier, found: " + getCurrentToken().code);
    }


    private ExpressionNode parseTypeCheck() {
        Node identifier = parseIdentifier();

        if (getCurrentToken().code == TokenCode.IS) {
            advance(); // Пропускаем 'is'
            Node typeNode = parseType();
            return new ExpressionNode(identifier, TokenCode.IS, typeNode);
        } else if (getCurrentToken().code == TokenCode.IN) {
            advance();
            Node typeNode = parseType();
            return new ExpressionNode(identifier, TokenCode.IN, typeNode);
        }

        throw new ParseException("Expected 'is', found: " + getCurrentToken());
    }


    private Node parseType() {
        switch (getCurrentToken().code) {
            case INT:
                advance(); // Пропускаем 'int'
                return new LiteralNode("int", "int");
            case REAL:
                advance(); // Пропускаем 'real'
                return new LiteralNode("real", "real");
            case STRING:
                advance(); // Пропускаем 'string'
                return new LiteralNode("string", "string");
            case TUPLE_LITERAL:
                advance(); // Пропускаем 'tuple'
                return new LiteralNode("tuple", "tuple");
            case ARRAY_LITERAL:
                advance(); // Пропускаем 'array'
                return new LiteralNode("array", "array");
            case IDENTIFIER:
                advance(); // Пропускаем 'identifier'
                return new LiteralNode("identifier", "identifier");
            case EMPTY:
                advance(); // Пропускаем 'empty'
                return new LiteralNode("empty", "empty");
            default:
                throw new ParseException("Expected a type, found: " + getCurrentToken().code);
        }
    }


    private Node parseCondition() {
        Node comparison = null;
        boolean flag = true;
        if (getCurrentToken().code == TokenCode.IDENTIFIER) {
            advance(); // Пропускаем идентификатор
            if (getCurrentToken().code == TokenCode.IS) {
                rewind(); // Возвращаемся назад
                comparison = parseTypeCheck();
                flag = false;
            } else if (getCurrentToken().code == TokenCode.IN) {
                rewind(); // Возвращаемся назад
                comparison = parseTypeCheck();
                flag = false;
            }else {
                rewind();
            }
        }

        if (flag) {
            comparison = parseComparison();
        }

        while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR || getCurrentToken().code == TokenCode.XOR) {
            TokenCode logicalOperator = getCurrentToken().code;
            advance(); // Пропускаем логический оператор
            Node rightOperand = parseComparison();
            comparison = new ExpressionNode(comparison, logicalOperator, rightOperand);
        }

        return comparison;
    }


    private Node parseComparison() {
        // Проверяем наличие 'not'
        if (getCurrentToken().code == TokenCode.NOT) {
            advance(); // Пропускаем 'not'

            if (getCurrentToken().code == TokenCode.LPAREN) {
                advance(); // Пропускаем '('
                Node innerComparison = parseComparison();

                if (getCurrentToken().code != TokenCode.RPAREN) {
                    throw new ParseException("Expected ')', found: " + getCurrentToken());
                }
                advance(); // Пропускаем ')'
                return new ExpressionNode(null, TokenCode.NOT, innerComparison);
            } else {
                System.out.println(getCurrentToken().code);
                Node innerComparison = parseComparisonWithoutLogicalOperators();
                return new ExpressionNode(null, TokenCode.NOT, innerComparison);
            }
        }

        if (getCurrentToken().code == TokenCode.LPAREN) {
            advance(); // Пропускаем '('
            Node innerComparison = parseLogicalExpression();
            if (getCurrentToken().code != TokenCode.RPAREN) {
                throw new ParseException("Expected ')', found: " + getCurrentToken());
            }
            advance(); // Пропускаем ')'
            return innerComparison;
        }
        Node leftOperand = parseExpression();

        TokenCode operator = getCurrentToken().code;
//        if (operator == TokenCode.DOT) {
//            advance();
//            TokenCode key = getCurrentToken().code;
//            IdentifierNode keyIdentifier = new IdentifierNode(key.name());
//            leftOperand = new DictionaryEntryNode(leftOperand, keyIdentifier);
//        }
        if (isComparisonOperator(operator)) {
            advance();
            Node rightOperand = parseExpression();

//            System.out.println(((LiteralNode) leftOperand).getValue());
            leftOperand = new ExpressionNode(leftOperand, operator, rightOperand);
        }


        while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR || getCurrentToken().code == TokenCode.XOR) {
            TokenCode logicalOperator = getCurrentToken().code;
            advance(); // Пропускаем логический оператор
            Node rightOperand = parseComparison();
            leftOperand = new ExpressionNode(leftOperand, logicalOperator, rightOperand);
        }

        return leftOperand;
    }



    private Node parseComparisonWithoutLogicalOperators() {
        Node leftOperand = parseExpression();

        TokenCode operator = getCurrentToken().code;
        if (isComparisonOperator(operator)) {
            advance(); // Пропускаем оператор
            Node rightOperand = parseExpression();
            leftOperand = new ExpressionNode(leftOperand, operator, rightOperand);
        }

        return leftOperand;
    }

    private Node parseLogicalExpression() {
        Node leftOperand = parseComparison();

        while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR || getCurrentToken().code == TokenCode.XOR) {
            TokenCode operator = getCurrentToken().code;
            advance(); // Пропускаем логический оператор
            Node rightOperand = parseComparison();
            leftOperand = new ExpressionNode(leftOperand, operator, rightOperand);
        }

        return leftOperand;
    }

    // Парсер для словаря
    private BlockNode parseDictionary() {
        advance(); // Пропускаем '{'

        List<Node> entries = new ArrayList<>();

        // Если словарь пустой, сразу закрываем его
        if (getCurrentToken().code != TokenCode.RBRACE) {
            Node key = parseIdentifier(); // Разбираем ключ
            if (getCurrentToken().code != TokenCode.ASSIGN) {
                throw new ParseException("Expected ':', found: " + getCurrentToken().code);
            }

            advance(); // Пропускаем ':'
            boolean flag = false;
            String last = "";
            if (getCurrentToken().code == TokenCode.LBRACE) {
                last = this.scope;
                flag = true;
                this.scope = ((IdentifierNode) key).getName();
            }
            Node value = parseExpression(); // Разбираем значение
            this.symbolTable.addSymbol(((IdentifierNode) key).getName() + "_" + this.scope, "dict_key", 0, this.scope, 0, null);
            entries.add(new DictionaryEntryNode(key, value)); // Добавляем пару в список
            if (flag) {
                this.scope = last;
            }
            // Пока есть запятые, продолжаем разбор пар
            int indexNum = 0;
            while (getCurrentToken().code == TokenCode.COMMA) {
                indexNum++;
                advance(); // Пропускаем запятую
                key = parseIdentifier(); // Разбираем следующий ключ

                if (getCurrentToken().code == TokenCode.ASSIGN) {
                    advance(); // Пропускаем ':'
                    flag = false;
                    last = "";
                    if (getCurrentToken().code == TokenCode.LBRACE) {
                        last = this.scope;
                        flag = true;
                        this.scope = ((IdentifierNode) key).getName();
                    }
                    value = parseExpression(); // Разбираем значение
                    if (flag) {
                        this.scope = last;
                    }
                    entries.add(new DictionaryEntryNode(key, value)); // Добавляем следующую пару
                    this.symbolTable.addSymbol(((IdentifierNode) key).getName() + "_" + this.scope, "dict_key", 0, this.scope, indexNum, null);
                } else if (getCurrentToken().code == TokenCode.COMMA || getCurrentToken().code == TokenCode.RBRACE) {
                    entries.add(new DictionaryEntryNode(key, null)); // Добавляем следующую пару
                    this.symbolTable.addSymbol(((IdentifierNode) key).getName() + "_" + this.scope, "dict_key", 0, this.scope, indexNum, null);
                } else {
                    throw new ParseException("Expected ':=', found: " + getCurrentToken().code);
                }
            }

        }

        if (getCurrentToken().code != TokenCode.RBRACE) {
            throw new ParseException("Expected '}', found: " + getCurrentToken().code);
        }

        advance(); // Пропускаем '}'
        this.len = entries.size();
        return new BlockNode(entries, "entries");
    }




    private boolean isComparisonOperator(TokenCode code) {
        return code == TokenCode.GREATER ||
                code == TokenCode.LESS ||
                code == TokenCode.GREATER_EQUAL ||
                code == TokenCode.LESS_EQUAL ||
                code == TokenCode.EQUAL;
    }


    private Node parseExpression() {
        return parseExpressionWithPrecedence(0);
    }

    private Node parseExpressionWithPrecedence(int precedence) {
        Node left = parsePrimary();


        while (isOperator(getCurrentToken().code) && precedence < getPrecedence(getCurrentToken().code)) {
            Token operator = getCurrentToken();
            advance(); // Пропускаем оператор

            int operatorPrecedence = getPrecedence(operator.code);
            Node right = parseExpressionWithPrecedence(operatorPrecedence);

            left = new ExpressionNode(left, operator.code, right);
        }

        if (getCurrentToken().code == TokenCode.ASSIGN) {
            advance(); // Пропускаем ':=' или '='

            Node right = parseExpression();

            if (left instanceof IdentifierNode) {
                return new VariableDeclarationNode(((IdentifierNode) left), (ExpressionNode) right, null);
            } else {
                throw new ParseException("Left-hand side of assignment must be an identifier");
            }
        }

        return left;
    }


    private int getPrecedence(TokenCode code) {
        switch (code) {
            case PLUS:
            case MINUS:
                return 1;
            case MULTIPLY:
            case DIVIDE:
                return 2;
            default:
                return 0;
        }
    }

    private Node parsePrimary() {
        if (getCurrentToken().code == TokenCode.REAL_LITERAL) {
            RealToken realToken = (RealToken) getCurrentToken();
            advance();
            return new LiteralNode(realToken.value, "real");
        } else if (getCurrentToken().code == TokenCode.INTEGER_LITERAL) {
            IntegerToken integerToken = (IntegerToken) getCurrentToken();
            advance();
            return new LiteralNode(integerToken.value, "int");
        } else if (getCurrentToken().code == TokenCode.STRING_LITERAL) {
            StringToken stringToken = (StringToken) getCurrentToken();
            advance();
            return new LiteralNode(stringToken.value, "string");
        } else if (getCurrentToken().code == TokenCode.EMPTY) {
//            StringToken stringToken = (StringToken) getCurrentToken();
            advance();
            return new LiteralNode("empty", "empty");
        } else if (getCurrentToken().code == TokenCode.BOOLEAN_LITERAL) {
            BooleanToken booleanToken = (BooleanToken) getCurrentToken();
            advance();
            return new LiteralNode(booleanToken.value, "boolean");
        } else if (getCurrentToken().code == TokenCode.LBRACKET) {
            BlockNode elem = parseList();
            return new ListNode(elem, null);
        }
        else if (getCurrentToken().code == TokenCode.LBRACE) {
            BlockNode dictElem = parseDictionary();
            return new DictionaryNode(dictElem, null);
        } else if (getCurrentToken().code == TokenCode.READ_INT) {
            advance();
            if (getCurrentToken().code == TokenCode.LPAREN) {
                advance();
            }
            if (getCurrentToken().code != TokenCode.RPAREN) {
                System.out.println(getCurrentToken().code);
                throw new ParseException("Expected ')', found: " + getCurrentToken().code);
            }

            FunctionCall re = new FunctionCall(new IdentifierNode("Read INT"), null);
            advance();
            return re;
        } else if (getCurrentToken().code == TokenCode.READ_STRING) {
            advance();
            if (getCurrentToken().code == TokenCode.LPAREN) {
                advance();
            }

            if (getCurrentToken().code != TokenCode.RPAREN) {
                System.out.println(getCurrentToken().code);
                throw new ParseException("Expected ')', found: " + getCurrentToken().code);
            }

            FunctionCall re = new FunctionCall(new IdentifierNode("Read STRING"), null);
            advance();
            return re;
        }else if (getCurrentToken().code == TokenCode.READ_REAL) {
            advance();
            if (getCurrentToken().code == TokenCode.LPAREN) {
                advance();
            }

            if (getCurrentToken().code != TokenCode.RPAREN) {
                System.out.println(getCurrentToken().code);
                throw new ParseException("Expected ')', found: " + getCurrentToken().code);
            }

            FunctionCall re = new FunctionCall(new IdentifierNode("Read REAL"), null);
            advance();
            return re;
        } else if (getCurrentToken().code == TokenCode.LPAREN) {
            advance(); // Пропускаем '('
            Node expression = parseExpression(); // Разбор выражения в скобках
            if (getCurrentToken().code != TokenCode.RPAREN) {
                throw new ParseException("Expected ')', found: " + getCurrentToken().code);
            }
            advance(); // Пропускаем ')'
            return expression;
        } else if (getCurrentToken().code == TokenCode.IDENTIFIER) {
            Identifier identifierToken = (Identifier) getCurrentToken();
            if (program.isFunction(new IdentifierNode(identifierToken.identifier)) != null) {
                List<Node> parameters = new ArrayList<>();
//                System.out.println(identifierToken.identifier + "_" + this.scope);
                this.symbolTable.addNumUse(identifierToken.identifier + "_" + this.scope);
                advance();
                if (getCurrentToken().code == TokenCode.LPAREN) {
                    advance();
                }
                if (getCurrentToken().code != TokenCode.RPAREN) {
                    Node ne = parseCondition();
                    System.out.println(ne);
                    parameters.add(ne);

                    while (getCurrentToken().code == TokenCode.COMMA) {
                        advance(); // Пропускаем запятую
                        parameters.add(parseCondition());
                    }
                }


                BlockNode param = new BlockNode(parameters, "param");
                if (getCurrentToken().code != TokenCode.RPAREN) {
                    System.out.println(getCurrentToken().code);
                    throw new ParseException("Expected ')', found: " + getCurrentToken().code);
                }

                FunctionCall re = new FunctionCall(new IdentifierNode(identifierToken.identifier), param);
                advance();
                return re;
//            } else if (program.isVariable(new IdentifierNode(identifierToken.identifier)) != null) {
            } else if (this.symbolTable.getSymbolScope(identifierToken.identifier + "_" + this.scope) == scope) {
                advance();
                this.symbolTable.addNumUse(identifierToken.identifier + "_" + this.scope);
                if (getCurrentToken().code == TokenCode.DOT) {
                    IdentifierNode variableIdentifier = new IdentifierNode(identifierToken.identifier);
                    advance();  // Переходим к следующему токену
                    Node initializer = getEntry(variableIdentifier);  // Получаем начальный узел

                    String index = ((IdentifierNode) ((DictionaryEntryNode) initializer).getValue()).getName();
                    String indexName = null;

                    // Проверка, является ли `index` числовым значением
                    if (isInteger(index)) {
                        int listLength = symbolTable.getSymbolLength(identifierToken.identifier + "_" + this.scope);
                        if (Integer.parseInt(index) < 0 || Integer.parseInt(index) >= listLength) {
                            throw new ParseException("Index " + index + " out of bounds for list " + identifierToken.identifier);
                        }
                        indexName = this.symbolTable.getSymbolByIndex(identifierToken.identifier, Integer.parseInt(index));
                        indexName = indexName.substring(0, indexName.indexOf('_'));
                    } else {
                        // Проверка, существует ли индекс как ключ в текущей области видимости
                        if (symbolTable.getSymbolScope(index + "_" + identifierToken.identifier) == null) {
                            throw new ParseException("Dictionary " + this.scope + " does not contain the key " + identifierToken.identifier);
                        }

                    }

                    // Обновляем scope на основе значения indexName или index
                    String lastScope = this.scope;
                    this.scope = (indexName != null) ? indexName : index;

                    // Обрабатываем вложенные обращения через .
                    while (getCurrentToken().code == TokenCode.DOT) {
                        advance();  // Переходим к следующему токену
                        initializer = getEntry(initializer);  // Получаем узел для вложенного элемента

                        // Получаем название вложенного ключа
                        index = ((IdentifierNode) ((DictionaryEntryNode) initializer).getValue()).getName();
                        indexName = null;

                        if (isInteger(index)) {
                            if (this.symbolTable.getSymbolByIndex(this.scope, Integer.parseInt(index)) == null) {
                                throw new ParseException("Index " + index + " out of bounds for list " + this.scope);
                            }
                            indexName = this.symbolTable.getSymbolByIndex(this.scope, Integer.parseInt(index));
                            indexName = indexName.substring(0, indexName.indexOf('_'));
//                            int listLength = symbolTable.getSymbolLength(this.scope);
//
////                            System.out.println(symbolTable.getSymbolNode(this.scope));
//                            if (Integer.parseInt(index) < 0 || Integer.parseInt(index) >= listLength) {
//                                throw new ParseException("Index " + index + " out of bounds for list " + indexName + "_" + this.scope);
//                            }
                        } else {
                            // Проверка на существование ключа во вложенном словаре
                            if (symbolTable.getSymbolScope(index + "_" + this.scope) == null) {
                                throw new ParseException("Dictionary " + this.scope + " does not contain the key " + index);
                            }
                        }

                        // Обновляем область видимости
                        lastScope = this.scope;
                        this.scope = (indexName != null) ? indexName : index;
                    }

                    // Восстанавливаем исходную область видимости
                    this.scope = lastScope;
                    return initializer;
                } else if (getCurrentToken().code == TokenCode.LBRACKET) {
//                    Node elem = parseLogicalExpression();

                    IdentifierNode variableIdentifier = new IdentifierNode(identifierToken.identifier);
                    advance();
                    Node initializer = getEntry(variableIdentifier);

// Получаем ListNode из таблицы символов
                    ListNode listNode = (ListNode) this.symbolTable.getSymbolNode(identifierToken.identifier + "_" + this.scope);

// Проверяем, что начальный индекс в допустимых пределах
                    int index = ((IdentifierNode) ((DictionaryEntryNode) initializer).getValue()).getValue();
                    int listLength = listNode.size();
                    if (index < 0 || index >= listLength) {
                        throw new ParseException("Index " + index + " out of bounds for list " + identifierToken.identifier);
                    }

                    Node selectedNode = listNode.get(index); // Получаем элемент на данном индексе

// Проверка, является ли элемент вложенным списком
                    if (selectedNode instanceof ListNode) {
                        listNode = (ListNode) selectedNode; // Обновляем listNode на текущий уровень вложенности
                    }
                    advance();
// Переход на следующий уровень вложенности, если он есть
                    while (getCurrentToken().code == TokenCode.LBRACKET) {
                        advance();
                        initializer = getEntry(initializer);
                        int nestedIndex = ((IdentifierNode) ((DictionaryEntryNode) initializer).getValue()).getValue();

                        if (!(listNode instanceof ListNode)) {
                            throw new ParseException("Это не лист");
                        }

                        // Проверяем, что текущий элемент является списком
//                        if (!(listNode.get(nestedIndex) instanceof ListNode)) {
//                            throw new ParseException("Expected a nested list at index " + nestedIndex + " in " + identifierToken.identifier);
//                        }

//                        listNode = (ListNode) listNode).get(nestedIndex); // Обновляем текущий listNode
                        int nestedListLength = listNode.size();
                        if (nestedIndex < 0 || nestedIndex >= nestedListLength) {
                            throw new ParseException("Index " + nestedIndex + " out of bounds for nested list at " + identifierToken.identifier);
                        }
                        if (listNode.get(nestedIndex) instanceof ListNode) {
                            listNode = (ListNode) listNode.get(nestedIndex); // Обновляем listNode на текущий уровень вложенности
                        }
                        advance();
                    }


// Возвращаем финальный элемент, если проверка завершена успешно
                    return initializer;

                }
                return new IdentifierNode(identifierToken.identifier);
            } else {
                throw new ParseException("The identifier is not declared: " + ((Identifier) getCurrentToken()).identifier + " in line " + ((Identifier) getCurrentToken()).span.lineNum);
            }
        }

        throw new ParseException("Incorrect use of " + getCurrentToken().code +  " in line: " + getCurrentToken().span.lineNum);


//        throw new ParseException("Unexpected token: " + getCurrentToken().code);
    }


    public boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private Node getEntry(Node variableIdentifier) {
        IdentifierNode key = null;
        if (getCurrentToken().code == TokenCode.IDENTIFIER) {
            key = new IdentifierNode(((Identifier) getCurrentToken()).identifier);
            Node initializer = new DictionaryEntryNode(variableIdentifier , key);
            advance();
            return initializer;
        } else if (getCurrentToken().code == TokenCode.MINUS) {
            advance();
            if (getCurrentToken().code == TokenCode.INTEGER_LITERAL) {
                key = new IdentifierNode("-" + ((Integer) (((IntegerToken) getCurrentToken()).value)).toString());
                Node initializer = new DictionaryEntryNode(variableIdentifier , key);
                advance();
                return initializer;
            } else {
                throw new ParseException("Expected integer index: " + getCurrentToken().code);
            }
        } else if (getCurrentToken().code == TokenCode.INTEGER_LITERAL) {
            key = new IdentifierNode(((Integer) (((IntegerToken) getCurrentToken()).value)).toString());
            Node initializer = new DictionaryEntryNode(variableIdentifier , key);
            advance();
            return initializer;
        } else if (getCurrentToken().code == TokenCode.MINUS) {
            advance();
            if (getCurrentToken().code == TokenCode.INTEGER_LITERAL) {
                key = new IdentifierNode("-" + ((Integer) (((IntegerToken) getCurrentToken()).value)).toString());
                Node initializer = new DictionaryEntryNode(variableIdentifier , key);
                advance();
                return initializer;
            } else {
                throw new ParseException("Expected integer index: " + getCurrentToken().code);
            }
        } else if (getCurrentToken().code == TokenCode.STRING_LITERAL) {
            key = new IdentifierNode(((StringToken) getCurrentToken()).value);
            Node initializer = new DictionaryEntryNode(variableIdentifier , key);
            advance();
            return initializer;
        } else if (getCurrentToken().code == TokenCode.LENGTH) {
            key = new IdentifierNode("LENGTH");
            Node initializer = new DictionaryEntryNode(variableIdentifier , key);
            advance();
            return initializer;
        }
        throw new ParseException("Unexpected token: " + getCurrentToken().code);
    }

    private boolean isOperator(TokenCode code) {
        return code == TokenCode.PLUS || code == TokenCode.MINUS || code == TokenCode.MULTIPLY || code == TokenCode.DIVIDE;
    }

    private BlockNode parseList() {
        List<Node> elements = new ArrayList<>();
        if (getCurrentToken().code == TokenCode.LBRACKET) {
            advance(); // Пропускаем '['
            // Разбор элементов списка до закрывающей скобки
            while (getCurrentToken().code != TokenCode.RBRACKET) {
                elements.add(parseExpression()); // Добавляем элемент списка
                if (getCurrentToken().code == TokenCode.COMMA) {
                    advance(); // Пропускаем запятую
                } else if (getCurrentToken().code != TokenCode.RBRACKET) {
                    throw new ParseException("Expected ',' or ']', found: " + getCurrentToken());
                }
            }
            advance(); // Пропускаем ']'
            this.len = elements.size();
            BlockNode elem = new BlockNode(elements, "elements");
            return elem;

        } else {
            throw new ParseException("Expected '[', found: " + getCurrentToken());
        }
    }


    private Node parseStatement() {
        if (getCurrentToken().code == TokenCode.SEMICOLON) {
            advance();
        }
        if (getCurrentToken().code == TokenCode.IF) {
            return parseIf();
        } else if (getCurrentToken().code == TokenCode.WHILE) {
            return parseWhile();
        } else if (getCurrentToken().code == TokenCode.FOR) {
            return parseFor();
        } else if (getCurrentToken().code == TokenCode.PRINT) {
            return parsePrint();
        } else if (getCurrentToken().code == TokenCode.VAR) {
            return parseDeclaration();
        } else if (getCurrentToken().code == TokenCode.RETURN) {
            if (this.scope != "global") {
                return parseReturn();
            }
            throw new ParseException("ERROR in line: " + getCurrentToken().span.lineNum + ". Return must be used inside the function.");
        } else if (getCurrentToken().code == TokenCode.FUNC) {
            return parseFunction();
        } else if (getCurrentToken().code == TokenCode.READ_REAL ||
                getCurrentToken().code == TokenCode.READ_INT ||
                getCurrentToken().code == TokenCode.READ_STRING) {
            return parsePrimary();
        } else if (getCurrentToken().code == TokenCode.INTEGER_LITERAL ||
                getCurrentToken().code == TokenCode.REAL_LITERAL ||
                getCurrentToken().code == TokenCode.BOOLEAN_LITERAL ||
                getCurrentToken().code == TokenCode.STRING_LITERAL ||
                getCurrentToken().code == TokenCode.IDENTIFIER) {
            return parseExpression();
        }
        throw new ParseException("Unexpected statement type: " + getCurrentToken().code + " in line " + getCurrentToken().span.lineNum);
    }
}


class Variable {
    private String name;
    private Object value;
    private String scope;

    public Variable(String name, Object value, String scope) {
        this.name = name;
        this.value = value;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "Variable{name='" + name + "', value=" + value + ", scope='" + scope + "'}";
    }
}

class Environment {
    private String name;
    private String scopeType;
    private Map<String, Map<String, Variable>> scopedVariables;

    public Environment(String name, String scopeType) {
        this.name = name;
        this.scopeType = scopeType;
        this.scopedVariables = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void addVariable(String name, Object value, String scopeType) {
        scopedVariables.putIfAbsent(scopeType, new HashMap<>());
        Map<String, Variable> scopeVars = scopedVariables.get(scopeType);
        if (scopeVars.containsKey(name)) {
            throw new RuntimeException("Переменная " + name + " уже существует в области " + scopeType + ".");
        }
        scopeVars.put(name, new Variable(name, value, scopeType));
    }

    public void updateVariable(String name, Object value, String scopeType) {
        Map<String, Variable> scopeVars = scopedVariables.get(scopeType);
        if (scopeVars == null || !scopeVars.containsKey(name)) {
            throw new RuntimeException("Переменная " + name + " не найдена в области " + scopeType + ".");
        }
        scopeVars.get(name).setValue(value);
    }

    public Variable getVariable(String name, String scopeType) {
        Map<String, Variable> scopeVars = scopedVariables.get(scopeType);
        if (scopeVars == null || !scopeVars.containsKey(name)) {
            throw new RuntimeException("Переменная " + name + " не найдена в области " + scopeType + ".");
        }
        return scopeVars.get(name);
    }

    private String determineType(Object value) {
        if (value instanceof Integer) {
            return "int";
        } else if (value instanceof Double || value instanceof Float) {
            return "real";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof String) {
            return "string";
        }
        throw new RuntimeException("Unsupported value type: " + value.getClass());
    }

    public Map<String, Variable> getVariablesInScope(String scopeType) {
        return scopedVariables.getOrDefault(scopeType, new HashMap<>());
    }
}


class LexerException extends RuntimeException {
    public LexerException(String message) {
        super(message);
    }
}

class Span {
    public long lineNum;
    public int posBegin, posEnd;

    public Span(long lineNum, int posBegin, int posEnd) {
        this.lineNum = lineNum;
        this.posBegin = posBegin;
        this.posEnd = posEnd;
    }

    public int getPosBegin() {
        return posBegin + 1;
    }

    public int getPosEnd() {
        return posEnd + 1;
    }
}

class Token {
    protected Span span;        // диапазон токена
    protected TokenCode code;   // код токена (из TokenCode)


    public Token(TokenCode code, Span span) {
        this.code = code;
        this.span = span;
    }


    public void print() {
        System.out.print(code + " ");
    }

    public void fullPrint() {
        System.out.println(code + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
    }

}


class Identifier extends Token {
    public String identifier;

    public Identifier(String identifier, Span span) {
        super(TokenCode.IDENTIFIER, span);
        this.identifier = identifier;
    }

    public void print() {
        System.out.print(code + "[" + this.identifier + "] ");
    }

    public void fullPrint() {
        System.out.println(code + " [Val: " + this.identifier + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
    }
}


class RealToken extends Token {
    public double value;

    public RealToken(double value, Span span) {
        super(TokenCode.REAL_LITERAL, span);
        this.value = value;
    }

    public void print() {
        System.out.print(code + "[" + this.value + "] ");
    }

    public void fullPrint() {
        System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
    }
}

class BooleanToken extends Token {
    public boolean value;

    public BooleanToken(boolean value, Span span) {
        super(TokenCode.BOOLEAN_LITERAL, span);
        this.value = value;
    }


    public void print() {
        System.out.print(code + "[" + this.value + "] ");
    }

    public void fullPrint() {
        System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
    }
}

class IntegerToken extends Token {
    public int value;

    public IntegerToken(int value, Span span) {
        super(TokenCode.INTEGER_LITERAL, span);
        this.value = value;
    }

    public void print() {
        System.out.print(code + "[" + this.value + "] ");
    }

    public void fullPrint() {
        System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
    }
}


class StringToken extends Token {
    public String value;

    public StringToken(String value, Span span) {
        super(TokenCode.STRING_LITERAL, span);
        this.value = value;
    }


    public void print() {
        System.out.print(code + "[" + this.value + "] ");
    }

    public void fullPrint() {
        System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
    }
}



class Optimizer {
    public static boolean flag;
    private SymbolTable symbolTable;

    public Optimizer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.flag = false;
    }

    public Node optimize(Node ast) {
        do {
            flag = false;
            simplifyConstantExpressions(ast);

            removeUnusedVariables(ast);

        } while (flag);
//        ast = removeUnusedVariables(ast);
        return ast;
    }
    public void updateOperands(Node node) {
        if (node instanceof ExpressionNode) {
            ExpressionNode exprNode = (ExpressionNode) node;
            List<Node> children = exprNode.getChildren();
            if (children.size() >= 2) {
                exprNode.setLeft(children.get(0));
                exprNode.setRight(children.get(1));
            }
        }
    }
    private Node simplifyConstantExpressions(Node node) {
        for (int i = 0; i < node.getChildren().size(); i++) {
            Node child = node.getChildren().get(i);
            if (child != null) {
                Node optimizedChild = simplifyConstantExpressions(child);
                if (optimizedChild instanceof ExpressionNode exprNode && exprNode.isConstant()) {
                    node.getChildren().set(i, exprNode.evaluate());
                } else {

                    node.getChildren().set(i, optimizedChild);
                }
                updateOperands(node);
            }
        }
        return node;
    }

    private Node removeUnusedVariables(Node node) {

        Set<String> usedVariables = new HashSet<>();
        markUsedVariables(node, usedVariables);


        return pruneUnusedVariables(node, usedVariables);
    }

    private void markUsedVariables(Node node, Set<String> usedVariables) {
        if (node == null) return;


        if (node instanceof IdentifierNode identifierNode) {
            usedVariables.add(identifierNode.getName());
        }

    }

    private Node pruneUnusedVariables(Node node, Set<String> usedVariables) {
        if (node == null) return null;


        try {
            if (node instanceof VariableDeclarationNode declarationNode) {
                String variableName = declarationNode.getName().getName();
                if (this.symbolTable.getSymbolUseNum(variableName + "_global") <= 0) {
                    return null;
                }
            }
        } catch (NullPointerException e) {
            System.out.println();
        }



        List<Node> children = new ArrayList<>();
        for (Node child : node.getChildren()) {
            Node prunedChild = pruneUnusedVariables(child, usedVariables);
            if (prunedChild != null) {
                children.add(prunedChild);
            }
        }

        node.children = children;
        return node;
    }
}

enum TokenCode {
    // Ключевые слова
    VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC,
    LOOP, END, IS, THEN, IN, READ_INT, READ_REAL, READ_STRING, LENGTH,

    // Булевые литералы и специальное значение
    TRUE, FALSE, EMPTY,

    // Типы данных
    INT, REAL, BOOL, STRING,

    // Операторы
    ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
    AND, OR, XOR, NOT,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL, IMPLICATION,

    // Специальные операторы
    DOT, LBRACKET, RBRACKET, LPAREN, RPAREN, TWO_DOT, QUOTE, DOUBLE_QUOTE,

    // Литералы
    INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, TUPLE_LITERAL, ARRAY_LITERAL,

    // Разделители
    SEMICOLON, COMMA, LBRACE, RBRACE, COLON,

    COMMENT,

    // Идентификатор
    IDENTIFIER,

    // Конец файла
    EOF
}


class Lexer {
    private final String code; // Код который мы чекаем
    private int lineNum = 1; // На какой сейчас строчке стоит поинт (для span)
    private int currentCharNum = 0; // На каком индексе сейчас находимся
    private List<Token> tokenList;
    List<Character> symbolList = Arrays.asList('(', ')', ',', '+', '/', '-', '=', ':', ';', '>', '<', '[', ']', '{', '}', '.');

    public Lexer (String code) {
        this.code = code;
        this.tokenList = new ArrayList<>();
    }

    private void spacesDelete() {
        while (this.currentCharNum < this.code.length() && Character.isWhitespace(this.code.charAt(this.currentCharNum))) {
            if (this.code.charAt(this.currentCharNum) == '\n') {
                lineNum++;
                System.out.println();
            }

            currentCharNum++;
        }
    }

    private void findWordEnd() {
        while(this.currentCharNum < this.code.length() && !Character.isWhitespace(this.code.charAt(this.currentCharNum)) && (!specSymbolCheck(this.currentCharNum))) {
            currentCharNum++;
        }
    }

    private void findLineEnd() {
        while(this.currentCharNum < this.code.length() && this.code.charAt(this.currentCharNum) != '\n') {
            currentCharNum++;
        }
    }

    public int findWordStart() {
        int charNum = currentCharNum;
        while(charNum > 0 && !Character.isWhitespace(this.code.charAt(charNum)) && (!specSymbolCheck(charNum))) {
            charNum--;
        }
        return charNum;
    }


    private boolean digitCheck(Span span) {
        for (int i = span.posBegin; i < span.posEnd; i++) {
            if (!(Character.isDigit(this.code.charAt(i)) || this.code.charAt(i) == '.' || this.code.charAt(i) == ',')) {
                return false;
            }
        }
        return true;
    }

    private int numberDelimCounter(Span span) {
        int delNum = 0;
        for (int i = span.posBegin; i < span.posEnd; i++) {
            if (this.code.charAt(i) == '.' || this.code.charAt(i) == ',') delNum++;
        }
        return delNum;
    }

    private Token scanNumber(Span span) {

        if (numberDelimCounter(span) > 1) {
            throw new LexerException("Invalid real number format at line " + this.lineNum);
        } else if (numberDelimCounter(span) == 1) {
            double value = Double.valueOf(this.code.substring(span.posBegin, span.posEnd).replace(',', '.'));
            return new RealToken(value, span);
        } else {
            int value = Integer.valueOf(this.code.substring(span.posBegin, span.posEnd).replace(',', '.'));
            return new IntegerToken(value, span);
        }
    }

    private boolean specSymbolCheck(int num) {
        return symbolList.contains(this.code.charAt(num));
    }

    private boolean commentCheck(int num) {
        if (num+1 < this.code.length()) {
            return this.code.substring(num, num + 2).equals("//");
        }
        return false;
    }

    private Token stringTokenFind() {
        this.currentCharNum++;
        int startNum = this.currentCharNum;
        while (code.charAt(this.currentCharNum) != '"') {
            if (code.charAt(this.currentCharNum) == '\n') {
                lineNum++;
                System.out.println();
            }
            currentCharNum++;
        }
        String value = code.substring(startNum, currentCharNum);
        Span span = new Span(lineNum, startNum, currentCharNum);
        currentCharNum++;
        return new StringToken(value, span);
    }




    public Token tokenFind() {
        spacesDelete();

        if (this.currentCharNum >= code.length() ) {
            return new Token(TokenCode.EOF, new Span(lineNum, currentCharNum, currentCharNum));
        }

        if (commentCheck(this.currentCharNum)) {
            findLineEnd();
            return new Token(TokenCode.COMMENT, new Span(lineNum, currentCharNum, currentCharNum));
        }

        int firstCharNum = currentCharNum;

        if (code.charAt(this.currentCharNum) == '"') {
            return stringTokenFind();
        }

        if (specSymbolCheck(this.currentCharNum)) {
            int offset = 1;
            if (this.code.charAt(this.currentCharNum) == '.' && digitCheck(new Span(lineNum, this.currentCharNum+1, this.currentCharNum+2))) {
                if (digitCheck(new Span(lineNum, findWordStart()-1, this.currentCharNum)) && (this.code.charAt(this.currentCharNum+1) != '.')){
                    tokenList.remove(tokenList.size()-1);
                    int charNumNew = findWordStart()-1;
                    this.currentCharNum++;
                    findWordEnd();
                    return scanNumber(new Span(lineNum, charNumNew, this.currentCharNum));
                }
            }
            if (this.currentCharNum+1 < code.length() && specSymbolCheck(this.currentCharNum+1)) offset++;
            TokenCode token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
            if (token == TokenCode.IDENTIFIER) {
                offset = 1;
                token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
            }
            currentCharNum += offset;
            return new Token(token, new Span(lineNum, this.currentCharNum, this.currentCharNum+offset));
        }

        findWordEnd();

        Span span = new Span(lineNum, firstCharNum, currentCharNum);

        if (digitCheck(span)) {
            return scanNumber(span);
        }
        String word = this.code.substring(span.posBegin, span.posEnd).toLowerCase();
        TokenCode token = getKeywordTokenCode(word);

        if (token == TokenCode.IDENTIFIER) {
            return new Identifier(this.code.substring(span.posBegin, span.posEnd), span);
        }

        if (token == TokenCode.TRUE) {
            return new BooleanToken(true, span);
        }

        if (token == TokenCode.FALSE) {
            return new BooleanToken(false, span);
        }

        return new Token(token, span);

    }

    public List<Token> start() {
        tokenList = new ArrayList<>();
        while (true) {
            Token token = tokenFind();
            tokenList.add(token);
            token.print();
            if (token.code == TokenCode.EOF) {
                return tokenList;
            }
        }
    }


    private TokenCode getKeywordTokenCode(String str) {
        if (str.length() == 1) {
            if (str.equals("<")){
                return TokenCode.LESS;
            }
            else if (str.equals(">")){
                return TokenCode.GREATER;
            }
            else if (str.equals(":")){
                return TokenCode.COLON;
            }
            else if (str.equals("=")){
                return TokenCode.EQUAL;
            }
            else if (str.equals("*")){
                return TokenCode.MULTIPLY;
            }
            else if (str.equals("/")){
                return TokenCode.DIVIDE;
            }
            else if (str.equals("-")){
                return TokenCode.MINUS;
            }
            else if (str.equals("+")){
                return TokenCode.PLUS;
            }
            else if (str.equals("(")){
                return TokenCode.LPAREN;
            }
            else if (str.equals(")")){
                return TokenCode.RPAREN;
            }
            else if (str.equals("[")){
                return TokenCode.LBRACKET;
            }
            else if (str.equals("]")){
                return TokenCode.RBRACKET;
            }
            else if (str.equals("{")){
                return TokenCode.LBRACE;
            }
            else if (str.equals("}")){
                return TokenCode.RBRACE;
            }
            else if (str.equals(".")){
                return TokenCode.DOT;
            }
            else if (str.equals(",")){
                return TokenCode.COMMA;
            }
            else if (str.equals(";")){
                return TokenCode.SEMICOLON;
            }
            else if (str.equals("'")){
                return TokenCode.QUOTE;
            }
            else if (str.equals("\"")){
                return TokenCode.DOUBLE_QUOTE;
            }

        }
        if (str.length() == 2) {
            if (str.equals("<=")){
                return TokenCode.LESS_EQUAL;
            }
            else if (str.equals("..")){
                return TokenCode.TWO_DOT;
            }
            else if (str.equals(">=")){
                return TokenCode.GREATER_EQUAL;
            }
            else if (str.equals("=>")){
                return TokenCode.IMPLICATION;
            }
            else if (str.equals(":=")){
                return TokenCode.ASSIGN;
            }
            else if (str.equals("/=")){
                return TokenCode.NOT_EQUAL;
            }
            else if (str.equals("or")){
                return TokenCode.OR;
            }
            else if (str.equals("if")){
                return TokenCode.IF;
            }
            else if (str.equals("in")){
                return TokenCode.IN;
            }
            else if (str.equals("is")){
                return TokenCode.IS;
            } else if (str.equals("//")){
                return TokenCode.COMMENT;
            }
        }
        if (str.length() == 3) {
            if (str.equals("and")) {
                return TokenCode.AND;
            }
            else if (str.equals("for")) {
                return TokenCode.FOR;
            }
            else if (str.equals("xor")) {
                return TokenCode.XOR;
            }
            else if (str.equals("not")) {
                return TokenCode.NOT;
            }
            else if (str.equals("int")) {
                return TokenCode.INT;
            }
            else if (str.equals("var")) {
                return TokenCode.VAR;
            }
            else if (str.equals("end")) {
                return TokenCode.END;
            }
        }
        if (str.length() == 4) {
            if (str.equals("bool")) {
                return TokenCode.BOOL;
            }
            else if (str.equals("real")) {
                return TokenCode.REAL;
            }
            else if (str.equals("true")) {
                return TokenCode.TRUE;
            }
            else if (str.equals("else")) {
                return TokenCode.ELSE;
            }
            else if (str.equals("func")) {
                return TokenCode.FUNC;
            }
            else if (str.equals("loop")) {
                return TokenCode.LOOP;
            }
            else if (str.equals("then")) {
                return TokenCode.THEN;
            }
        }
        if (str.length() == 5) {
            if (str.equals("false")) {
                return TokenCode.FALSE;
            }
            else if (str.equals("empty")) {
                return TokenCode.EMPTY;
            }
            else if (str.equals("print")) {
                return TokenCode.PRINT;
            }
            else if (str.equals("while")) {
                return TokenCode.WHILE;
            }
        }
        if (str.length() == 6) {
            if (str.equals("string")) {
                return TokenCode.STRING;
            }
            else if (str.equals("return")) {
                return TokenCode.RETURN;
            }
            else if (str.equals("length")) {
                return TokenCode.LENGTH;
            }
        }
        if (str.length() == 7) {
            if (str.equals("readint")) {
                return TokenCode.READ_INT;
            }
        }
        if (str.length() == 8) {
            if (str.equals("readreal")) {
                return TokenCode.READ_REAL;
            }
        }
        if (str.length() == 10) {
            if (str.equals("readstring")) {
                return TokenCode.READ_STRING;
            }
        }
        return TokenCode.IDENTIFIER;
    }

    public static void printTree(Node node, int depth) {
        if (node == null) return;


        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }


        System.out.println(node.getClass().getSimpleName());


        for (Node child : node.getChildren()) {
            printTree(child, depth + 1);
        }
    }

    public static void main(String[] args) {
        // Путь к файлу

        for (int i = 0; i <= 0; i++) {
            String filePath = "test/test" + i + ".d";

            System.out.println();
            System.out.println();
            System.out.println("-----------------  " + "test" + i + ".d" + "  --------------------");
            System.out.println();
            System.out.println();

            try {
                String str = new String(Files.readAllBytes(Paths.get(filePath)));


                Lexer lexer = new Lexer(str);
                List<Token> tokenList = lexer.start();
                System.out.println();
                System.out.println();
                System.out.println();
                Parser parser = new Parser(tokenList);
                System.out.println(123);
                ProgramNode ast = parser.parseProgram();

                SymbolTable symbolTable = parser.getSymbolTable();
                Optimizer optimizer = new Optimizer(symbolTable);
                optimizer.optimize(ast);
                System.out.println(ast);
                ast.printTree("", true);// Метод для парсинга


                Environment init = new Environment("global", "global");
                Interpreter interpreter = new Interpreter(init);
                interpreter.interpret(ast);

            } catch (IOException e) {
                System.out.println("Ошибка чтения файла: " + e.getMessage());
            }
        }
    }
}
