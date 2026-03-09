<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<?xml version="1.0" encoding="ISO-8859-1" ?>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>


<!DOCTYPE html>
<html lang="es">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<title>MG Services Unlimited Request System</title>

 <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">

<!-- Bootstrap Core CSS -->
<link href="<c:url value='/css/bootstrap.min.css'/>" rel="stylesheet" />

<!-- MetisMenu CSS -->
<link href="<c:url value='/css/metisMenu.min.css'/>" rel="stylesheet" />

<!-- Custom CSS -->
<link href="<c:url value='/css/sb-admin-2.css'/>" rel="stylesheet" />

<!-- Custom Fonts -->
<link href="<c:url value='/css/font-awesome.min.css'/>" rel="stylesheet" />

<!-- DataTables CSS -->
<link href="<c:url value='/css/dataTables.bootstrap.css'/>"
	rel="stylesheet" />


<!-- DataTables Responsive CSS -->
<link href="<c:url value='/css/dataTables.responsive.css'/>"
	rel="stylesheet" />


<!-- autocomplete -->
<link href="<c:url value='/css/easy-autocomplete.min.css'/>"
	rel="stylesheet" />


<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

<!-- jQuery -->
<script src="<c:url value='/js/jquery.min.js'/>"></script>

<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>


<!-- Bootstrap Core JavaScript -->
<script src="<c:url value='/js/bootstrap.min.js'/>"></script>

<!-- Metis Menu Plugin JavaScript -->
<script src="<c:url value='/js/metisMenu.min.js'/>"></script>

<!-- Custom Theme JavaScript -->
<script src="<c:url value='/js/sb-admin-2.js'/>"></script>

<!-- DataTables JavaScript -->
<script src="<c:url value='/js/jquery.dataTables.min.js'/>"></script>
<script src="<c:url value='/js/dataTables.bootstrap.min.js'/>"></script>
<script src="<c:url value='/js/dataTables.responsive.js'/>"></script>

<!-- jquery validate -->
<script src="<c:url value='/js/jquery.validate.js'/>"></script>

<script type="text/javascript">
	jQuery.validator.addMethod("notEqual", function(value, element, param) {
		return this.optional(element) || value != param;
	}, "Please specify a different (non-default) value");

	 $(function(){
		 console.log('ejecutando jquery');
		 
 		<c:if test="${usuarioLogueado==null}">
 		 console.log('no hay sesion');	
 		$(location).attr('href','/');
 		
 	   </c:if>
 	
		});
	
</script>

<!--  autocomplete -->
<script src="<c:url value='/js/jquery.easy-autocomplete.min.js'/>"></script>

</head>

<body>

	<div id="wrapper">

		<!-- Navigation -->
		<nav class="navbar navbar-default navbar-static-top" role="navigation"
			style="margin-bottom: 0">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target=".navbar-collapse">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="/home">MG Services Unlimited</a>
			</div>
			<!-- /.navbar-header -->

			<ul class="nav navbar-top-links navbar-right">

				<!-- /.dropdown -->
				<li class="dropdown"><a class="dropdown-toggle"
					data-toggle="dropdown" href="#"> <i class="fa fa-user fa-fw"></i>
						<i class="fa fa-caret-down"></i>
				</a>
					<ul class="dropdown-menu dropdown-user">

						<li class="divider"></li>
						<li><a href="/logout"><i class="fa fa-sign-out fa-fw"></i>
								Logout</a></li>
					</ul> <!-- /.dropdown-user --></li>
				<!-- /.dropdown -->
			</ul>
			<!-- /.navbar-top-links -->

			<div class="navbar-default sidebar" role="navigation">
				<div class="sidebar-nav navbar-collapse">
					<ul class="nav" id="side-menu">

						<c:forEach items="${menu}" var="item" varStatus="row">


							<c:if test="${item.menuItem.type=='H'}">
								<ul class="nav nav-second-level">
							</c:if>
							<li><a
								href="${pageContext.request.contextPath}/<c:out value='${item.menuItem.uri}'/>"><c:out
										value="${item.menuItem.description}" /></a> <c:if
									test="${item.menuItem.type=='N'}"></li>
							</c:if>
							<c:if test="${item.menuItem.type=='H'}">
								</li>
					</ul>
					<c:if test="${menu[row.index+1].menuItem.type!='H'}">
						</li>
					</c:if>


					</c:if>
					</c:forEach>

					</ul>
				</div>
				<!-- /.sidebar-collapse -->
			</div>
			<!-- /.navbar-static-side -->
		</nav>