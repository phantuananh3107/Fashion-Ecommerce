<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<!-- Favicons -->
  <link href="<c:url value='/templates/web/images/logo.png'/>"  rel="icon" />
  <link href="<c:url value='/templates/web/images/logo.png'/>" rel="apple-touch-icon" />

  <!-- Google Fonts -->
  <link href="https://fonts.gstatic.com" rel="preconnect" />
  <link
    href="https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,600,600i,700,700i|Nunito:300,300i,400,400i,600,600i,700,700i|Poppins:300,300i,400,400i,500,500i,600,600i,700,700i"
    rel="stylesheet" />



  <!-- Vendor CSS Files -->
  <link href="<c:url value='/templates/admin/bootstrap/bootstrap.min.css'/>" rel="stylesheet" />
  <link href="<c:url value='/templates/admin/bootstrap-icons/bootstrap-icons.css'/>" rel="stylesheet" />
  <link href="<c:url value='/templates/admin/boxicons/css/boxicons.min.css'/>" rel="stylesheet" />
  <link href="<c:url value='/templates/admin/quill/quill.snow.css'/>" rel="stylesheet" />
  <link href="<c:url value='/templates/admin/quill/quill.bubble.css'/>" rel="stylesheet" />
  <link href="<c:url value='/templates/admin/remixicon/remixicon.css'/>" rel="stylesheet" />
  <link href="<c:url value='/templates/admin/simple-datatables/style.css'/>" rel="stylesheet" />
	
  <!-- Fontawesome -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"
    integrity="sha512-Evv84Mr4kqVGRNSgIGL/F/aIDqQb7xQ2vcrdIwxfjThSH8CSR7PBEakCr51Ck+w+/U6swU2Im1vVX0SVk9ABhg=="
    crossorigin="anonymous" referrerpolicy="no-referrer" />
  <!-- Template Main CSS File -->
  <link rel="stylesheet" href="<c:url value='/templates/admin/css/ngoquangha-order-list.css'/>">
  <link rel="stylesheet" href="<c:url value='/templates/admin/css/ngoquangha-order-edit.css'/>">
  <link rel="stylesheet" href="<c:url value='/templates/admin/css/manager-user.css'/>">

  <link href="<c:url value='/templates/admin/css/style.css'/>" rel="stylesheet" />
<title><dec:title default="Trang admin"/></title>
</head>
<body>
	<!-- Header -->
	<%@ include file="/common/admin/header.jsp"%>
	<!-- End Header -->
	
	<!-- Sidebar -->
	<%@ include file="/common/admin/sidebar.jsp"%>
	<!-- End Sidebar -->

	<dec:body/>
	

	<!-- Footer -->
	<%@ include file="/common/admin/footer.jsp"%>
	<!-- End Footer -->



	
	<!-- Vendor JS Files -->
  <script src="<c:url value='/templates/admin/apexcharts/apexcharts.min.js'/>"></script>
  <script src="<c:url value='/templates/admin/bootstrap/bootstrap.bundle.min.js'/>"></script>
  <script src="<c:url value='/templates/admin/chart.js/chart.umd.js'/>"></script>
  <script src="<c:url value='/templates/admin/echarts/echarts.min.js'/>"></script>
  <script src="<c:url value='/templates/admin/quill/quill.js'/>"></script>
  <script src="<c:url value='/templates/admin/simple-datatables/simple-datatables.js'/>"></script>
  <script src="<c:url value='/templates/admin/tinymce/tinymce.min.js'/>"></script>
  <script src="<c:url value='/templates/admin/php-email-form/validate.js'/>"></script>

  <!-- Template Main JS File -->
  <script src="<c:url value='/templates/admin/js/main.js'/>"></script>
  <script src="<c:url value='/templates/admin/js/products-list.js'/>"></script>
  <script src="<c:url value='/templates/admin/js/validate-product.js'/>"></script>
  <script src="<c:url value='/templates/admin/js/add-product.js'/>"></script>
   <script src="<c:url value='/templates/admin/js/repair-product.js'/>"></script>
  
</body>
</html>