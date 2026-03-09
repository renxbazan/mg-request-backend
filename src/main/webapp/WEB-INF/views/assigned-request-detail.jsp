<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
  <jsp:include page="template/header.jsp"/>
  <script type="text/javascript">
		$(function(){
                var address =  '/attend-assigned-request/'+$("input[name='id']").val();
			$(".btn-success").click(function(){
				$.ajax({
				url: address, // your api url
			    // jQuery < 1.9.0 -> use type
			    // jQuery >= 1.9.0 -> use method
			    method: "PUT", // method is any HTTP method
			    data: {}, // data as js object
			    success: function() {
			    	
			    	$(location).attr('href','/assigned-request');
			    	
			    },
			    error: function(req, err){
			    	console.log('my message' + err);
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
                         <h4> Request detail </h4>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                       
                        <form:form method="PUT" modelAttribute="mgRequest" action=""  >
                                        
                                       <div class="form-group">
                                            <label>Site</label>
                                            <form:select cssClass="form-control" path="siteId" cssErrorClass="" disabled="disabled"  >
                                            <form:option value="">[--Seleccione--]</form:option>
                                            <form:options items="${siteList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                       
                                        <div class="form-group">
                                        	<form:hidden path="id"/>
                                            <label>Location (Room)</label>
                                             <form:input cssClass="form-control" path="location" readonly="true"  />
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Service Category</label>
                                            <form:select cssClass="form-control" id="serviceCategorySelect" cssErrorClass="" path="serviceCategoryId" disabled="disabled"  >
                                            <form:option value="">[--Select--]</form:option>
                                            <form:options items="${serviceCategoryList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>Service Sub Category</label>
                                            <form:select cssClass="form-control" id="serviceSubCategorySelect" path="serviceSubCategoryId" cssErrorClass="" disabled="disabled" >
                                            <form:option value="">[--Select--]</form:option>
                                            <form:options items="${serviceSubCategoryList}" itemValue="id" itemLabel="name"/>
                                            </form:select>
                                        </div>
                                        
                                        
                                        <div class="form-group">
                                            <label>Description</label>
                                            <form:textarea cssClass="form-control" path="description" rows="3" readonly="true"/>
                                        </div>
                                        
                                           <div class="form-group">
                                            <label>Priority</label>
                                            <label class="radio-inline">
                                                <form:radiobutton path="priority"  id="optionsRadiosInline3" value="L" disabled="disabled" /> Low
                                            </label>
                                            <label class="radio-inline">
                                               <form:radiobutton path="priority"  id="optionsRadiosInline3" value="M" disabled="disabled"/> Medium
                                            </label>
                                            <label class="radio-inline">
                                               <form:radiobutton path="priority"  id="optionsRadiosInline3" value="H" disabled="disabled"/> High
                                            </label>
                                          
                                        </div>
                                        
                                        <div class="form-group">
                                            <form:hidden path="userId"/>
                                        </div>
                                        
                                       
                                          <a class="btn btn-success" href="#"> Acknowledge </a>
                                        
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