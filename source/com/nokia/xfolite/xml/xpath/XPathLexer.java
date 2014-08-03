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

public class XPathLexer {
	private String m_string;
	private int m_currpos;
	private int m_strsize;
	Token m_perceding_token;
	
	protected XPathLexer()
	{	
	}
	
	protected XPathLexer (String InpString)
	{
		SetInputString (InpString);	
	}
	
	protected int Position()
	{
		return m_currpos;
	}
	
	protected void SetInputString (String InpString)
	{
	m_string = InpString;
	m_currpos = 0;
	m_strsize = InpString.length();
	m_perceding_token = null;
	}	

	
// Utility functions
	private char ConsumeNextChar() throws XPathException

	{
	if (m_currpos < m_strsize)
		{
		return m_string.charAt(m_currpos++);
		}
	throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,"Unexpected end of xpath expression: " + m_string);
	}
	
	private char ConsumeNextCharSkippingWhiteSpaces() throws XPathException
	{
	SkipWhiteSpaces();
	return ConsumeNextChar();
	}
	private char Peek()
	{
	if (m_currpos < m_strsize)
		{
		return m_string.charAt(m_currpos);
		}
	else return 0;
	}
	
	private char PeekSkippingWhiteSpaces()
	{
		if (m_currpos>=m_strsize) return 0;
		
		//initialization
		int i = m_currpos;
		char c = m_string.charAt(i);
		
		//skipping white spaces
		while (i<m_strsize && isWhiteSpace(c))
		  {
			  c = m_string.charAt(++i);
		  }
		
		//dirty trick to detect '::' sequence
		if (i<m_strsize-1
			&& c==':' 
			&& m_string.charAt(i+1)==':')
			c= Token.TOK_COLONCOLON;
		
		//check end of array
		if (i < m_strsize)
			{
			return c;
			}
		else return 0;
	}
	
	
	//at the moment there is only support for english xpath expressions
	protected boolean isLetter(char c)
	{
		return (c>='a' && c<='z') || (c>='A' && c<='Z');
	}
	
	protected boolean isDigit(char c)
	{
		return c>='0' && c<='9';
	}
	
	protected static boolean isWhiteSpace(char c)
	{
		return c==0x0A ||c==0x0D ||c==' '||c=='\t';
	}

	private boolean OperatorContext()
	{
	return m_perceding_token!=null
		&& m_perceding_token.TokenType!='@'
		&& m_perceding_token.TokenType!=Token.TOK_COLONCOLON
		&& m_perceding_token.TokenType!='('
		&& m_perceding_token.TokenType!='['
		&& !Token.IsOperatorToken(m_perceding_token.TokenType);
	}
	
	void SkipWhiteSpaces() throws XPathException
	{
	while (isWhiteSpace(Peek())) ConsumeNextChar();
	}

	public static int BinarySearch(String[] table, String value)
	{
		int low = 0;
		int high = table.length-1;

		while (low <= high) {
		    int mid = (low + high) >> 1;
		    int midVal = table[mid].compareTo(value);

		    if (midVal < 0)
		    	low = mid + 1;
		    else if (midVal > 0)
		    	high = mid - 1;
		    else
			return mid; // key found
		}
		return -(low + 1);  // key not found.
	}
	
	

	
	protected Token GetNextToken() throws XPathException
	  {

		  //skip white spaces
		  SkipWhiteSpaces();
		  
		  // Create a new token and set its position
		  Token newToken = new Token();
		  newToken.Position = m_currpos;
		  
		  //Normal termination should be here
		  if (m_currpos==m_strsize)
		  {
			  m_currpos++; //to generate an exception next time it is called
			  return newToken; //return EOF token
		  }
		  
		  //read first character of token
		  char c = ConsumeNextChar();

		  //match tokens
	      switch (c)
	      {
	      case '/' :
	    	  if (Peek() == '/')
		    	{
	    		newToken.TokenType = Token.TOK_SLASHSLASH;
	    		ConsumeNextChar();
		    	}
	    	  else  newToken.TokenType = c; 
		        break;
	      
	      case '\"' :
	      case '\'' :
		    {
	        newToken.TokenType = Token.TOK_LITERAL;

	        // it will either find first matched character or EOFException will happen
	        while (ConsumeNextChar() != c);
		    }
	        break;
	        

	      case '+' :
      		{
	        newToken.TokenType = Token.TOK_ADDITIVEOP;
	        newToken.OpType = Token.OP_PLUS; 
	        }
	        break;
	      
	      case '-' :
	        newToken.TokenType = Token.TOK_ADDITIVEOP;
	        newToken.OpType = Token.OP_MINUS; 
	        break;
	      
	      case '*' :
	    	if (OperatorContext())
		    	{
		    	newToken.TokenType = Token.TOK_MULTOP;
				newToken.OpType = Token.OP_MULT;	
		    	}
	    	else
	    		{
	    		newToken.TokenType=Token.TOK_QNAME;
	    		}
	    		
	         
		    break;
		    
	      case '<' :
	        newToken.TokenType = Token.TOK_RELATIONALOP;
	        if (Peek() == '=')
	        	{
	        	newToken.OpType = Token.OP_LTE;
	        	ConsumeNextChar();
	        	}
	        else
	        	newToken.OpType = Token.OP_LT;
	        break;
	        
	      case '>' :
	        newToken.TokenType = Token.TOK_RELATIONALOP;
	        if (Peek() == '=')
	        	{
	        	newToken.OpType = Token.OP_GTE;
	        	ConsumeNextChar();
	        	}
	        else
	        	newToken.OpType = Token.OP_GT; 
	        break;
	      
	      case '=' :
	        newToken.TokenType = Token.TOK_EQUALITYOP;
	        newToken.OpType = Token.OP_EQ; 
	        break;
	      
	      case '!' :  
	    	if (Peek() == '=')
	    	{
	    		newToken.TokenType = Token.TOK_EQUALITYOP;
		        newToken.OpType = Token.OP_NEQ;
		        ConsumeNextChar();
	    	}
	    	  
	        break;
	        
	      case '|' :
	    	newToken.TokenType = c;
		    newToken.OpType = Token.OP_UNION; 
		    break;
		    
	      
		  
	      case ':' :
	    	  if (Peek() == ':')
		    	{
	    		newToken.TokenType = Token.TOK_COLONCOLON;
	    		ConsumeNextChar();
		    	}
	    	  else  newToken.TokenType = c; 
		        break;
		        
	      case '.' :
	      	 {
	    	  char lookahead = Peek();
	    	  if (lookahead == '.')
		    	{
	    		newToken.TokenType = Token.TOK_DOTDOT;
	    		ConsumeNextChar();
		    	}
	    	  else if (isDigit(lookahead))
	    	  	{
	    		newToken.TokenType = Token.TOK_NUMBER;
	    		do  
	    			{
	    			ConsumeNextChar();
	    			lookahead = Peek();
	    			}
	    		while (isDigit(lookahead));
	    	  	}
	    	  else newToken.TokenType = c;
	      	 }
		     break;

	      case '(' :
	      case ')' :
	      case '[' :
	      case ']' :
	      case '@' :
	      case ',' :
	      case '$' :
	    	  newToken.TokenType = c; 
	    	  break;
	      default:
	    	  //Number
	    	  if (isDigit(c))
	    	  {
	    		  char lookahead = Peek();
	    		  while (isDigit(lookahead))
		    		{
		    		ConsumeNextChar();
		    		lookahead = Peek();
		    		}
	    		  if (lookahead == '.')
			    	{
	    			ConsumeNextChar();
	    			lookahead = Peek();
	    			while (isDigit(lookahead))
			    		{
			    		ConsumeNextChar();
			    		lookahead = Peek();
			    		}
			    	}
	    		  newToken.TokenType = Token.TOK_NUMBER;  
	    	  }
	    	  
	      	  //QName or others
	          else if (c=='_' || isLetter(c))
	    	  {
	    		  char lookahead = Peek();
	    		  while (lookahead=='.' 
	    			  || lookahead=='-' 
	    			  || lookahead=='_'
	    			  || isLetter(lookahead) 
	    			  || isDigit(lookahead))
	    		  {
	    			ConsumeNextChar();
	    			lookahead = Peek();
	    		  }
	    		  lookahead = PeekSkippingWhiteSpaces();
	    		  if (lookahead==':') // Note: the peek implementation checks that it is not "::"
	    		  {
	    			  ConsumeNextCharSkippingWhiteSpaces();
	    			  lookahead = PeekSkippingWhiteSpaces();
	    			  if (lookahead=='_' || isLetter(lookahead)){
	    				  ConsumeNextCharSkippingWhiteSpaces();
	    				  lookahead = Peek();
	    	    		  while (lookahead=='.' 
	    	    			  || lookahead=='-' 
	    	    			  || lookahead=='_'
	    	    			  || isLetter(lookahead) 
	    	    			  || isDigit(lookahead))
	    	    		  {
	    	    			ConsumeNextChar();
	    	    			lookahead = Peek();
	    	    		  }
	    			  }
	    			  //modify QName to allow for the NCName:* test 
	    			  else if (lookahead=='*'){
	    				  ConsumeNextCharSkippingWhiteSpaces();
	    			  }
	    			  else throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,"NCName or * expected after \':\' in pos "+ m_currpos+" in xpath expression: "+m_string);
	    		  }	  
	    		  newToken.TokenType = Token.TOK_QNAME;
	    	  }
	      }

	      newToken.image = m_string.substring(newToken.Position, m_currpos);
	      
	      RefineToken(newToken);
	      
	      m_perceding_token = newToken;
	      return newToken;
	  }

	/*
	 * recognizes OperatorName, NodeType, FunctionName and AxisName tokens
	 */
	private void RefineToken(Token t)
	{
		if (t.TokenType !=Token.TOK_QNAME)
			return;
		
		if (OperatorContext())
		  {
			  int index = BinarySearch(OPERATOR_NAMES,t.image);
			  if (index>=0) //OperatorName
			  {
				  t.TokenType = OPERATOR_TOKS[index];
				  t.OpType = OPERATOR_OPS[index]; 
			  }
			  
		  }
		if (t.TokenType ==Token.TOK_QNAME)
		  {
			  char nextchar = PeekSkippingWhiteSpaces();
			  if (nextchar=='(')
			  {
				  //node type or function name
				  int index = BinarySearch(NODE_TYPES,t.image);  
				  if (index>=0) //NodeType
				  	{
					t.TokenType = Token.TOK_NODETYPE;
					t.OpType = NODE_TYPES_OPS[index] ; 
				  	}
				  else //FunctionName
					{
					t.TokenType = Token.TOK_FUNCTIONNAME;
					}		  
			  }
			  else if (nextchar==Token.TOK_COLONCOLON)
			  {
				//AxisName
				  int index = BinarySearch(AXES,t.image);  
				  if (index>=0)
				  {
					  t.TokenType = Token.TOK_AXIS_NAME;
					  t.OpType = AXES_OPS[index]; 
				  }
			  }
		  }
	}
	
//________________________TABLES____________________________
	
	private static final String[] AXES=
	{
		 "ancestor"  
		,"ancestor-or-self" 
		,"attribute"  
		,"child"  
		,"descendant"  
		,"descendant-or-self"  
		,"following"  
		,"following-sibling"  
		,"namespace"
		,"parent"  
		,"preceding"  
		,"preceding-sibling"
		,"self"
	};
	
	private static final byte[] AXES_OPS =
	{ 
		 Token.OP_REVERSE_AXIS_ANCESTOR 
		,Token.OP_REVERSE_AXIS_ANCESTOR_OR_SELF
		,Token.OP_FORWARD_AXIS_ATTRIBUTE
		,Token.OP_FORWARD_AXIS_CHILD
		,Token.OP_FORWARD_AXIS_DESCENDENT 
		,Token.OP_FORWARD_AXIS_DESCENDENT_OR_SELF
		,Token.OP_FORWARD_AXIS_FOLLOWING
		,Token.OP_FORWARD_AXIS_FOLLOWING_SIBLING
		,Token.OP_FORWARD_AXIS_NAMESPACE
		,Token.OP_REVERSE_AXIS_PARENT
		,Token.OP_REVERSE_AXIS_PRECEDING
		,Token.OP_REVERSE_AXIS_PRECEDING_SIBLING
		,Token.OP_FORWARD_AXIS_SELF		 
	};
	
	private static final String[] NODE_TYPES=
	{
		"comment" 
		,"node"
		,"processing-instruction" 
		,"text" 
		
	};
	
	private static final byte[] NODE_TYPES_OPS =
	{
		Token.OP_NT_COMMENT
		,Token.OP_NT_NODE
		,Token.OP_NT_PI
		,Token.OP_NT_TEXT 
	};
	
	private static final String[] OPERATOR_NAMES=
	{
		 "and"
		,"div"
		,"mod"
		,"or" 
	};
	
	private static final byte[] OPERATOR_OPS=
	{
		 Token.OP_AND
		,Token.OP_DIV
		,Token.OP_MOD
		,Token.OP_OR
	};
	
	private static final char[] OPERATOR_TOKS=
	{
		Token.TOK_AND
		,Token.TOK_MULTOP
		,Token.TOK_MULTOP
		,Token.TOK_OR
	};
}
