/*
 * This file is part of: Xfolite (J2ME XForms client)
 *
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
 *
 * Contact: Oskari Koskimies <oskari.koskimies@nokia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser
 * General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.nokia.xfolite.xml.xpath;

import com.nokia.xfolite.xml.dom.DOMException;

class XPathParser{
	
	Token m_token;
	XPathLexer m_lexer;
	XPathNSResolver m_resolver;
	
	protected XPathParser()
	{
		m_lexer = new XPathLexer();
	}
	
	protected ParseNode Parse (String XPathExpr, XPathNSResolver resolver) throws XPathException
	{
		m_resolver = resolver;
		m_lexer.SetInputString(XPathExpr);
		m_token = m_lexer.GetNextToken();
		ParseNode pn = XPath();
		if (m_token.TokenType!=Token.TOK_EOF)
			throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,
					"End of expression expected");
		return pn;
	}
	
	private void match(char TokType) throws XPathException
	{
	  	
	    if (m_token.TokenType == TokType)
	    	m_token = m_lexer.GetNextToken();
		else
			throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,
					"Unexpected token \'"
					+ m_token.image 
					+ "\'Xpath Parse Error at position " 
					+ m_token.Position) ;
	}

// XPath    ::=    Expr
private ParseNode XPath()  throws XPathException
{
	return Expr();
}

// Expr    ::=    OrExpr
private ParseNode Expr()  throws XPathException
{
	return OrExpr();
}


// OrExpr    ::=    AndExpr  ( "or" AndExpr )*
private ParseNode OrExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = AndExpr();
	  while (m_token.TokenType == Token.TOK_OR)
	  {
	    match(Token.TOK_OR);
	    right = AndExpr();
	    left = new ParseNode(Token.OP_OR,left,right);
	  }
	  return left;
}

// AndExpr    ::=    EqualityExpr ( "and" EqualityExpr)*
private ParseNode AndExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = EqualityExpr();
	  while (m_token.TokenType == Token.TOK_AND)
	  {
	    match(Token.TOK_AND);
	    right = EqualityExpr();
	    left = new ParseNode(Token.OP_AND,left,right);
	  }
	  return left;
}

// EqualityExpr    ::=    RelationalExpr  ( ( "=" | "!=" ) RelationalExpr)*
private ParseNode EqualityExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = RelationalExpr();
	  while (m_token.TokenType == Token.TOK_EQUALITYOP)
	  {
	    byte op = m_token.OpType;
		match(Token.TOK_EQUALITYOP);
	    right = RelationalExpr();
	    left = new ParseNode(op,left,right);
	  }
	  return left;
}

//RelationalExpr    ::=    AdditiveExpr ( ("<" | ">" | "<=" | ">=" ) AdditiveExpr)*
private ParseNode RelationalExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = AdditiveExpr();
	  while (m_token.TokenType == Token.TOK_RELATIONALOP)
	  {
		byte op = m_token.OpType;
		match(Token.TOK_RELATIONALOP);
	    right = AdditiveExpr();
	    left = new ParseNode(op,left,right);
	  }
	  return left;
}

//AdditiveExpr    ::=    MultiplicativeExpr ( ( "+" | "-" ) MultiplicativeExpr )*
private ParseNode AdditiveExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = MultiplicativeExpr();
	  while (m_token.TokenType == Token.TOK_ADDITIVEOP)
	  {
		byte op = m_token.OpType;
		match(Token.TOK_ADDITIVEOP);
	    right = MultiplicativeExpr();
	    left = new ParseNode(op,left,right);
	  }
	  return left;
}

// MultiplicativeExpr    ::=    UnaryExpr ( ( "*" | "div"| "mod") UnaryExpr )*
private ParseNode MultiplicativeExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = UnaryExpr();
	  while (m_token.TokenType == Token.TOK_MULTOP)
	  {
		byte op = m_token.OpType;
		match(Token.TOK_MULTOP);
	    right = UnaryExpr();
	    left = new ParseNode(op,left,right);
	  }
	  return left;
}

//UnaryExpr    ::=    ("+" | "-")* UnionExpr
private ParseNode UnaryExpr() throws XPathException 
{
    ParseNode first;
    ParseNode operand;
    ParseNode curr_node;
    if (m_token.TokenType == Token.TOK_ADDITIVEOP)
    {
      first = new ParseNode(m_token.OpType);
      match(Token.TOK_ADDITIVEOP);
      curr_node = first;
      while (m_token.TokenType == Token.TOK_ADDITIVEOP)
      {
    	    operand = new ParseNode(m_token.OpType);
    	    match(Token.TOK_ADDITIVEOP);
            curr_node.addChild(operand);
            curr_node = operand;
      }
      curr_node.addChild(UnionExpr());
    } else first = UnionExpr();
    return first;
}

// UnionExpr    ::=    PathExpr ( "|" PathExpr )*
private ParseNode UnionExpr()  throws XPathException
{
	  ParseNode left;
	  ParseNode right;
	  left = PathExpr();
	  if (m_token.TokenType == '|')
	  {
	    match('|');
	    right = PathExpr();
	    left = new ParseNode(Token.OP_UNION,left,right);
	  }
	  
	  while (m_token.TokenType == '|')
	  {
	    match('|');
	    left.addChild(PathExpr());
	  }
	  return left;
}


//PathExpr    ::=  (("//")? RelativePathExpr) |  ("/" RelativePathExpr?)
private ParseNode PathExpr() throws XPathException
{
	
	  ParseNode first;
      // "/" RelativePathExpr?
	  if (m_token.TokenType == '/')
	  {
	    match('/');
	    first = new ParseNode(Token.OP_SELECT_ROOT);
	    
	    // resolving ambiguity for RelativePathExpr?
	    if (m_token.TokenType == Token.TOK_QNAME
	    	||m_token.TokenType == '@'
	    	||m_token.TokenType == Token.TOK_AXIS_NAME
	    	||m_token.TokenType == Token.TOK_NODETYPE
	    	||m_token.TokenType == '.'
	    	||m_token.TokenType == Token.TOK_DOTDOT 
	    	||m_token.TokenType == '$'
	    	||m_token.TokenType == '('
	    	||m_token.TokenType == Token.TOK_NUMBER
	    	||m_token.TokenType == Token.TOK_LITERAL
	    	||m_token.TokenType == Token.TOK_FUNCTIONNAME 
	    	)
	    {
	    	first.SetSibling(RelativePathExpr());
	    }  	
	  }
	  else{ // "//" RelativePathExpr
		  if (m_token.TokenType == Token.TOK_SLASHSLASH)
		  	{
			match(Token.TOK_SLASHSLASH);
			first = new ParseNode(Token.OP_SELECT_ROOT);
			ParseNode SS_extended = new ParseNode(Token.OP_FORWARD_AXIS_DESCENDENT_OR_SELF
										, new ParseNode(Token.OP_NT_NODE));
			first.SetSibling(SS_extended);
			SS_extended.SetSibling(RelativePathExpr()) ;
		  	}
		  // RelativePathExpr
		  else
		  	{
			first = RelativePathExpr();
		  	}
		  
		  
		  
	  }
	  return first; 
	  
}


// 	RelativePathExpr    ::=    StepExpr (("/" | "//") StepExpr)*
private ParseNode RelativePathExpr()throws XPathException
{
	  ParseNode first;
	  ParseNode current_node;
	  first = StepExpr();
	  current_node = first.LastInSiblingChain(); 
	  while (m_token.TokenType == '/'
		  || m_token.TokenType == Token.TOK_SLASHSLASH)
	  {
	    if (m_token.TokenType == Token.TOK_SLASHSLASH)
	    {
	    	current_node.SetSibling(
	    			new ParseNode(Token.OP_FORWARD_AXIS_DESCENDENT_OR_SELF
	    					, new ParseNode(Token.OP_NT_NODE)));
	    	current_node = current_node.LastInSiblingChain();
	    }
		match(m_token.TokenType);
	    current_node.SetSibling(StepExpr());
	    current_node = current_node.LastInSiblingChain(); 
	    //iterate till the last sibling. this is necessary since there may be a long list of siblings due to predicates
	  }
	  return first;
}

// StepExpr ::= (PrimaryExpr | AxisSpecifier NodeTest) Predicate* | AbbreviatedStep
private ParseNode StepExpr()throws XPathException
{
	ParseNode first;
	
	// AbbreviatedStep
	if (m_token.TokenType == '.'
		||m_token.TokenType == Token.TOK_DOTDOT ){
		first = AbbreviatedStep();
	}else{
		boolean reverse_order = false;
		
		// PrimaryExpr
		if(m_token.TokenType == '$'
			||m_token.TokenType == '('
			||m_token.TokenType == Token.TOK_NUMBER
			||m_token.TokenType == Token.TOK_LITERAL
			||m_token.TokenType == Token.TOK_FUNCTIONNAME)
		{
			first = PrimaryExpr();
		
		}
		// AxisSpecifier NodeTest
		else{
			first = AxisSpecifier();
			reverse_order = Token.IsReverseAxis(first.Op());
			first.setChildren(NodeTest());
		}
		// Predicate*
		ParseNode current_node = first.LastInSiblingChain();
		while (m_token.TokenType =='[')
		{
			current_node.SetSibling(Predicate(reverse_order));
			current_node = current_node.LastInSiblingChain();
		}
		 
	}
	return first;
}

// AxisSpecifier    ::= AxisName '::'  | AbbreviatedAxisSpecifier
private ParseNode AxisSpecifier()throws XPathException
{
 if (m_token.TokenType == Token.TOK_AXIS_NAME)
 {
	 byte op = m_token.OpType;
	 match(Token.TOK_AXIS_NAME);
	 match(Token.TOK_COLONCOLON);
	 return new ParseNode(op);
 } 
 else return AbbreviatedAxisSpecifier();
}

// AbbreviatedAxisSpecifier    ::=    '@'?
private ParseNode AbbreviatedAxisSpecifier()throws XPathException
{
	if(m_token.TokenType == '@')
	{
		match('@');
		return new ParseNode(Token.OP_FORWARD_AXIS_ATTRIBUTE);
	}else
		return new ParseNode(Token.OP_FORWARD_AXIS_CHILD);
}

// NodeTest    ::=    NameTest  | NodeType '(' ')'  | 'processing-instruction' '(' Literal ')'
private ParseNode[] NodeTest()throws XPathException
{
	ParseNode[] nodearray;
	ParseNode n1=null;
	ParseNode n2=null;
	
	//NodeType '(' ')'  | 'processing-instruction' '(' Literal ')'
	if(m_token.TokenType == Token.TOK_NODETYPE)
	{
		byte op = m_token.OpType;
		match (Token.TOK_NODETYPE);
		n1 = new ParseNode(op);
		match('(');
		if (op==Token.OP_NT_PI &&
			m_token.TokenType==Token.TOK_LITERAL)
		{
			n2 = new LiteralNode(m_token.image);
			match (Token.TOK_LITERAL);
		}
		match(')');	
	}
	
	// NameTest
	else
		n1 = NameTest();
	
	if (n2==null){
		nodearray = new ParseNode[1];
	}else{
		nodearray = new ParseNode[2];
		nodearray[1] = n2;
	}
	nodearray[0] = n1;
	return nodearray;
}

// NameTest    ::=    '*'  | NCName ':' '*'  | QName 
private ParseNode NameTest() throws XPathException
{
	return QName();
}

// PrimaryExpr    ::=    VariableReference  | '(' Expr ')'  | Literal  | Number  | FunctionCall

private ParseNode PrimaryExpr() throws XPathException
{
	switch(m_token.TokenType)
	{
	case '$':
		return VariableReference();
	case '(':
		{
		match('(');
		ParseNode n = Expr();
		match(')');
		return n;
		}
	case Token.TOK_LITERAL:
		{
		String s = m_token.image;
		match(Token.TOK_LITERAL);
		return new LiteralNode(s);
		}
	case Token.TOK_NUMBER:
		{
		double d = Double.parseDouble(m_token.image);
		match(Token.TOK_NUMBER);
		return new NumberNode(d);
		}
	case Token.TOK_FUNCTIONNAME:
		return FunctionCall();
	default:
		throw new RuntimeException("Primary expression expected at: " + m_lexer.Position()) ;
	}
}

private ParseNode VariableReference() throws XPathException
{
	match('$');
	return new ParseNode(Token.OP_VARIABLE_REFERENCE,QName());
}

//  NameTest ::= '*'  | NCName ':' '*'  | QName 
//  QName ::=  (NCName ':')? NCName 
private ExpandedNameNode QName() throws XPathException
{
	String str = m_token.image;
	
	match(Token.TOK_QNAME); 
	
	return ExpandedName(str);	
}

private ExpandedNameNode ExpandedName(String s) throws XPathException
{
	String namespace_URI, prefix, localName;
	
	int i= s.indexOf(':');
	if (i>=0)
	{
		//extract prefix and name space
		prefix = s.substring(0,i).trim();
		namespace_URI = m_resolver==null?null:m_resolver.lookupNamespaceURI(prefix);
		//extract local name
		localName = s.substring(i+1).trim();
	}
	else
	{
		prefix = null;
		namespace_URI=null;
		localName = s;
	}
		
	
	
	if (prefix!=null && namespace_URI==null) {
           throw new DOMException(DOMException.NAMESPACE_ERR,"Undefined namespace for prefix "+prefix);       
    }
 	
	return new ExpandedNameNode(namespace_URI,localName);
}

// FunctionCall ::=  FunctionName '(' ( Expr ( ',' Expr )* )? ')'
private ParseNode FunctionCall() throws XPathException
{

	String FunctionName = m_token.image;
	
	
	ParseNode n = new ParseNode(Token.OP_FUNCTION_CALL,
								ExpandedName(FunctionName));
	match(Token.TOK_FUNCTIONNAME);
	match('(');
	if (m_token.TokenType!=')')
	{
		n.addChild(Expr());
		while (m_token.TokenType==',')
		{
			match(',');
			n.addChild(Expr());
		}
	}
	else if(XPathLexer.BinarySearch(XPATH_FUNCTIONS_WITH_IMPLICIT_ARGS,FunctionName)>=0)
		n.addChild(new ParseNode(Token.OP_FORWARD_AXIS_SELF,new ParseNode(Token.OP_NT_NODE)));
	
	if (FunctionName.equals("last")|| FunctionName.equals("position")|| FunctionName.equals("lang"))
		n.m_isContextIndependent=false; //last, position and lang functions are special cases and not context independent
	
	match(')');
	return n;
}
// Predicate    ::=    '[' Expr ']'
private ParseNode Predicate(boolean reverse_order) throws XPathException
{
	match('[');
	ParseNode n= new ParseNode(reverse_order? Token.OP_REVERSE_PREDICATE : Token.OP_PREDICATE,Expr());
	match(']');
	return n;
}

// AbbreviatedStep    ::=    '.'  | '..'
private ParseNode AbbreviatedStep() throws XPathException
{
	if (m_token.TokenType=='.')
	{
		match('.');
		return new ParseNode(Token.OP_FORWARD_AXIS_SELF,new ParseNode(Token.OP_NT_NODE));
	}
	else
	{
		match(Token.TOK_DOTDOT);
		return new ParseNode(Token.OP_REVERSE_AXIS_PARENT,new ParseNode(Token.OP_NT_NODE));
	}
}

private static final String[] XPATH_FUNCTIONS_WITH_IMPLICIT_ARGS=
{
	"local-name"
	,"name"
	,"namespace-uri"
	,"normalize-space"
	,"number"
	,"string"
	,"string-length"
};
}

