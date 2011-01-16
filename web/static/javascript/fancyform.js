/*
* FancyFormPrototype 0.91
* Prototype compatibility enhancements by
* Jeremy Green, webeprint.com
*
* Adapted from  
* FancyForm 0.91
* By Vacuous Virtuoso, lipidity.com
*
* ---
* Checkbox and radio input replacement script.
* Toggles defined class when input is selected.
*/

var FancyForm = {
	start: function(elements, options){
		FancyForm.runningInit = 1;
		if($type(elements)!='array') elements = $$('input');
		if(!options) options = [];
		FancyForm.onclasses = ($type(options['onClasses']) == 'object') ? options['onClasses'] : {
			checkbox: 'checked',
			radio: 'selected'
		}
		FancyForm.offclasses = ($type(options['offClasses']) == 'object') ? options['offClasses'] : {
			checkbox: 'unchecked',
			radio: 'unselected'
		}
		if($type(options['extraClasses']) == 'object'){
			FancyForm.extra = options['extraClasses'];
		} else if(options['extraClasses']){
			FancyForm.extra = {
				checkbox: 'f_checkbox',
				radio: 'f_radio',
				on: 'f_on',
				off: 'f_off',
				all: 'fancy'
			}
		} else {
			FancyForm.extra = {};
		}
		FancyForm.onSelect = $pick(options['onSelect'], function(el){});
		FancyForm.onDeselect = $pick(options['onDeselect'], function(el){});
		var keeps = [];
		FancyForm.chks = filter(elements,function(chk){
			chk = $(chk);
			if( $type(chk) != 'element' ) return false;
			if( chk.tagName.toLowerCase() == 'input' && (FancyForm.onclasses[chk.getAttribute('type')]) ){
			    var el = chk.parentNode;
			    Element.extend(el);
			    if(el.getElementsBySelector('input')[0]==chk){
					el.type = chk.getAttribute('type');
					el.inputElement = chk;
					this.push(el);
				} else {
					chk.observe('click',function(ev){ev.stopPropagation();})
				}
			} else if( chk.getElementsBySelector('input') && (chk.inputElement = chk.getElementsBySelector('input')[0]) && (FancyForm.onclasses[(chk.type = chk.inputElement.getAttribute('type'))]) ){
				return true;
			}
			return false;
		}.bind(keeps));
		FancyForm.chks = mergeArray(FancyForm.chks,keeps);
		keeps = null;
		FancyForm.chks.each(function(chk){
			chk.inputElement.setStyle({position:'absolute'});
			chk.inputElement.setStyle({left:'-9999px'});
			chk.observe('selectStart', function(){})
			chk.name = chk.inputElement.getAttribute('name');
			if(chk.inputElement.checked) FancyForm.select(chk);
			else FancyForm.deselect(chk);
			chk.observe('click', function(e){
				if(chk.inputElement.getAttribute('disabled')) return;
				if ($type(e.preventDefault) == 'function')
					e.preventDefault(true);
				else if ($type(e.returnValue) == 'function')
					e.returnValue(true);
				if (!chk.hasClassName(FancyForm.onclasses[chk.type]))
						FancyForm.select(chk);
				else if(chk.type != 'radio')
					FancyForm.deselect(chk);
				FancyForm.focusing = 1;
				chk.inputElement.focus();
				FancyForm.focusing = 0;
			});
			chk.observe('mousedown', function(e){
				if ($type(e.preventDefault) == 'function')
					e.preventDefault(true);
				else if ($type(e.returnValue) == 'function')
					e.returnValue(true);
			});
			chk.inputElement.observe('focus', function(e){
				if(!FancyForm.focusing) chk.setStyle({outline:'1px dotted'});
			});
			chk.inputElement.observe('blur', function(e){chk.setStyle({outline:'0'})});
			if(extraclass = FancyForm.extra[chk.type])
				chk.addClassName(extraclass);
			if(extraclass = FancyForm.extra['all'])
				chk.addClassName(extraclass);
		});
		FancyForm.runningInit = 0;
	},
	select: function(chk){
	        chk = $(chk);
		chk.inputElement.checked = 'checked';
		chk.removeClassName(FancyForm.offclasses[chk.type]);
		chk.addClassName(FancyForm.onclasses[chk.type]);
		if (chk.type == 'radio'){
			FancyForm.chks.each(function(other){
				if (other.name != chk.name || other == chk) return;
				FancyForm.deselect(other);
			});
		}
		if(extraclass = FancyForm.extra['on'])
			chk.addClassName(extraclass);
		if(extraclass = FancyForm.extra['off'])
			chk.removeClassName(extraclass);
		if(!FancyForm.runningInit)
			FancyForm.onSelect(chk);
	},
	deselect: function(chk){
		chk.inputElement.checked = false;
		chk.removeClassName(FancyForm.onclasses[chk.type]);
		chk.addClassName(FancyForm.offclasses[chk.type]);
		if(extraclass = FancyForm.extra['off'])
			chk.addClassName(extraclass);
		if(extraclass = FancyForm.extra['on'])
			chk.removeClassName(extraclass);
		if(!FancyForm.runningInit)
			FancyForm.onDeselect(chk);
	},
	all: function(){
		FancyForm.chks.each(function(chk){
			FancyForm.select(chk);
		});
	},
	none: function(){
		FancyForm.chks.each(function(chk){
			FancyForm.deselect(chk);
		});
	}
};



/*
  Here are a few function borrowed from the mootools code to make the FanyForm work.
  I tried to use prototype.js equivalents for as many things as I could.
 */


/*
Function: $merge
	merges a number of objects recursively without referencing them or their sub-objects.

Arguments:
	any number of objects.

Example:
	>var mergedObj = $merge(obj1, obj2, obj3);
	>//obj1, obj2, and obj3 are unaltered
*/

function $merge(){
	var mix = {};
	for (var i = 0; i < arguments.length; i++){
		for (var property in arguments[i]){
			var ap = arguments[i][property];
			var mp = mix[property];
			if (mp && $type(ap) == 'object' && $type(mp) == 'object') mix[property] = $merge(mp, ap);
			else mix[property] = ap;
		}
	}
	return mix;
};


/*
Function: $defined
	Returns true if the passed in value/object is defined, that means is not null or undefined.

Arguments:
	obj - object to inspect
*/

function $defined(obj){
	return (obj != undefined);
};

/*
Function: $pick
	Returns the first object if defined, otherwise returns the second.

Arguments:
	obj - object to test
	picked - the default to return

Example:
	(start code)
		function say(msg){
			alert($pick(msg, 'no meessage supplied'));
		}
	(end)
*/

function $pick(obj, picked){
	return $defined(obj) ? obj : picked;
};

/*

Function: $type
	Returns the type of object that matches the element passed in.

Arguments:
	obj - the object to inspect.

Example:
	>var myString = 'hello';
	>$type(myString); //returns "string"

Returns:
	'element' - if obj is a DOM element node
	'textnode' - if obj is a DOM text node
	'whitespace' - if obj is a DOM whitespace node
	'arguments' - if obj is an arguments object
	'object' - if obj is an object
	'string' - if obj is a string
	'number' - if obj is a number
	'boolean' - if obj is a boolean
	'function' - if obj is a function
	'regexp' - if obj is a regular expression
	'class' - if obj is a Class. (created with new Class, or the extend of another class).
	'collection' - if obj is a native htmlelements collection, such as childNodes, getElementsByTagName .. etc.
	false - (boolean) if the object is not defined or none of the above.
*/

function $type(obj){
	if (!$defined(obj)) return false;
	if (obj.htmlElement) return 'element';
	var type = typeof obj;
	if (type == 'object' && obj.nodeName){
		switch(obj.nodeType){
			case 1: return 'element';
			case 3: return (/\S/).test(obj.nodeValue) ? 'textnode' : 'whitespace';
		}
	}
	if (type == 'object' || type == 'function'){
		switch(obj.constructor){
			case Array: return 'array';
			case RegExp: return 'regexp';
			case Class: return 'class';
		}
		if (typeof obj.length == 'number'){
			if (obj.item) return 'collection';
			if (obj.callee) return 'arguments';
		}
	}
	return type;
};



/*
  My own function to filter an array.
  Borrowed from MooTools Array.filter
 */

function filter(array,fn,bind){
    var results = [];
    for (var i = 0, j = array.length; i < j; i++){
	if (fn.call(bind, array[i], i, array)) results.push(array[i]);
    }
    return results;
}


/*
  My own function to merge two arrays
 */

function mergeArray(first,second){
    for (var i = 0, l = second.length; i < l; i++){
	first.push(second[i]);
    }
    return first;
}



