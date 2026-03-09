<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
  <jsp:include page="template/header.jsp"/>
  <script type="text/javascript">
		$(function(){
			$("#tabla").DataTable({
	            responsive: true
	        });
			$( "form" ).validate( {
				rules: {
					siteId:{notEqual : "-1"},
					name: "required",
				},
				messages: {
					siteId: "Wrong Site",
					nroDocumento: "Please insert a Name"
				},
				errorElement: "em",
				errorPlacement: function ( error, element ) {
					// Add the `help-block` class to the error element
					error.addClass( "help-block" );

					if ( element.prop( "type" ) === "checkbox" ) {
						error.insertAfter( element.parent( "label" ) );
					} else {
						error.insertAfter( element );
					}
				},
				highlight: function ( element, errorClass, validClass ) {
					$( element ).parent().addClass( "has-error" ).removeClass( "has-success" );
				},
				unhighlight: function (element, errorClass, validClass) {
					$( element ).parent().addClass( "has-success" ).removeClass( "has-error" );
				}
			} );
			
			$("#serviceCategorySelect").change(function(){
				 $('#serviceSubCategorySelect').empty();
				 $('#serviceSubCategorySelect').append('<option value="">' + '[--Select--]'+ '</option>');
				$.ajax({
		            type: "GET",
		            url: "/serviceSubCategoryByCategoryId/"+$(this).val(),
		            data: { },
		            success: function(data){
		                // Use jQuery's each to iterate over the opts value
		                $.each(data, function(i, d) {
		                    // You will need to alter the below to get the right values from your json object.  Guessing that d.id / d.modelName are columns in your carModels data
		                    $('#serviceSubCategorySelect').append('<option value="' + d.id + '">' + d.name + '</option>');
		                });
		            }
		        });
				
				
				
				
				
			});
			
			
		});
  	
  </script>
  
  <!-- Page Content -->
        <div id="page-wrapper">
            <div class="container-fluid">
    
               <div class="panel-body">
                <div class="row">
                    <div class="col-lg-6">       
                       
                       
                        <div class="panel panel-default">
                        <div class="panel-heading">
                         <h4> New Request </h4>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                       
                        <form:form method="POST" modelAttribute="mgRequest" action="${pageContext.request.contextPath}/request"  >
                                        
                                       <div class="form-group">
                                            <label>Site</label>
                                            <form:select cssClass="form-control" path="siteId" cssErrorClass=""  >
                                            <form:option value="-1">[--Select--]</form:option>
                                            <form:options items="${siteList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                       
                                        <div class="form-group">
                                        	<form:hidden path="id"/>
                                            <label>Location (Room)</label>
                                             <form:input cssClass="form-control" path="location" />
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Service Category</label>
                                            <form:select cssClass="form-control" id="serviceCategorySelect" cssErrorClass="" path="serviceCategoryId"  >
                                            <form:option value="">[--Select--]</form:option>
                                            <form:options items="${serviceCategoryList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Service Sub Category</label>
                                            <form:select cssClass="form-control" id="serviceSubCategorySelect" path="serviceSubCategoryId" cssErrorClass=""  >
                                            <form:option value="">[--Select--]</form:option>
                                            <form:options items="${serviceSubCategoryList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                        
                                        
                                        <div class="form-group">
                                            <label>Description</label>
                                            <form:textarea cssClass="form-control" path="description" rows="3"/>
                                        </div>
                                        
                                         <div class="form-group">
                                            <label>Priority</label>
                                            <label class="radio-inline">
                                                <form:radiobutton path="priority"  id="optionsRadiosInline3" value="L" /> Low
                                            </label>
                                            <label class="radio-inline">
                                               <form:radiobutton path="priority"  id="optionsRadiosInline3" value="M" /> Medium
                                            </label>
                                            <label class="radio-inline">
                                               <form:radiobutton path="priority"  id="optionsRadiosInline3" value="H" /> High
                                            </label>
                                          
                                        </div>
                                        
                                        <div class="form-group">
                                            <form:hidden path="userId"/>
                                        </div>
                                        
                                       
                                        <button type="submit" class="btn btn-default">Save</button>
                                        
                           </form:form> 
                      </div>
                    
                 <!-- /.panel-body -->
                    </div>             
                      	
                    </div>
                    <!-- /.col-lg-6 -->
                     <div class="col-lg-6">
                       <!-- /.table-responsive -->
                          
                     </div>
                </div>
                <!-- /.row -->
               </div>
                <!-- /.panel-body -->
            </div>
            <!-- /.container-fluid -->
        </div>
        <!-- /#page-wrapper -->
   <jsp:include page="template/footer.jsp"/>