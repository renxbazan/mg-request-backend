<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
  <jsp:include page="template/header.jsp"/>
  <style>
    .star-rating { display: inline-flex; gap: 2px; font-size: 1.8rem; cursor: pointer; }
    .star-rating .star { color: #ddd; user-select: none; transition: color 0.15s; }
    .star-rating .star.filled { color: #f1c40f; }
    .star-rating .star:hover { color: #f1c40f; }
    .star-rating.hover-preview .star.filled { color: #f1c40f; }
  </style>
  <script type="text/javascript">
		$(function(){
                var address =  '/rate-request/'+$("input[name='id']").val();
			$(".btn-success").click(function(){
				$.ajax({
				url: address,
			    method: "PUT",
			    data: {comment : $("#comment").val(),
			    	   rating  : $("#ratingValue").val()},
			    success: function() { $(location).attr('href','/my-request'); },
			    error: function(req, err){ console.log('my message' + err); }
				});
		 });

			// Star rating: hover preview, click to confirm
			var $container = $("#starRating");
			var $stars = $container.find(".star");
			var $hidden = $("#ratingValue");
			var current = parseInt($hidden.val(), 10) || 5;

			function setDisplay(value) {
				$stars.each(function(i) {
					var $s = $(this);
					if (i + 1 <= value) $s.addClass("filled").text("★");
					else $s.removeClass("filled").text("☆");
				});
			}
			setDisplay(current);

			$stars.on("mouseenter", function() {
				var v = $(this).data("value");
				setDisplay(v);
			});
			$container.on("mouseleave", function() {
				setDisplay(current);
			});
			$stars.on("click", function() {
				current = $(this).data("value");
				$hidden.val(current);
				setDisplay(current);
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
                                            <label>Rating</label>
                                            <div class="star-rating" id="starRating" role="group" aria-label="Valoración de 1 a 5 estrellas">
                                                <input type="hidden" name="rating" id="ratingValue" value="5">
                                                <span class="star" data-value="1" title="1">☆</span>
                                                <span class="star" data-value="2" title="2">☆</span>
                                                <span class="star" data-value="3" title="3">☆</span>
                                                <span class="star" data-value="4" title="4">☆</span>
                                                <span class="star" data-value="5" title="5">☆</span>
                                            </div>
                                        </div>
                                        
                                        <div class="form-group">
                                           <label for="comment"> Comments :  </label>
                                           <textarea rows="3" id="comment" class="form-control">
                                            
                                           
                                           </textarea>
                                        </div>
                                        
                                        
                                       
                                          <a class="btn btn-success" href="#"> Rate Request </a>
                                        
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