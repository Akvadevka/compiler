import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

abstract class Node {
    // Список дочерних узлов
    protected List<Node> children = new ArrayList<>();

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

    // Метод для поиска переменной по имени
    public VariableDeclarationNode isVariable(IdentifierNode name) {
        // Применяем поиск в текущем узле
        if (!this.children.isEmpty()) {
            return searchVariable(this.children.get(0), name);
        }
        return null;
    }

    // Метод для красивого вывода дерева
    public void printTree(String indent, boolean last) {
        if (this == null) {
            return; // Предотвращаем вызов на null
        }
        System.out.print(indent);
        if (last) {
            System.out.print("└── ");
            indent += "    ";
        } else {
            System.out.print("├── ");
            indent += "│   ";
        }
        System.out.println(this); // или используйте toString() для вывода
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
        // Если текущий узел - это функция, проверяем имя
        if (current instanceof FunctionDeclarationNode) {
            FunctionDeclarationNode functionNode = (FunctionDeclarationNode) current;
            if (functionNode.getName().getName().equals(name.getName())) {
                return functionNode; // Функция найдена
            }
        }
        // Рекурсивный поиск в дочерних узлах

        for (Node child : current.getChildren()) {
            FunctionDeclarationNode result = searchFunction(child, name);
            if (result != null) {
                return result; // Если нашли, возвращаем
            }
        }

        return null; // Если не нашли
    }

    private VariableDeclarationNode searchVariable(Node current, IdentifierNode name) {
        if (current == null) {
            return null;
        }

        // Если текущий узел - это переменная, проверяем имя
        if (current instanceof VariableDeclarationNode) {
            VariableDeclarationNode variableNode = (VariableDeclarationNode) current;
            if (variableNode.getName().getName().equals(name.getName())) {
                return variableNode; // Переменная найдена
            }
        }

        // Рекурсивный поиск в дочерних узлах
        for (Node child : current.getChildren()) {
            VariableDeclarationNode result = searchVariable(child, name);
            if (result != null) {
                return result; // Если нашли, возвращаем
            }
        }

        return null; // Если не нашли
    }

    // Визуализация дерева в виде строки
    @Override
    public abstract String toString();

    public List<Node> getChildren() {
        return children;
    }
}

class ProgramNode extends Node {
    private final List<Node> statements;

    public ProgramNode() {
        this.statements = new ArrayList<>();
    }

    public void addStatement(Node statement) {
        this.statements.add(statement);
        addChild(statement); // Добавляем в дочерние узлы
    }

    @Override
    public String toString() {
        return "Program";
    }
}

class ExpressionNode extends Node {
    private final Node leftOperand;
    private final TokenCode operator;
    private final Node rightOperand;

    public ExpressionNode(Node leftOperand, TokenCode operator, Node rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;

        addChild(leftOperand);
        addChild(rightOperand);
    }

    @Override
    public String toString() {
        if (operator == null) {
            return "Identifier";
        }
        return "Expression: " + operator.toString();
    }
}

class ComparisonNode extends Node {
    private final Node leftOperand;
    private final TokenCode operator;
    private final Node rightOperand;

    public ComparisonNode(Node leftOperand, TokenCode operator, Node rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;

        addChild(leftOperand);
        addChild(rightOperand);
    }

    @Override
    public String toString() {
        return "Comparison: " + operator.toString();
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

    // Метод для печати узла (должен быть реализован в подклассах)
    @Override
    public abstract String toString();
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
        addChild(initializer); // Добавляем initializer в дочерние узлы
    }

    public IdentifierNode getName() {
        return variableName;
    }

    @Override
    public String toString() {
        return "Variable: " + variableName;
    }
}

class IdentifierNode extends Node {
    private final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Identifier: " + name;
    }

    public String getName() {
        return name;
    }
}

class BlockNode extends Node {
    private final List<Node> statements;
    private String name;

    public BlockNode(List<Node> statements, String name) {
        this.name = name;
        this.statements = statements;
        for (Node statement : statements) {
            addChild(statement); // Добавляем все узлы в дочерние
        }
    }
    public IdentifierNode getName() {
        return (IdentifierNode) this.statements.get(0);
    }
    @Override
    public String toString() {
        return this.name;
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
}


class ListNode extends VariableDeclarationNode {
    private final BlockNode elements;
    private final IdentifierNode name;

    public ListNode(BlockNode elements, IdentifierNode name) {
        super(null, null, null);
        this.elements = elements;
        this.name = name;
        addChild(name); // Добавляем все элементы в дочерние
        addChild(elements); // Добавляем все элементы в дочерние
    }

    @Override
    public String toString() {
        return "List: ";
    }
}

class DictionaryNode extends VariableDeclarationNode {
    private final BlockNode entriesBlock;
    private final IdentifierNode name;

    public DictionaryNode(BlockNode entries, IdentifierNode name) {
        super(null, null, null);
        this.name = name;
        this.entriesBlock = entries;
        addChild(name); // Добавляем BlockNode как дочерний узел
        addChild(entriesBlock); // Добавляем BlockNode как дочерний узел
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

    @Override
    public String toString() {
        return "Entry:";
    }
}



class LiteralNode extends ExpressionNode {
    private final Object value;

    public LiteralNode(Object value) {
        super(null, null, null);
        this.value = value;
    }

    @Override
    public String toString() {
        return "Literal: " + value.toString();
    }
}

class LogicalNode extends Node {
    private final Node leftOperand;
    private final TokenCode operator;
    private final Node rightOperand;

    public LogicalNode(Node leftOperand, TokenCode operator, Node rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;

        addChild(leftOperand);
        addChild(rightOperand);
    }

    @Override
    public String toString() {
        return "Logical: " + operator.toString();
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
}


class NotNode extends Node {
    private final Node operand;

    public NotNode(Node operand) {
        this.operand = operand;
        addChild(operand); // Добавляем операнд в дочерние узлы
    }

    @Override
    public String toString() {
        return "Not";
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

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
        this.program = new ProgramNode();
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

    public ProgramNode parseProgram() {
        while (current < tokens.size()) {
            if (getCurrentToken().code == TokenCode.VAR) {
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
            }
            else {
                current++;
//                throw new ParseException("Unexpected token: " + getCurrentToken().code);
            }
        }
        return program;
    }


    private VariableDeclarationNode parseDeclaration() {
        advance(); // Пропускаем 'var'
        Identifier variableName = (Identifier) getCurrentToken();
        advance(); // Пропускаем идентификатор

        if (getCurrentToken().code == TokenCode.ASSIGN) {
            advance();// Пропускаем '='
            if (getCurrentToken().code == TokenCode.LBRACKET) {
                BlockNode elem = parseList();
                return new ListNode(elem, new IdentifierNode(variableName.identifier));
            }
            else if (getCurrentToken().code == TokenCode.LBRACE) {
                BlockNode dictElem = parseDictionary();
                return new DictionaryNode(dictElem, new IdentifierNode(variableName.identifier));
            }
            Node initializer = (Node) parseExpression();
            IdentifierNode variableIdentifier = new IdentifierNode(variableName.identifier);
            return new VariableDeclarationNode(variableIdentifier, initializer, null);
        } else {
            IdentifierNode variableIdentifier = new IdentifierNode(variableName.identifier);
            return new VariableDeclarationNode(variableIdentifier, null, null);
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

            IdentifierNode init = new IdentifierNode(functionToken.identifier);
//            String functionName = functionToken.identifier;
            advance(); // Пропускаем название функции

            if (getCurrentToken().code != TokenCode.LPAREN) {
                throw new ParseException("Expected '(', found: " + getCurrentToken());
            }
            advance(); // Пропускаем '('


            List<Node > parameters = new ArrayList<>();
            if (getCurrentToken().code != TokenCode.RPAREN) {
                parameters.add(parseParameter());

                while (getCurrentToken().code == TokenCode.COMMA) {
                    advance(); // Пропускаем запятую
                    parameters.add(parseParameter());
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

                List<Node > headerL = new ArrayList<>();
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
                List<Node > headerL = new ArrayList<>();
                headerL.add(init);
                headerL.add(param);
                BlockNode headerBlock = new BlockNode(headerL, "head");

                List<Node > bodyBlock = new ArrayList<>();
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
        return new ReturnNode(expression);
    }

    private Node parseIdentifier() {
        if (getCurrentToken().code == TokenCode.IDENTIFIER) {
            Identifier identifierToken = (Identifier) getCurrentToken();
            String identifierName = identifierToken.identifier;
            advance();

            return new IdentifierNode(identifierName);
        }

        throw new ParseException("Expected identifier, found: " + getCurrentToken().code);
    }


    private ComparisonNode parseTypeCheck() {
        Node identifier = parseIdentifier();

        if (getCurrentToken().code == TokenCode.IS) {
            advance(); // Пропускаем 'is'
            Node typeNode = parseType();
            return new ComparisonNode(identifier, TokenCode.IS, typeNode);
        } else if (getCurrentToken().code == TokenCode.IN) {
            advance();
            Node typeNode = parseType();
            return new ComparisonNode(identifier, TokenCode.IN, typeNode);
        }

        throw new ParseException("Expected 'is', found: " + getCurrentToken());
    }


    private Node parseType() {
        switch (getCurrentToken().code) {
            case INT:
                advance(); // Пропускаем 'int'
                return new LiteralNode("int");
            case REAL:
                advance(); // Пропускаем 'real'
                return new LiteralNode("real");
            case STRING:
                advance(); // Пропускаем 'string'
                return new LiteralNode("string");
            case TUPLE_LITERAL:
                advance(); // Пропускаем 'tuple'
                return new LiteralNode("tuple");
            case ARRAY_LITERAL:
                advance(); // Пропускаем 'array'
                return new LiteralNode("array");
            case IDENTIFIER:
                advance(); // Пропускаем 'identifier'
                return new LiteralNode("identifier");
            case EMPTY:
                advance(); // Пропускаем 'empty'
                return new LiteralNode("empty");
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

        while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR) {
            TokenCode logicalOperator = getCurrentToken().code;
            advance(); // Пропускаем логический оператор
            Node rightOperand = parseComparison();
            comparison = new LogicalNode(comparison, logicalOperator, rightOperand);
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
                return new NotNode(innerComparison);
            } else {
                System.out.println(getCurrentToken().code);
                Node innerComparison = parseComparisonWithoutLogicalOperators();
                return new NotNode(innerComparison);
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
        if (isComparisonOperator(operator)) {
            advance();
            Node rightOperand = parseExpression();
            leftOperand = new ComparisonNode(leftOperand, operator, rightOperand);
        }


        while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR) {
            TokenCode logicalOperator = getCurrentToken().code;
            advance(); // Пропускаем логический оператор
            Node rightOperand = parseComparison();
            leftOperand = new LogicalNode(leftOperand, logicalOperator, rightOperand);
        }

        return leftOperand;
    }



    private Node parseComparisonWithoutLogicalOperators() {
        Node leftOperand = parseExpression();

        TokenCode operator = getCurrentToken().code;
        if (isComparisonOperator(operator)) {
            advance(); // Пропускаем оператор
            Node rightOperand = parseExpression();
            leftOperand = new ComparisonNode(leftOperand, operator, rightOperand);
        }

        return leftOperand;
    }

    private Node parseLogicalExpression() {
        Node leftOperand = parseComparison();

        while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR) {
            TokenCode operator = getCurrentToken().code;
            advance(); // Пропускаем логический оператор
            Node rightOperand = parseComparison();
            leftOperand = new LogicalNode(leftOperand, operator, rightOperand);
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
            Node value = parseExpression(); // Разбираем значение
            entries.add(new DictionaryEntryNode(key, value)); // Добавляем пару в список

            // Пока есть запятые, продолжаем разбор пар
            while (getCurrentToken().code == TokenCode.COMMA) {
                advance(); // Пропускаем запятую
                key = parseIdentifier(); // Разбираем следующий ключ

                if (getCurrentToken().code == TokenCode.ASSIGN) {
                    advance(); // Пропускаем ':'
                    value = parseExpression(); // Разбираем значение
                    entries.add(new DictionaryEntryNode(key, value)); // Добавляем следующую пару
                }
                else if (getCurrentToken().code == TokenCode.COMMA || getCurrentToken().code == TokenCode.RBRACE) {
                    entries.add(new DictionaryEntryNode(key, null)); // Добавляем следующую пару
                }
                else {
                    throw new ParseException("Expected ':', found: " + getCurrentToken().code);
                }
            }
        }

        if (getCurrentToken().code != TokenCode.RBRACE) {
            throw new ParseException("Expected '}', found: " + getCurrentToken());
        }

        advance(); // Пропускаем '}'

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
            return new LiteralNode(realToken.value);
        } else if (getCurrentToken().code == TokenCode.INTEGER_LITERAL) {
            IntegerToken integerToken = (IntegerToken) getCurrentToken();
            advance();
            return new LiteralNode(integerToken.value);
        } else if (getCurrentToken().code == TokenCode.STRING_LITERAL) {
            StringToken stringToken = (StringToken) getCurrentToken();
            advance();
            return new LiteralNode(stringToken.value);
        } else if (getCurrentToken().code == TokenCode.BOOLEAN_LITERAL) {
            BooleanToken booleanToken = (BooleanToken) getCurrentToken();
            advance();
            return new LiteralNode(booleanToken.value);
        } else if (getCurrentToken().code == TokenCode.READ_INT) {
            List<Node > parameters = new ArrayList<>();
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

            FunctionCall re = new FunctionCall(new IdentifierNode("Read INT"), param);
            advance();
            return re;
        } else if (getCurrentToken().code == TokenCode.LPAREN) {
            advance(); // Пропускаем '('
            Node expression = parseExpression(); // Разбор выражения в скобках
            if (getCurrentToken().code != TokenCode.RPAREN) {
                throw new ParseException("Expected ')', found: " + getCurrentToken());
            }
            advance(); // Пропускаем ')'
            return expression;
        } else if (getCurrentToken().code == TokenCode.IDENTIFIER) {
            Identifier identifierToken = (Identifier) getCurrentToken();
            if (program.isFunction(new IdentifierNode(identifierToken.identifier)) != null) {
                List<Node > parameters = new ArrayList<>();
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
            }
            advance();
            return new IdentifierNode(identifierToken.identifier);
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
            BlockNode elem = new BlockNode(elements, "elements");
            return elem;

        } else {
            throw new ParseException("Expected '[', found: " + getCurrentToken());
        }
    }


    private Node parseStatement() {
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
            return parseReturn();
        } else if (getCurrentToken().code == TokenCode.FUNC) {
            return parseFunction();
        } else if (getCurrentToken().code == TokenCode.INTEGER_LITERAL ||
                getCurrentToken().code == TokenCode.REAL_LITERAL ||
                getCurrentToken().code == TokenCode.BOOLEAN_LITERAL ||
                getCurrentToken().code == TokenCode.STRING_LITERAL ||
                getCurrentToken().code == TokenCode.IDENTIFIER) {
            return parseExpression();
        }
        throw new ParseException("Unexpected statement type: " + getCurrentToken().code);
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

    // Идентификатор
    IDENTIFIER,

    // Конец файла
    EOF
}


class Lexer {
    private final String code; // Код который мы чекаем
    private int lineNum = 1; // На какой сейчас строчке стоит поинт (для span)
    private int currentCharNum = 0; // На каком индексе сейчас находимся

    List<Character> symbolList = Arrays.asList('(', ')', ',', '+', '/', '-', '=', ':', ';', '>', '<', '[', ']', '{', '}', '.');

    public Lexer (String code) {
        this.code = code;
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

        int firstCharNum = currentCharNum;

        if (code.charAt(this.currentCharNum) == '"') {
            return stringTokenFind();
        }

        if (specSymbolCheck(this.currentCharNum)) {
            int offset = 1;
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
        List<Token> tokenList = new ArrayList<>();
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
            if (str.equals("readInt")) {
                return TokenCode.READ_INT;
            }
        }
        if (str.length() == 8) {
            if (str.equals("readReal")) {
                return TokenCode.READ_REAL;
            }
        }
        if (str.length() == 10) {
            if (str.equals("readString")) {
                return TokenCode.READ_STRING;
            }
        }
        return TokenCode.IDENTIFIER;
    }

    public static void printTree(Node node, int depth) {
        if (node == null) return;

        // Добавляем отступы
        for (int i = 0; i < depth; i++) {
            System.out.print("  "); // 2 пробела для каждого уровня
        }

        // Выводим информацию о текущем узле (можно заменить на нужный формат)
        System.out.println(node.getClass().getSimpleName());

        // Рекурсивно выводим дочерние узлы
        for (Node child : node.getChildren()) {
            printTree(child, depth + 1);
        }
    }

    public static void main(String[] args) {
        // Путь к файлу

        for (int i = 0; i <= 15; i++) {
            String filePath = "test/test" + i + ".d";

            System.out.println();
            System.out.println();
            System.out.println("-----------------  " + "test" + i + ".d" + "  --------------------");
            System.out.println();
            System.out.println();

            try {
                // Чтение содержимого файла в строку
                String str = new String(Files.readAllBytes(Paths.get(filePath)));

                // Передача строки в конструктор Lexer
                Lexer lexer = new Lexer(str);
                List<Token> tokenList = lexer.start();
                System.out.println();
                System.out.println();
                System.out.println();
                Parser parser = new Parser(tokenList); // Предположим, что у тебя есть класс Parser
                System.out.println(123);
                Node ast = parser.parseProgram();
                System.out.println(ast);// Метод для парсинга
                ast.printTree("", true);
//                printTree(ast);
//                System.out.println(ast.toString(5)); // Вывод AST


                // Ваши действия с объектом Lexer

            } catch (IOException e) {
                // Обработка ошибки, если файл не найден или произошла ошибка ввода/вывода
                System.out.println("Ошибка чтения файла: " + e.getMessage());
            }
        }
    }
}
