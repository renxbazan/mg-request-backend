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

    <title>Login</title>

    <!-- Bootstrap Core CSS -->
    <link href="<c:url value='/css/bootstrap.min.css'/>" rel="stylesheet"/>

    <!-- MetisMenu CSS -->
 	<link href="css/metisMenu.min.css" rel="stylesheet"/>
 	
    <!-- Custom CSS -->
	<link href="<c:url value='/css/sb-admin-2.css'/>" rel="stylesheet"/>
	
    <!-- Custom Fonts -->
	<link href="<c:url value='/css/font-awesome.min.css'/>" rel="stylesheet"/>
  
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    
    
      <!-- jQuery -->
	<script src="<c:url value='/js/jquery.min.js'/>"></script>
	
    <!-- Bootstrap Core JavaScript -->
	<script src="<c:url value='/js/bootstrap.min.js'/>"></script>
	
    <!-- Metis Menu Plugin JavaScript -->
	<script src="<c:url value='/js/metisMenu.min.js'/>"></script>
	
    <!-- Custom Theme JavaScript -->
	<script src="<c:url value='/js/sb-admin-2.js'/>"></script>
    
   	<script type="text/javascript">
   	$(function(){
   		
   		$("#username").attr("placeholder","Username");
   		$("#clave").attr("placeholder","Password");
   		
   		$("#btnIngresar").click(function(){
   			$(".form").submit();
   		});
   		
   	})
   	
   	</script>
    

</head>

<body>

    <div class="container">
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <div class="login-panel panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">
                          <div class="container" style='width:100px;height:100px;line-height:100px'>
                            <img class="resize_fit_center" src="<c:url value='/images/logo_mg.png'/>">
                          </div>
                        </h3>
                    </div>
                    <div class="panel-body">
                     	<form:form  method="POST" cssClass="form" modelAttribute="user" action="${pageContext.request.contextPath}/login/">
                            <fieldset>
                                <div class="form-group">
                                    <form:input path="username" id="username" cssClass="form-control" />
                                    
                                </div>
                                <div class="form-group">
                                      <form:password path="password" id="clave" cssClass="form-control"  />
                                
                                </div>
                                <div class="checkbox">
                                    <label>
                                        <input name="remember" type="checkbox" value="Remember Me">Remember me
                                    </label>
                                </div>
                                <!-- Change this to a button or input when using this as a form -->
                                <button class="btn btn-lg btn-success btn-block" id="btnIngresar">Login</button> 
                                
                                <c:if test="${message!=null}">
                                <div class="alert alert-danger">
                                  <c:out value="${message}"/>
                                </div>
                                </c:if>
                                
                            </fieldset>
                        </form:form>
                    </div>
                </div>
            </div>
        </div>
    </div>

  
</body>

</html>
