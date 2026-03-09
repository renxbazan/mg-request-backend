	function autoCliente(link){
		
		var options = {

				  url: function(phrase) {
				    return link;
				  },

				  getValue: function(element) {
				    return element.razonSocialApeNombres;
				  },

				  ajaxSettings: {
				    method: "GET",
				    data: {
				      dataType: "json"
				    }
				  },

				  preparePostData: function(data) {
				    data.term = $("#autoCliente").val();
				    return data;
				  },
				  

					list: {

						onClickEvent: function() {
							alert($("#autoCliente").getSelectedItemData().codCliente);
						}
					},

				  requestDelay: 400
				};

			$("#autoCliente").easyAutocomplete(options);
		
		
		
	}
	
function autoEmpleado(link){

	var optionsEmpleado = {

			  url: function(phrase) {
			    return link;
			  },

			  getValue: function(element) {
			    return element.primerNombre + ' ' +element.segundoNombre + ' ' +element.apellidoPaterno + ' '+ element.apellidoMaterno ;
			  },

			  ajaxSettings: {
			    method: "GET",
			    data: {
			      dataType: "json"
			    }
			  },

			  preparePostData: function(data) {
			    data.term = $("#autoEmpleado").val();
			    return data;
			  },
			  

				list: {

					onClickEvent: function() {
						alert($("#autoEmpleado").getSelectedItemData().codEmpleado);
					}
				},

			  requestDelay: 400
			};

		$("#autoEmpleado").easyAutocomplete(optionsEmpleado);
		
		
	}

  	
          