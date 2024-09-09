import java.io.*;

	class Span {
		public long lineNum;
		public int posBegin, posEnd;

		public Span(long lineNum, int posBegin, int posEnd) {
			this.lineNum = lineNum;
			this.posBegin = posBegin;
			this.posEnd = posEnd;
		}
	}

	// Общая структура токена
	class Token {
		protected Span span;        // диапазон токена
		protected TokenCode code;         // код токена (из TokenCode)
		protected int intValue;     // для целых чисел
		protected long realValue;   // для вещественных чисел
		protected String id;        // для идентификаторов
		protected String strValue;  // для строковых значений


		public Token(TokenCode code, Span span) {
			this.code = code;
			this.span = span;
		}

		
		public Token(TokenCode code, int intValue, Span span) {
			this(code, span);
			this.intValue = intValue;
		}

		
		public Token(TokenCode code, long realValue, Span span) {
			this(code, span);
			this.realValue = realValue;
		}

	
		public Token(TokenCode code, String id, Span span) {
			this(code, span);
			this.id = id;
		}

		
		public Token(TokenCode code, String strValue, Span span, boolean isString) {
			this(code, span);
			this.strValue = strValue;
		}
	}

	
	class Identifier extends Token {
		public Identifier(String id, Span span) {
			super(TokenCode.IDENTIFIER, id, span); 
		}
	}

	
	class Real extends Token {
		public Real(long realValue, Span span) {
			super(TokenCode.REAL_LITERAL, realValue, span);
		}
	}

	class IntegerToken extends Token {
		public IntegerToken(int intValue, Span span) {
			super(TokenCode.INTEGER_LITERAL, intValue, span);
	}

	class StringToken extends Token {
		public StringToken(String strValue, Span span) {
			super(TokenCode.STRING_LITERAL, strValue, span, true); 
		}

		@Override
		public String toString() {
			return "StringToken{" + "strValue='" + strValue + '\'' + ", span=" + span + '}';
		}
	}

	// Перечисление типов токенов
	enum TokenCode {
		// Ключевые слова
		VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC, LOOP, END, IS, THEN, IN,

		// Булевые литералы и специальное значение
		TRUE, FALSE, EMPTY,

		// Типы данных
		INT, REAL, BOOL, STRING, VECTOR_TYPE, TUPLE_TYPE, FUNCTION_TYPE,

		// Операторы
		ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
		AND, OR, XOR, NOT,
		LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL,

		// Специальные операторы
		DOT, LBRACKET, RBRACKET, LPAREN, RPAREN,

		// Литералы
		INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, TUPLE_LITERAL, ARRAY_LITERAL,

		// Разделители
		SEMICOLON, COMMA, LBRACE, RBRACE,

		//Bдентификатор
		IDENTIFIER,

		// Конец файла
		EOF
	}

