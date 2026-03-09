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
					tipoDocumento:{notEqual : "-1"},
					name: "required",
				},
				messages: {
					tipoDocumento: "Select a Company Type",
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
		});
  	
  </script>
  
  <!-- Page Content -->
        <div id="page-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-12">
                       
                       
                        <div class="panel panel-default">
                        <div class="panel-heading">
                           Site List
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                            <table width="100%" class="table table-striped table-bordered table-hover" id="tabla">
                                <thead>
                                    <tr>
                                        <th>Name </th>
                                        <th>Description </th>
                                        <th>Company </th>
                                        <th>Address </th>
                                        <th>Phone </th>
                                        <th></th>
                                        <th></th>
                                    </tr>
                                </thead>
                                
                                <tbody>
                                   <c:forEach items="${siteList}" var="site">
                                    <tr>
                                        <td>${site.name}</td>
                                        <td>${site.description}</td>
                                        <td>${site.company.name}</td>
                                        <td>${site.address}</td>
                                        <td>${site.phone}</td>
                                        <td><a href="${pageContext.request.contextPath}/site/${site.id}"><span class="glyphicon glyphicon-edit" title="edit"></span></a></td>
                                        <td><a href="${pageContext.request.contextPath}/deleteSite/${site.id}" ><span class="glyphicon glyphicon-trash" title="remove"></span></a></td>
                                    </tr>
                                  </c:forEach>
                       
                                </tbody>
                            </table>
                          
                        </div>
                        <!-- /.panel-body -->
                    </div>
                       
                  </div>
                    <!-- /.col-lg-12 -->
               </div>
                <!-- /.row -->
               <div class="panel-body">
                <div class="row">
                    <div class="col-lg-6">       
                       
                       
                        <div class="panel panel-default">
                        <div class="panel-heading">
                         <h4> Site : <c:out value="${site.name}"></c:out></h4>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                       
                        <form:form method="POST" modelAttribute="site" action="${pageContext.request.contextPath}/site"  >
                                        
                                       <div class="form-group">
                                            <label>Company</label>
                                            <form:select cssClass="form-control" path="companyId" cssErrorClass=""  >
                                            <form:option value="">[--Seleccione--]</form:option>
                                            <form:options items="${companyList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                       
                                        <div class="form-group">
                                        	<form:hidden path="id"/>
                                            <label>Name</label>
                                             <form:input cssClass="form-control" path="name" />
                                        </div>
                                        <div class="form-group">
                                            <label>Description</label>
                                            <form:input cssClass="form-control" path="description" />
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Comments</label>
                                            <form:textarea cssClass="form-control" path="comments"  />
                                        </div>
                                        <div class="form-group">
                                            <label>Address</label>
                                            <form:input cssClass="form-control" path="address" />
                                        </div>
                                        <div class="form-group">
                                            <label>Phone</label>
                                            <form:input cssClass="form-control" path="phone" />
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