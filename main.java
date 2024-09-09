import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

}


class Identifier extends Token {
	private String identifier;

	public Identifier(String identifier, Span span) {
		super(TokenCode.IDENTIFIER, span);
		this.identifier = identifier;
	}

	public void print() {
		System.out.print(code + "[" + this.identifier + "] ");
	}
}


class RealToken extends Token {
	private double value;

	public RealToken(double value, Span span) {
		super(TokenCode.REAL_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}
}

class BooleanToken extends Token {
	private boolean value;

	public BooleanToken(boolean value, Span span) {
		super(TokenCode.BOOLEAN_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}
}

class IntegerToken extends Token {
	private int value;

	public IntegerToken(int value, Span span) {
		super(TokenCode.INTEGER_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}
}


class StringToken extends Token {
	private String value;

	public StringToken(String value, Span span) {
		super(TokenCode.STRING_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}
}


enum TokenCode {
	// Ключевые слова
	VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC, LOOP, END, IS, THEN, IN, READ_INT, READ_REAL, READ_STRING,

	// Булевые литералы и специальное значение
	TRUE, FALSE, EMPTY,

	// Типы данных
	INT, REAL, BOOL, STRING, VECTOR_TYPE, TUPLE_TYPE, FUNCTION_TYPE,

	// Операторы
	ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
	AND, OR, XOR, NOT,
	LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL,

	// Специальные операторы
	DOT, LBRACKET, RBRACKET, LPAREN, RPAREN,TWO_DOT, QUOTE, DOUBLE_QUOTE,

	// Литералы
	INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, TUPLE_LITERAL, ARRAY_LITERAL,

	// Разделители
	SEMICOLON, COMMA, LBRACE, RBRACE,

	// Идентификатор
	IDENTIFIER,

	// Конец файла
	EOF
}


class Lexer {

	private final String code; // Код который мы чекаем
	private int lineNum = 0; // На какой сейчас строчке стоит поинт (для span)
	private int currentCharNum = 0; // На каком индексе сейчас находимся
	List<Character> symbolList = Arrays.asList('(', ')', ',', '+', '/', '-', '=', ':', ';'); //Ахуевшие символы, которые надо проверять отдельно, т.к. они могу стоять без пробела

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
//			System.out.println(specSymbolCheck(this.currentCharNum));
//			System.out.println(code.charAt(this.currentCharNum));
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
		int startNum = this.currentCharNum;
		this.currentCharNum++;
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
			if (specSymbolCheck(this.currentCharNum+1)) offset++;
			TokenCode token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
			if (token == TokenCode.IDENTIFIER) {
				offset = 1;
				token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
			}
			currentCharNum += offset;
			return new Token(token, new Span(lineNum, this.currentCharNum, this.currentCharNum+1));
		}

		findWordEnd();

		Span span = new Span(lineNum, firstCharNum, currentCharNum);

		if (digitCheck(span)) {
			return scanNumber(span);

		}
		String word = this.code.substring(span.posBegin, span.posEnd).toLowerCase();
		TokenCode token = getKeywordTokenCode(word);

		if (token == TokenCode.IDENTIFIER) {
			return new Identifier(word, span);
		}

		if (token == TokenCode.TRUE) {
			return new BooleanToken(true, span);
		}

		if (token == TokenCode.FALSE) {
			return new BooleanToken(false, span);
		}

		return new Token(token, span);

	}

	public void start() {
		while (true) {
			if (currentCharNum >= code.length()) {
				return;
			}
			tokenFind().print();
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

	public static void main(String[] args) {
		// Путь к файлу
		String filePath = "test.d";

		try {
			// Чтение содержимого файла в строку
			String str = new String(Files.readAllBytes(Paths.get(filePath)));

			// Передача строки в конструктор Lexer
			Lexer lexer = new Lexer(str);
			lexer.start();

			// Ваши действия с объектом Lexer

		} catch (IOException e) {
			// Обработка ошибки, если файл не найден или произошла ошибка ввода/вывода
			System.out.println("Ошибка чтения файла: " + e.getMessage());
		}

	}

}