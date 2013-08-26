define([
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/declare",
	"./_Container",
	"./at",
	"./Group",
	"dijit/form/TextBox"
], function(array, lang, declare, Container, at){

	return declare("dojox.mvc.Generate", [Container], {
		// summary:
		//		A container that generates a view based on the data model its bound to.
		//
		// description:
		//		A generate introspects its data binding and creates a view contained in
		//		it that allows displaying the bound data. Child dijits or custom view
		//		components inside it inherit their parent data binding context from it.
	
		// _counter: [private] Integer
		//		A count maintained internally to always generate predictable widget
		//		IDs in the view generated by this container.
		_counter : 0,
	
		// defaultWidgetMapping: Object
		//		The mapping of types to a widget class. Set widgetMapping to override this. 
		//	
		_defaultWidgetMapping: {"String" : "dijit/form/TextBox"},
	
		// defaultClassMapping: Object
		//		The mapping of class to use. Set classMapping to override this. 
		//	
		_defaultClassMapping: {"Label" : "generate-label-cell", "String" : "generate-dijit-cell", "Heading" : "generate-heading", "Row" : "row"},
	
	
		// defaultIdNameMapping: Object
		//		The mapping of id and name to use. Set idNameMapping to override this. A count will be added to the id and name
		//	
		_defaultIdNameMapping: {"String" : "textbox_t"},

		// children: dojo/Stateful
		//		The array of data model that is used to render child nodes.
		children: null,

		// _relTargetProp: String
		//		The name of the property that is used by child widgets for relative data binding.
		_relTargetProp : "children",

		startup: function(){
			this.inherited(arguments);
			this._setChildrenAttr(this.children);
		},

		////////////////////// PRIVATE METHODS ////////////////////////

		_setChildrenAttr: function(/*dojo/Stateful*/ value){
			// summary:
			//		Handler for calls to set("children", val).
			// description:
			//		Sets "ref" property so that child widgets can refer to, and then rebuilds the children.

			var children = this.children;
			this._set("children", value);
			// this.binding is the resolved ref, so not matching with the new value means change in repeat target.
			if(this.binding != value){
				this.set("ref", value);
			}
			if(this._started && (!this._builtOnce || children != value)){
				this._builtOnce = true;
				this._buildContained(value);
			}
		},
	
		_buildContained: function(/*dojo/Stateful*/ children){
			// summary:
			//		Destroy any existing generated view, recreate it from scratch
			//		parse the new contents.
			// children: dojo/Stateful
			//		The array of child widgets.
			// tags:
			//		private

			if(!children){ return; }

			this._destroyBody();
	
			this._counter = 0;
			this.srcNodeRef.innerHTML = this._generateBody(children);
	
			this._createBody();
		},
	
		_generateBody: function(/*dojo/Stateful*/ children, /*Boolean*/ hideHeading){
			// summary:
			//		Generate the markup for the view associated with this generate
			//		container.
			// children: dojo/Stateful
			//		The associated data to generate a view for.
			// hideHeading: Boolean
			//		Whether the property name should be displayed as a heading.
			// tags:
			//		private

			if(children === void 0){ return ""; }

			var body = [];
			var isStatefulModel = lang.isFunction(children.toPlainObject);

			function generateElement(value, prop){
				if(isStatefulModel ? (value && lang.isFunction(value.toPlainObject)) : !lang.isFunction(value)){
					if(lang.isArray(value)){
						body.push(this._generateRepeat(value, prop));
					}else if(isStatefulModel ? value.value : ((value == null || {}.toString.call(value) != "[object Object]") && (!(value || {}).set || !(value || {}).watch))){
						// TODO: Data types based widgets
						body.push(this._generateTextBox(prop, isStatefulModel));
					}else{
						body.push(this._generateGroup(value, prop, hideHeading));
					}
				}
			}

			if(lang.isArray(children)){
				array.forEach(children, generateElement, this);
			}else{
				for(var s in children){
					if(children.hasOwnProperty(s)){
						generateElement.call(this, children[s], s);
					}
				}
			}

			return body.join("");
		},
	
		_generateRepeat: function(/*dojox/mvc/StatefulArray*/ children, /*String*/ repeatHeading){
			// summary:
			//		Generate a repeating model-bound view.
			// children: dojox/mvc/StatefulArray
			//		The bound node (a collection/array node) to generate a
			//		repeating UI/view for.
			// repeatHeading: String
			//		The heading to be used for this portion.
			// tags:
			//		private

			var headingClass = (this.classMapping && this.classMapping["Heading"]) ? this.classMapping["Heading"] : this._defaultClassMapping["Heading"];
			return '<div data-dojo-type="dojox/mvc/Group" data-dojo-props="target: at(\'rel:\', \'' + repeatHeading + '\')" + id="' + this.id + '_r' + this._counter++ + '">'
			 + '<div class="' + headingClass + '\">' + repeatHeading + '</div>'
			 + this._generateBody(children, true)
			 + '</div>';
		},
		
		_generateGroup: function(/*dojo/Stateful*/ model, /*String*/ groupHeading, /*Boolean*/ hideHeading){
			// summary:
			//		Generate a hierarchical model-bound view.
			// model: dojo/Stateful
			//		The bound (intermediate) model to generate a hierarchical view portion for.
			// groupHeading: String
			//		The heading to be used for this portion.
			// hideHeading: Boolean
			//		Whether the heading should be hidden for this portion.
			// tags:
			//		private

			var html = ['<div data-dojo-type="dojox/mvc/Group" data-dojo-props="target: at(\'rel:\', \'' + groupHeading + '\')" + id="' + this.id + '_g' + this._counter++ + '">'];
			if(!hideHeading){
				var headingClass = (this.classMapping && this.classMapping["Heading"]) ? this.classMapping["Heading"] : this._defaultClassMapping["Heading"];
				html.push('<div class="' + headingClass + '\">' + groupHeading + '</div>');
			}
			html.push(this._generateBody(model) + '</div>');
			return html.join("");
		},
	
		_generateTextBox: function(/*String*/ prop, /*Boolean*/ referToValue){
			// summary:
			//		Produce a widget for a simple value.
			// prop: String
			//		The data model property name.
			// referToValue: Boolean
			//		True if the property is dojox/mvc/StatefulModel with "value" attribute.
			// tags:
			//		private
			// TODO: Data type based widget generation / enhanced meta-data

			var idname = this.idNameMapping ? this.idNameMapping["String"] : this._defaultIdNameMapping["String"];
			idname = idname + this._counter++; 
			var widClass = this.widgetMapping ? this.widgetMapping["String"] : this._defaultWidgetMapping["String"];
			var labelClass = (this.classMapping && this.classMapping["Label"]) ? this.classMapping["Label"] : this._defaultClassMapping["Label"];
			var stringClass = (this.classMapping && this.classMapping["String"]) ? this.classMapping["String"] : this._defaultClassMapping["String"];
			var rowClass = (this.classMapping && this.classMapping["Row"]) ? this.classMapping["Row"] : this._defaultClassMapping["Row"];
			var bindingSyntax = 'value: at(\'rel:' + (referToValue && prop || '') + '\', \'' + (referToValue ? 'value' : prop) + '\')'; 

			return '<div class="' + rowClass + '\">' +
					'<label class="' + labelClass + '\">' + prop + ':</label>' +
					'<input class="' + stringClass + '\" data-dojo-type="' + widClass + '\"' +
					' data-dojo-props="name: \'' + idname + '\', ' + bindingSyntax + '" id="' + idname + '\"></input>' +
					'</div>';
		}
	});
});
