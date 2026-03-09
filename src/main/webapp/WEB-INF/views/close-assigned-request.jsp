<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
  <jsp:include page="template/header.jsp"/>
  <script type="text/javascript">
		$(function(){
                var address =  '/close-assigned-request/'+$("input[name='id']").val();
			$(".btn-success").click(function(){
				$.ajax({
				url: address, // your api url
			    // jQuery < 1.9.0 -> use type
			    // jQuery >= 1.9.0 -> use method
			    method: "PUT", // method is any HTTP method
			    data: {comment : $("#comment").val()}, // data as js object
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
                         <h4> New Request </h4>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                       
                        <form>
                                        
                                        <div class="form-group">
                                        	 <input type="hidden" name="id" value="<c:out value="${mgRequest.id}"></c:out>">
                                            <label>Request # <c:out value="${mgRequest.id}"></c:out></label>
                                        </div>
                                       
                                        <div class="form-group">
                                        	
                                            <label>Location (Room) : <c:out value="${mgRequest.location}"></c:out></label>
                                        </div>
                                      
                                        
                                        
                                        <div class="form-group">
                                            <label>Description :  <c:out value="${mgRequest.description}"></c:out></label>
                                          
                                        </div>
                                        
                                        
                                        
                                        <div class="form-group">
                                           <label for="comment"> Comments :  </label>
                                           <textarea rows="3" id="comment" class="form-control">
                                            
                                           
                                           </textarea>
                                        </div>
                                        
                                        
                                       
                                          <a class="btn btn-success" href="#"> Close Request </a>
                                        
                           </form> 
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