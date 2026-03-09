<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
  <jsp:include page="template/header.jsp"/>
  <style>
    .rating-stars { color: #f1c40f; letter-spacing: 1px; }
    .rating-stars .empty { color: #ddd; }
  </style>
  <script type="text/javascript">
		$(function(){
                var address =  '/rate-request/'+$("input[name='id']").val();
			$(".btn-success").click(function(){
				$.ajax({
				url: address, // your api url
			    // jQuery < 1.9.0 -> use type
			    // jQuery >= 1.9.0 -> use method
			    method: "PUT", // method is any HTTP method
			    data: {comment : $("#comment").val(),
			    	   rating  : $('input[name=rating]:checked').val()}, // data as js object
			    success: function() {
			    	
			    	$(location).attr('href','/my-request');
			    	
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
                         <h4> Rate Request </h4>
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
                                            <label>Employee :  <c:out value="${mgRequest.user.customer.fullName}"></c:out></label>
                                          
                                        </div>
                                        
                                        <div class="form-group">
                                            <label>status :  <c:out value="${mgRequest.requestStatus}"></c:out></label>
                                          
                                        </div>
                                        
    
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
                        <div class="row">
                    <div class="col-lg-12">
                       
                       
                        <div class="panel panel-default">
                        <div class="panel-heading">
                           Customer List
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                            <table width="100%" class="table table-striped table-bordered table-hover" id="tabla">
                                <thead>
                                    <tr>
                                        <th>Status</th>
                                        <th>Comment</th>
                                        <th>Rating</th>
                                        <th>Date</th>
                                        
                                    </tr>
                                </thead>
                                
                                <tbody>
                                   <c:forEach items="${requestHistoryList}" var="rh">
                                    <tr>
                                        <td>${rh.requestStatus}</td>
                                        <td>${rh.comments}</td>
                                        <td class="center"><span class="rating-stars" title="${rh.rating}"><c:forEach begin="1" end="5" var="i"><c:choose><c:when test="${rh.rating != null && rh.rating >= i}">★</c:when><c:otherwise>☆</c:otherwise></c:choose></c:forEach></span> <c:if test="${rh.rating != null}">(${rh.rating})</c:if></td> 
                                        <td class="center">${rh.stringLocalDateTime}</td> 
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
               </div>
                <!-- /.panel-body -->
            </div>
            <!-- /.container-fluid -->
        </div>
        <!-- /#page-wrapper -->
   <jsp:include page="template/footer.jsp"/>