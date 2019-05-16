/*
    @author George Vasios
*/

import static java.lang.System.out;
import java_cup.runtime.Symbol;

%%

%class Lexer
%unicode
%public
%final
%integer
%line
%column
%cup

%eofval{
    return createSymbol(sym.EOF);
%eofval}

%{
    private StringBuffer sb = new StringBuffer();

    private Symbol createSymbol(int type) {
        return new Symbol(type, yyline+1, yycolumn+1);
    }

    private Symbol createSymbol(int type, Object value) {
        return new Symbol(type, yyline+1, yycolumn+1, value);
    }

    /* Slightly modified version of http://www.cs.cornell.edu/courses/cs4120/2016sp/lectures/02lexing/java.flex*/
    private Symbol char_lit(String s) {
        if (s.length() == 1) {
            char x = s.charAt(0);
            return createSymbol(sym.CHARACTER_LITERAL, x);
        } else {
            throw new RuntimeException((yyline+1) + " : " + (yycolumn+1) + ": illegal character <"+ yytext()+">");
        }
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f] 
Comment        = "/*" [^*] ~"*/" | "/*" "*"+ "/"

Identifier     = [:jletter:] [:jletterdigit:]*
IntegerLiteral = 0 | [1-9][0-9]*
Exponent       = [eE][\+\-]?[0-9]+
Float1         = [0-9]+ \. [0-9]+ {Exponent}?
Float2         = \. [0-9]+ {Exponent}?
Float3         = [0-9]+ \. {Exponent}?
Float4         = [0-9]+ {Exponent}
FloatLiteral   = {Float1} | {Float2} | {Float3} | {Float4}

%state STRING, CHARACTER

%%

<YYINITIAL> {
    /* reserved keywords */
    "float"                        { return createSymbol(sym.FLOAT);      }
    "int"                          { return createSymbol(sym.INT);        }
    "char"                         { return createSymbol(sym.CHAR);       }
    "while"                        { return createSymbol(sym.WHILE);      }
    "if"                           { return createSymbol(sym.IF);         }
    "else"                         { return createSymbol(sym.ELSE);       }
    "print"                        { return createSymbol(sym.PRINT);      }
    "void"                         { return createSymbol(sym.VOID);       }
    "return"                       { return createSymbol(sym.RETURN);     }
    "break"                        { return createSymbol(sym.BREAK);      }
    "continue"                     { return createSymbol(sym.CONTINUE);   }
    "new"                          { return createSymbol(sym.NEW);        }

    /* identifiers */ 
    {Identifier}                   { return createSymbol(sym.IDENTIFIER, yytext()); }

    /* literals */
    {IntegerLiteral}               { return createSymbol(sym.INTEGER_LITERAL, Integer.valueOf(yytext()));                 }
    {FloatLiteral}                 { return createSymbol(sym.DOUBLE_LITERAL, Double.valueOf(yytext()));                   }
    \'                             { yybegin(CHARACTER); sb.setLength(0);                                                 }
    \"                             { sb.setLength(0); yybegin(STRING);                                                    }

    /* operators */
    "="                            { return createSymbol(sym.ASSIGN);                  }
    ">"                            { return createSymbol(sym.GREATERTHAN);             }
    "<"                            { return createSymbol(sym.LESSTHAN);                }
    "!="                           { return createSymbol(sym.NOTEQUAL);                }
    "<="                           { return createSymbol(sym.LESSOREQUAL);             }
    ">="                           { return createSymbol(sym.GREATEROREQUAL);          }
    "+"                            { return createSymbol(sym.PLUS);                    }
    "-"                            { return createSymbol(sym.MINUS);                   }
    "*"                            { return createSymbol(sym.TIMES);                   }
    "/"                            { return createSymbol(sym.DIVIDE);                  }
    "%"                            { return createSymbol(sym.MOD);                     }
    "=="                           { return createSymbol(sym.EQUAL);                   }
    "&&"                           { return createSymbol(sym.LOGICAL_AND);             }
    "||"                           { return createSymbol(sym.LOGICAL_OR);              }
    "!"                            { return createSymbol(sym.LOGICAL_NOT);             }

    /* separators */
    "{"                            { return createSymbol(sym.LEFT_BRACE);              }
    "}"                            { return createSymbol(sym.RIGHT_BRACE);             }
    "["                            { return createSymbol(sym.LEFT_SQUARE_BRACKET);     }
    "]"                            { return createSymbol(sym.RIGHT_SQUARE_BRACKET);    }
    ";"                            { return createSymbol(sym.SEMICOLON);               }
    "("                            { return createSymbol(sym.LPAREN);                  }
    ")"                            { return createSymbol(sym.RPAREN);                  }
    ","                            { return createSymbol(sym.COMMA);                   }

    /* comments */
    {Comment}                      {            /* do nothing */                       }

    /* whitespace */
    {WhiteSpace}                   {            /* do nothing */                       }
}

<STRING> {
    \"                             { 
                                     yybegin(YYINITIAL);
                                     return createSymbol(sym.STRING_LITERAL, sb.toString()); 
                                   }

    [^\n\r\"\\]+                   { sb.append(yytext());   }
    \\t                            { sb.append('\t');       }
    \\n                            { sb.append('\n');       }

    \\r                            { sb.append('\r');       }
    \\\"                           { sb.append('\"');       }
    \\                             { sb.append('\\');       }
}

<CHARACTER> {
    \'                             { yybegin(YYINITIAL);
                                     return char_lit(sb.toString()); 
                                   }

    "\\t"                          { sb.append('\t');       }
    "\\n"                          { sb.append('\n');       }
    "\\0"                          { sb.append('\0');       }

    [^\r\n\'\\]+                   { sb.append(yytext());   }
}

/* error fallback */
[^]                                { 
                                     throw new RuntimeException((yyline+1) + " : " + 
                                     (yycolumn+1) + ": illegal character <"+ yytext()+">"); 
                                   }
