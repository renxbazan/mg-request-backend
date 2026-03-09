<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
  <jsp:include page="template/header.jsp"/>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/1.3.3/jspdf.min.js"></script>
  <script src="https://html2canvas.hertzen.com/dist/html2canvas.js"></script>
  <script type="text/javascript">
		$(function(){
			$( "#fromDate" ).datepicker({dateFormat: "dd/mm/yy"});
			$( "#toDate" ).datepicker({dateFormat: "dd/mm/yy"});
		
			$("#search").on("click", function(e){
			    e.preventDefault();
			    
			    var validator = $( "form" ).validate( {
					rules: {
						toDate: "required",
						fromDate: "required"
					},
					messages: {
						toDate: "Select a Date",
						fromDate: "Select a Date",
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
			    
			    
			    
			    $('#registrationForm').attr('action', "/hour-registration-search").submit();
			    validator.destroy();
			});
			
			$("#save").on("click", function(e){
				
			    e.preventDefault();
			    
			   var validator = $( "form" ).validate( {
					rules: {
						
						fromDate: "required",
						siteId:{notEqual : "-1"},
						fromHour: "required",
						toHour: "required"
					},
					messages: {
						siteId: "Select a Site",
						fromDate: "Select a Date",
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
			    
			   
			    
			    $('#registrationForm').attr('action', "/hour-registration").submit();
			    validator.destroy();
			});
		});
		
		function getPDF(){

			var HTML_Width = $(".canvas_div_pdf").width();
			var HTML_Height = $(".canvas_div_pdf").height();
			var top_left_margin = 15;
			var PDF_Width = (HTML_Width+(top_left_margin*2))*0.8;
			var PDF_Height = (PDF_Width*1.5)+(top_left_margin*2);
			var canvas_image_width = HTML_Width;
			var canvas_image_height = HTML_Height;
			
			var totalPDFPages = Math.ceil(HTML_Height/PDF_Height)-1;
			

			html2canvas($(".canvas_div_pdf")[0],{allowTaint:true}).then(function(canvas) {
				canvas.getContext('2d');
				
				console.log(canvas.height+"  "+canvas.width);
				
				
				var imgData = canvas.toDataURL("image/jpeg", 1.5);
				var pdf = new jsPDF('p', 'pt',  [PDF_Width, PDF_Height]);
			    pdf.addImage(imgData, 'JPG', top_left_margin, top_left_margin,canvas_image_width,canvas_image_height);
				
				
				for (var i = 1; i <= totalPDFPages; i++) { 
					pdf.addPage(PDF_Width, PDF_Height);
					pdf.addImage(imgData, 'JPG', top_left_margin, -(PDF_Height*i)+(top_left_margin*4),canvas_image_width,canvas_image_height);
				}
				
			    pdf.save("HTML-Document.pdf");
	        });
		};
  	
  </script>
  <style>


.container {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: initial;
    margin-bottom: 40px;
}
</style>
  
  <!-- Page Content -->
        <div id="page-wrapper">
            <div class="container-fluid">
    
               <div class="panel-body">
                <div class="row">
                    <div class="col-lg-6">       
                       
                       
                        <div class="panel panel-default">
                        <div class="panel-heading">
                         <h4> Hour Registration </h4>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                      <c:if test="${hourRegistration.id==null}"> 
                        <form:form autocomplete="off" method="POST" id="registrationForm" modelAttribute="hourRegistrationRequest" action="${pageContext.request.contextPath}/hour-registration"  >
                                        
                                       <div class="form-group">
                                            <label>Empleado</label>
                                            <form:select cssClass="form-control" path="customerId" cssErrorClass=""  >
                                            <form:option value="-99">[--Select--]</form:option>
                                            <form:options items="${customerList}" itemValue="id" itemLabel="fullName"/>
                                            </form:select>
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Site</label>
                                            <form:select cssClass="form-control" path="siteId" cssErrorClass=""  >
                                            <form:option value="-1">[--Select--]</form:option>
                                            <form:options items="${siteList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                       
                                        <div class="form-group">
                                            <label>Fecha Inicial</label>
                                            <form:input cssClass="form-control" path="fromDate" id="fromDate" />
                                        </div>
                                        
                                         <div class="form-group">
                                            <label>Fecha Final</label>
                                            <form:input cssClass="form-control" path="toDate" id="toDate" />
                                        </div>
                                        
                                       <div class="form-group">
                                            <label>Hora Entrada</label>
                                            <form:input type="time" cssClass="form-control" path="fromHour" />
                                        </div>
                                        
                                         <div class="form-group">
                                            <label>Hora Salida</label>
                                            <form:input type="time" cssClass="form-control" path="toHour" />
                                        </div>
                                        
                                         <div class="form-group">
                                            <button type="submit" class="btn btn-default" id="search">Search</button><br>
                                        </div>
                                        
                                         <div class="form-group">
                                         <button type="submit" class="btn btn-default" id="save">Save</button>

                                        </div>
                                        
                                                                                                                   
                                       
                                       
                                        
                                        
                           </form:form> 
                            </c:if>
                      </div>
                      
                        <div class="panel-body">
                        <c:if test="${hourRegistration.id!=null}"> 
                        <form:form autocomplete="off" method="POST" modelAttribute="hourRegistration" action="${pageContext.request.contextPath}/update-hour-registration"  >
                                        <form:hidden path="id"/>
                                        <form:hidden path="dateString"/>
                                       <div class="form-group">
                                            <label>Empleado</label>
                                            <form:select cssClass="form-control" path="customerId" cssErrorClass=""  >
                                            <form:option value="-99">[--Select--]</form:option>
                                            <form:options items="${customerList}" itemValue="id" itemLabel="fullName"/>
                                            </form:select>
                                        </div>
                                        
                                         <div class="form-group">
                                            <label>Site</label>
                                            <form:select cssClass="form-control" path="siteId" cssErrorClass=""  >
                                            <form:option value="-1">[--Select--]</form:option>
                                            <form:options items="${siteList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Fecha</label>
                                            <label>${hourRegistration.dateString}</label>
                                        </div>
                                      
                                       <div class="form-group">
                                            <label>Horas Trabajadas</label>
                                            <form:input  cssClass="form-control" path="hour" />
                                        </div>
                                        
                                        <button type="submit" class="btn btn-default">Save</button>
                                        
                           </form:form> 
                           </c:if>
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
                 <div class="row">
                    <div class="col-lg-12">
                       
                       
                        <div class="panel panel-default">
                        <div class="panel-heading">
                           Worked Hours List 
                           <button onclick="getPDF()" id="downloadbtn"><b>Download as PDF</b></button>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                         <c:if test="${not empty workedHourList}"> 
                         <c:set var="total" value="${0}"/>

                         <div class="canvas_div_pdf">
                          <div class="container" style='width:100px;height:100px;line-height:100px'>
                            <img class="resize_fit_center" src="<c:url value='/images/logo_mg.png'/>">
                          </div>
                          <div class="container" style='width:100px;height:100px;line-height:100px'>
                            <div class="container">
                            <h3>Employee: <c:out value="${workedHourList[0].customer.fullName}"></c:out></h3>
                            
                            </div>
                          </div>
                            <table width="100%" class="table table-striped table-bordered table-hover" id="tabla">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Date</th>
                                        <th>Site</th>
                                        <th>Worked Hours</th>
                                        <th>Payment</th>
                                        <th></th>
                                         <th></th>
                                    </tr>
                                </thead>
                                
                                <tbody>
                                   <c:forEach items="${workedHourList}" var="workedHour">
                                    <tr>
                                        <td>${workedHour.customer.fullName}</td>
                                        <td>${workedHour.dateString}</td>
                                         <td>${workedHour.site.name}</td>
                                        <td class="center">${workedHour.hour}</td> 
                                        <td class="center">${workedHour.paymentAmount}</td> 
                                        <td><a href="${pageContext.request.contextPath}/hour-registration/${workedHour.id}"><span class="glyphicon glyphicon-edit" title="edit"></span></a></td>
                                        <td><a href="${pageContext.request.contextPath}/deleteHourRegistration/${workedHour.id}" ><span class="glyphicon glyphicon-trash" title="remove"></span></a></td>
                                        <c:set var="total" value="${total + workedHour.paymentAmount}" />
                                    </tr>
                                  </c:forEach>
                       
                                </tbody>
                            </table>
                            <span>
                            Total: <c:out value="${total}"></c:out>
                            </span>
                            </div>
                          </c:if>
                           <c:if test="${not empty hourRegistrationDTOList}"> 
                           <div class="canvas_div_pdf">
                            <table width="100%" class="table table-striped table-bordered table-hover" id="tabla">
                                <thead>
                                    <tr>
                                        <th>Full Name</th>
                                        <th>Worked Hours</th>
                                        <th>Payment Amount</th>
                                    </tr>
                                </thead>
                                
                                <tbody>
                                   <c:forEach items="${hourRegistrationDTOList}" var="hourRegistrationDto">
                                    <tr>
                                        <td>${hourRegistrationDto.fullName}</td>
                                        <td>${hourRegistrationDto.workedHour}</td>
                                        <td class="center">${hourRegistrationDto.paymentAmount}</td> 
                                       
                                        
                                    </tr>
                                  </c:forEach>
                       
                                </tbody>
                            </table>
                            </div>
                          </c:if>
                        </div>
                        <!-- /.panel-body -->
                    </div>
                       
                  </div>
                    <!-- /.col-lg-12 -->
               </div>
                <!-- /.row -->
               </div>
                <!-- /.panel-body -->
            </div>
            <!-- /.container-fluid -->
        </div>
        <!-- /#page-wrapper -->
   <jsp:include page="template/footer.jsp"/>