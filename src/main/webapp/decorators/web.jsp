<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<!-- ChatBot Context Path -->
	<meta name="contextPath" content="${pageContext.request.contextPath}" />

	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" />
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" />
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.css" />

	<link rel="stylesheet" href="<c:url value='/templates/web/css/style.css'/>" />

	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>

	<title><dec:title default="Trang chủ"/></title>
</head>
<body>
<%
	String uri = request.getRequestURI();
	boolean isAuthPage = uri.contains("login") || uri.contains("register");
%>

<% if (!isAuthPage) { %>
<%@ include file="/common/web/header.jsp"%>
<% } %>

<dec:body/>

<% if (!isAuthPage) { %>
<%@ include file="/common/web/footer.jsp"%>
<% } %>


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.js"></script>

<script src="<c:url value='/templates/web/js/swiper.js'/>"></script>

<%-- Nếu đã dùng jQuery CDN ở trên Head thì không cần dòng jquery.js local này nữa --%>
<%-- <script src="<c:url value='/templates/web/js/jquery.js'/>"></script> --%>

<script src="<c:url value='/templates/web/js/jquery-saleT5.js'/>"></script>
<script src="<c:url value='/templates/web/js/cart.js'/>"></script>
<script src="${pageContext.request.contextPath}/templates/web/js/checkout.js?v=<%= System.currentTimeMillis() %>"></script>
<!-- ChatBot JavaScript - Phải load sau jQuery -->
<script src="<c:url value='/templates/web/js/chatbot.js'/>"></script>
</body>
</html>