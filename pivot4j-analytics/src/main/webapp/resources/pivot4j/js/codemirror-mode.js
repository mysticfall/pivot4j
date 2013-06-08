CodeMirror
		.defineMode(
				"mdx",
				function(config, parserConfig) {
					var keywords = parserConfig.keywords, functions = parserConfig.functions, properties = parserConfig.properties, multiLineStrings = parserConfig.multiLineStrings;
					var isOperatorChar = /[+\-*&%=<>!?:\/|]/;
					function chain(stream, state, f) {
						state.tokenize = f;
						return f(stream, state);
					}

					function ret(tp, style) {
						type = tp;
						return style;
					}

					function tokenBase(stream, state) {
						var ch = stream.next();
						// start of string?
						if (ch == '"' || ch == "'")
							return chain(stream, state, tokenString(ch));
						// is it one of the special signs {}().,;? Seperator?
						else if (/[{}\(\),;\.]/.test(ch))
							return ret("bracket", "bracket");
						// start of a number value?
						else if (/\d/.test(ch)) {
							stream.eatWhile(/[\w\.]/);
							return ret("number", "number");
						}
						// multi line comment or simple operator?
						else if (ch == "/") {
							if (stream.eat("*")) {
								return chain(stream, state, tokenComment);
							} else {
								stream.eatWhile(isOperatorChar);
								return ret("operator", "operator");
							}
						}
						// single line comment or simple operator?
						else if (ch == "-") {
							if (stream.eat("-")) {
								stream.skipToEnd();
								return ret("comment", "comment");
							} else {
								stream.eatWhile(isOperatorChar);
								return ret("operator", "operator");
							}
						}
						// is it an identifier?
						else if (ch == "[") {
							return chain(stream, state, tokenString("]"));
						}
						// is it a operator?
						else if (isOperatorChar.test(ch)) {
							stream.eatWhile(isOperatorChar);
							return ret("operator", "operator");
						} else {
							// get the whole word
							stream.eatWhile(/[\w\$_]/);
							// is it one of the listed keywords?
							if (keywords
									&& keywords.propertyIsEnumerable(stream
											.current().toLowerCase()))
								return ret("keyword", "keyword");
							// is it one of the listed functions?
							if (functions
									&& functions.propertyIsEnumerable(stream
											.current().toLowerCase()))
								return ret("keyword", "builtin");
							// is it one of the listed types?
							if (properties
									&& properties.propertyIsEnumerable(stream
											.current().toLowerCase()))
								return ret("keyword", "variable-2");
							// default: just a "variable"
							return ret("word", "variable");
						}
					}

					function tokenString(quote) {
						return function(stream, state) {
							var escaped = false, next, end = false;
							while ((next = stream.next()) != null) {
								if (next == quote && !escaped) {
									end = true;
									break;
								}
								escaped = !escaped && next == "\\";
							}
							if (end || !(escaped || multiLineStrings))
								state.tokenize = tokenBase;
							return ret("string", "qualifier");
						};
					}

					function tokenComment(stream, state) {
						var maybeEnd = false, ch;
						while (ch = stream.next()) {
							if (ch == "/" && maybeEnd) {
								state.tokenize = tokenBase;
								break;
							}
							maybeEnd = (ch == "*");
						}
						return ret("comment", "comment");
					}

					// Interface
					return {
						startState : function(basecolumn) {
							return {
								tokenize : tokenBase,
								startOfLine : true
							};
						},

						token : function(stream, state) {
							if (stream.eatSpace())
								return null;
							var style = state.tokenize(stream, state);
							return style;
						}
					};
				});

(function() {
	function keywords(str) {
		var obj = {}, words = str.split(" ");
		for ( var i = 0; i < words.length; ++i)
			obj[words[i]] = true;
		return obj;
	}

	var mdxKeywords = "select from on columns rows where with not in properties";

	var mdxFunctions = "hierarchize crossjoin union filter iif format exists except "
			+ "dateDiff dateAdd bottomCount bottomSum";

	var mdxProperties = "null children descendants members firstChild firstSibling lastChild "
			+ "lastSibling parent name uniqueName nextMember hierarchy dimension currentMember "
			+ "defaultMember allMembers";

	CodeMirror.defineMIME("text/x-mdx", {
		name : "mdx",
		keywords : keywords(mdxKeywords),
		functions : keywords(mdxFunctions),
		properties : keywords(mdxProperties)
	});
}());

CodeMirror
		.defineMode(
				"freemarker",
				function(config, parserConfig) {
					var keywords = parserConfig.keywords, functions = parserConfig.functions, properties = parserConfig.properties, multiLineStrings = parserConfig.multiLineStrings;
					var isOperatorChar = /[+\-*&%=<>!?:\/|]/;
					function chain(stream, state, f) {
						state.tokenize = f;
						return f(stream, state);
					}

					function ret(tp, style) {
						type = tp;
						return style;
					}

					function tokenBase(stream, state) {
						var ch = stream.next();
						// start of string?
						if (ch == '"' || ch == "'")
							return chain(stream, state, tokenString(ch));
						// is it one of the special signs {}().,;? Seperator?
						else if (/[<>\[\]{}\(\),;\.]/.test(ch))
							return ret("bracket", "bracket");
						// start of a number value?
						else if (/\d/.test(ch)) {
							stream.eatWhile(/[\w\.]/);
							return ret("number", "number");
						}
						// is it a operator?
						else if (isOperatorChar.test(ch)) {
							stream.eatWhile(isOperatorChar);
							return ret("operator", "operator");
						} else {
							// get the whole word
							stream.eatWhile(/[\w\$_]/);
							// is it one of the listed keywords?
							if (keywords
									&& keywords.propertyIsEnumerable(stream
											.current().toLowerCase()))
								return ret("keyword", "keyword");
							// is it one of the listed functions?
							if (functions
									&& functions.propertyIsEnumerable(stream
											.current().toLowerCase()))
								return ret("keyword", "builtin");
							// is it one of the listed types?
							if (properties
									&& properties.propertyIsEnumerable(stream
											.current().toLowerCase()))
								return ret("keyword", "variable-2");
							// default: just a "variable"
							return ret("word", "variable");
						}
					}

					function tokenString(quote) {
						return function(stream, state) {
							var escaped = false, next, end = false;
							while ((next = stream.next()) != null) {
								if (next == quote && !escaped) {
									end = true;
									break;
								}
								escaped = !escaped && next == "\\";
							}
							if (end || !(escaped || multiLineStrings))
								state.tokenize = tokenBase;
							return ret("string", "qualifier");
						};
					}

					// Interface
					return {
						startState : function(basecolumn) {
							return {
								tokenize : tokenBase,
								startOfLine : true
							};
						},

						token : function(stream, state) {
							if (stream.eatSpace())
								return null;
							var style = state.tokenize(stream, state);
							return style;
						}
					};
				});

(function() {
	function keywords(str) {
		var obj = {}, words = str.split(" ");
		for ( var i = 0; i < words.length; ++i)
			obj[words[i]] = true;
		return obj;
	}

	var mdxKeywords = "#if #list #assign #else as";

	var mdxFunctions = "html cap_first lower_case upper_case trim size int repeat";

	var mdxProperties = "member level hierarchy model cube catalog locale dimension position "
			+ "columnPosition rowPosition cell cellType property roleName memberUtils attributes "
			+ "axis rowIndex rowIndex rowSpan columnSpan rowCount columnCount rowHeaderCount columnHeaderCount";

	CodeMirror.defineMIME("text/x-freemarker", {
		name : "freemarker",
		keywords : keywords(mdxKeywords),
		functions : keywords(mdxFunctions),
		properties : keywords(mdxProperties)
	});
}());

(function() {
	CodeMirror.pivot4jHint = function(editor, options) {
		var keywords = [];

		return scriptHint(editor, keywords, function(e, cur) {
			return e.getTokenAt(cur);
		}, options);
	};

	function Pos(line, ch) {
		if (!(this instanceof Pos))
			return new Pos(line, ch);
		this.line = line;
		this.ch = ch;
	}

	function forEach(arr, f) {
		for ( var i = 0, e = arr.length; i < e; ++i) {
			f(arr[i]);
		}
	}

	function arrayContains(arr, item) {
		if (!Array.prototype.indexOf) {
			var i = arr.length;
			while (i--) {
				if (arr[i] === item) {
					return true;
				}
			}
			return false;
		}
		return arr.indexOf(item) != -1;
	}

	function scriptHint(editor, keywords, getToken, options) {
		var result = [];

		// Find the token at the cursor
		var cur = editor.getCursor();
		var token = getToken(editor, cur);

		token.state = CodeMirror.innerMode(editor.getMode(), token.state).state;

		var ch = token.string.charAt(0);

		if (ch == "<" || ch == "/") {
			if (ch == "<") {
				result = [ "<#if ", "<#list ", "<#assign ", "<#else>" ];
			} else if (getToken(editor, Pos(cur.line, token.start)).string
					.charAt(0) == "<") {
				result = [ "/#if>", "/#list>", "/#assign>" ];
			}
		} else {
			var tprop = token;

			// If it's not a 'word-style' token, ignore the token.
			if (!/^[\w$_]*$/.test(token.string)) {
				token = tprop = {
					start : cur.ch,
					end : cur.ch,
					string : "",
					state : token.state,
					type : token.string == "." ? "property" : null
				};
			}

			var context = [];

			while (tprop) {
				tprop = getToken(editor, Pos(cur.line, tprop.start));

				if (tprop.string != ".") {
					break;
				}

				tprop = getToken(editor, Pos(cur.line, tprop.start));

				context.push(tprop);
			}

			result = getCompletions(token, context, keywords, options);
		}

		return {
			list : result,
			from : Pos(cur.line, token.start),
			to : Pos(cur.line, token.end)
		};
	}

	var MetadataElement = {
		name : "",
		uniqueName : "",
		caption : "",
		description : "",
		visible : true
	};

	var Database = {
		name : "",
		description : "",
		catalogs : [],
		dataSourceInfo : "",
		olapConnection : {},
		providerName : "",
		URL : ""
	};

	var Catalog = {
		name : "",
		schemas : [],
		database : Database
	};

	var Schema = {
		name : "",
		catalog : Catalog,
		cubes : [],
		sharedDimensions : [],
		supportedLocales : []
	};

	var Cube = $.extend({
		hierarchies : [],
		dimensions : [],
		sets : [],
		measures : [],
		supportedLocales : [],
		schema : Schema
	}, MetadataElement);

	var Locale = {
		language : "",
		country : "",
		variant : ""
	};

	var Model = {
		cube : Cube,
		roleName : "",
		locale : Locale,
		cellSet : {}
	};

	var Axis = {
		axisOrdinal : 0
	};

	var Position = {
		ordinal : 0,
		members : []
	};

	var Dimension = $.extend({
		dimensionType : {
			UNKNOWN : "",
			TIME : "",
			MEASURE : "",
			OTHER : "",
			QUANTITATIVE : "",
			ACCOUNTS : "",
			CUSTOMERS : "",
			PRODUCTS : "",
			SCENARIO : "",
			UTILITY : "",
			CURRENCY : "",
			RATES : "",
			CHANNEL : "",
			PROMOTION : "",
			ORGANIZATION : "",
			BILL_OF_MATERIALS : "",
			GEOGRAPHY : ""
		},
		hierarchies : []
	}, MetadataElement);

	var Hierarchy = $.extend({
		dimension : Dimension,
		levels : [],
		rootMembers : []
	}, MetadataElement);

	Dimension.defaultHierarchy = Hierarchy;

	var Level = $.extend({
		dimension : Dimension,
		hierarchy : Hierarchy,
		members : [],
		properties : [],
		depth : 0,
		calculated : false,
		cardinality : [],
		levelType : {
			REGULAR : "",
			ALL : "",
			NULL : "",
			TIME_YEARS : "",
			TIME_HALF_YEAR : "",
			TIME_QUARTERS : "",
			TIME_MONTHS : "",
			TIME_WEEKS : "",
			TIME_DAYS : "",
			TIME_HOURS : "",
			TIME_MINUTES : "",
			TIME_SECONDS : "",
			TIME_UNDEFINED : "",
			GEO_CONTINENT : "",
			GEO_REGION : "",
			GEO_COUNTRY : "",
			GEO_STATE_OR_PROVINCE : "",
			GEO_COUNTY : "",
			GEO_CITY : "",
			GEO_POSTALCODE : "",
			GEO_POINT : "",
			ORG_UNIT : "",
			BOM_RESOURCE : "",
			QUANTITATIVE : "",
			ACCOUNT : "",
			CUSTOMER : "",
			CUSTOMER_GROUP : "",
			CUSTOMER_HOUSEHOLD : "",
			PRODUCT : "",
			PRODUCT_GROUP : "",
			SCENARIO : "",
			UTILITY : "",
			PERSON : "",
			COMPANY : "",
			CURRENCY_SOURCE : "",
			CURRENCY_DESTINATION : "",
			CHANNEL : "",
			REPRESENTATIVE : "",
			PROMOTION : ""
		}
	}, MetadataElement);

	var Member = $.extend({
		dimension : Dimension,
		hierarchy : Hierarchy,
		level : Level,
		all : false,
		calculated : false,
		calculatedInQuery : false,
		hidden : false,
		solveOrder : 0,
		ordinal : 0,
		depth : 0,
		childMemberCount : 0,
		ancestorMembers : [],
		childMembers : [],
		properties : []
	}, MetadataElement);

	Member.parentMember = Member;
	Member.dataMember = Member;

	Hierarchy.defaultMember = Member;

	var Cell = {
		coordinateList : [],
		ordinal : 0,
		value : "",
		doubleValue : "",
		errorText : "",
		empty : false,
		error : false,
		"null" : false,
		formattedValue : ""
	};

	var ELContext = {
		cube : Cube,
		catalog : Catalog,
		model : Model,
		roleName : "",
		locale : Locale,
		memberUtils : {
			lookupMember : function(uniqueName) {
			}
		},
		axis : Axis,
		position : Position,
		columnPosition : Position,
		rowPosition : Position,
		hierarchy : Hierarchy,
		level : Level,
		member : Member,
		cell : Cell,
		cellType : {
			Value : "",
			Header : "",
			Title : "",
			Aggregation : "",
			None : ""
		},
		rowIndex : 0,
		columnIndex : 0,
		rowSpan : 0,
		columnSpan : 0,
		rowCount : 0,
		columnCount : 0,
		rowHeaderCount : 0,
		columnHeaderCount : 0,
		attributes : {}
	};

	function getCompletions(token, context, keywords, options) {
		var found = [], start = token.string;

		function maybeAdd(str) {
			if (str.indexOf(start) == 0 && !arrayContains(found, str)) {
				found.push(str);
			}
		}

		function gatherCompletions(obj) {
			for ( var name in obj) {
				maybeAdd(name);
			}
		}

		if (context) {
			var base = ELContext;

			while (base != null && context.length) {
				base = base[context.pop().string];
			}

			gatherCompletions(base);
		} else {
			gatherCompletions(ELContext);

			forEach(keywords, maybeAdd);
		}

		return found;
	}
})();
