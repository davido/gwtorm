// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

grammar Query;
options {
  language = Java;
  output = AST;
}

tokens {
  WHERE;
  ORDER;
  BY;
  AND;
  LT;
  LE;
  GT;
  GE;
  EQ;
  ID;
  PLACEHOLDER;
  COMMA;
  ASC;
  DESC;
  LIMIT;
  CONSTANT_INTEGER;
  CONSTANT_STRING;
  TRUE;
  FALSE;
}

@header {
package com.google.gwtorm.schema;
}
@members {
    public static Tree parse(final RelationModel m, final String str)
      throws QueryParseException {
      try {
        final QueryParser p = new QueryParser(
          new TokenRewriteStream(
            new QueryLexer(
              new ANTLRStringStream(str)
            )
          )
        );
        p.relationModel = m;
        return (Tree)p.query().getTree();
      } catch (QueryParseInternalException e) {
        throw new QueryParseException(e.getMessage());
      } catch (RecognitionException e) {
        throw new QueryParseException(e.getMessage());
      }
    }

    public static class Column extends CommonTree {
      private static ColumnModel resolve(Tree node, RelationModel model) {
        ColumnModel c;
        if (node.getType() == ID) {
          c = model.getField(node.getText());
        } else {
          c = resolve(node.getChild(0), model);
        }
        if (c == null) {
          throw new QueryParseInternalException("No field " + node.getText());
        }
        if (node.getType() == DOT) {
          c = resolve(node.getChild(1), c);
        }
        return c;
      }

      private static ColumnModel resolve(Tree node, ColumnModel model) {
        ColumnModel c;
        if (node.getType() == ID) {
          c = model.getField(node.getText());
        } else {
          c = resolve(node.getChild(0), model);
        }
        if (c == null) {
          throw new QueryParseInternalException("No field " + node.getText());
        }
        if (node.getType() == DOT) {
          c = resolve(node.getChild(1), c);
        }
        return c;
      }

      private final ColumnModel field;

      public Column(int ttype, Tree tree, final RelationModel relationModel) {
        field = resolve(tree, relationModel);
        token = new CommonToken(ID, field.getPathToFieldName());
      }

      public Column(final Column o, final ColumnModel f) {
        token = o.token;
        field = f;
      }

      public ColumnModel getField() {
        return field;
      }

      public Tree dupNode() {
        return new Column(this, field);
      }
    }

    private RelationModel relationModel;

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        throw new QueryParseInternalException(hdr + " " + msg);
    }
}

@lexer::header {
package com.google.gwtorm.schema;
}
@lexer::members {
    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        throw new QueryParseInternalException(hdr + " " + msg);
    }
}


query
  : where? orderBy? limit?
  ;

where
  : WHERE^ conditions
  ;

orderBy
  : ORDER^ BY! fieldSort (COMMA! fieldSort)*
  ;

fieldSort
  : field sortDirection^
  | field -> ^(ASC field)
  ;

sortDirection
  : ASC
  | DESC
  ;

limit
  : LIMIT^ limitArg
  ;

limitArg
  : PLACEHOLDER
  | CONSTANT_INTEGER
  ;

conditions
  : condition AND^ condition (AND! condition)*
  | condition
  ;

condition
  : field compare_op^ conditionValue
  ;

compare_op
 : LT
 | LE
 | GT
 | GE
 | EQ
 ;

field
  : n=qualifiedFieldName -> ID<Column>[(Tree)n.tree, relationModel]
  ;

qualifiedFieldName
  : ID (DOT^ ID)*
  ;

conditionValue
  : PLACEHOLDER
  | CONSTANT_INTEGER
  | constantBoolean
  | CONSTANT_STRING
  ;

constantBoolean
  : TRUE
  | FALSE
  ;

WHERE: 'WHERE' ;
ORDER: 'ORDER' ;
BY:    'BY'    ;
AND:   'AND'   ;
ASC:   'ASC'   ;
DESC:  'DESC'  ;
LIMIT: 'LIMIT' ;
TRUE:  'true'  ;
FALSE: 'false' ;

LT : '<'  ;
LE : '<=' ;
GT : '>'  ;
GE : '>=' ;
EQ : '='  ;

PLACEHOLDER: '?' ;
COMMA: ',' ;
DOT: '.' ;

CONSTANT_INTEGER
  : '0'
  | '1'..'9' ('0'..'9')*
  ;

CONSTANT_STRING
  : '\'' ( ~('\'') )* '\''
  ;

ID
  : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
  ;

WS
  :  ( ' ' | '\r' | '\t' | '\n' ) { $channel=HIDDEN; }
  ;
