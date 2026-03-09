<%@page import="com.renx.mg.request.model.RequestStatusType"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<jsp:include page="template/header.jsp" />
<script type="text/javascript">
	$(function() {
		$("#tabla").DataTable({
			responsive : true
		});
		
	});
</script>

<!-- Page Content -->
<div id="page-wrapper">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-12">

   				 <c:set var="ASSIGNED" value="<%=RequestStatusType.ASSIGNED%>"/> 
   				  <c:set var="IN_TRANSIT" value="<%=RequestStatusType.IN_TRANSIT%>"/> 

				<div class="panel panel-default">
					<div class="panel-heading">Assigned Requests</div>
					<!-- /.panel-heading -->
					<div class="panel-body">
						<table width="100%"
							class="table table-striped table-bordered table-hover" id="tabla">
							<thead>
								<tr>
									<th>Site</th>
									<th>Location</th>
									<th>Service Category</th>
									<th>Service Sub Category</th>
									<th>Priority</th>
									<th>Requester</th>
									<th>Status</th>
									<th>Date</th>
									<th></th>
								</tr>
							</thead>

							<tbody>
								<c:forEach items="${requestAssignmentList}" var="ra">
									<tr>
										<td>${ra.request.site.name}</td>
										<td>${ra.request.location}</td>
										<td>${ra.request.serviceCategory.name}</td>
										<td>${ra.request.serviceSubCategory.name}</td>
										<td>${ra.request.priority}</td>
										<td>${ra.request.user.customer.fullName}</td>
										<td>${ra.request.requestStatus}</td>
										<td>${ra.request.stringLocalDateTime}</td>
										
										<c:if test="${ra.request.requestStatus == ASSIGNED}">
										<td><a
											href="${pageContext.request.contextPath}/assigned-request-detail/${ra.request.id}"><span
												class="glyphicon glyphicon-eye-open  " title="view detail"></span></a></td>
										
										</c:if>
										
										<c:if test="${ra.request.requestStatus == IN_TRANSIT}">
										<td><a
											href="${pageContext.request.contextPath}/close-assigned-request/${ra.request.id}"><span
												class="glyphicon glyphicon-send" title="close request"></span></a></td>
										
										</c:if>
										
										
									
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
	<!-- /.container-fluid -->
</div>
<!-- /#page-wrapper -->
<jsp:include page="template/footer.jsp" />