<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>


<%@ taglib prefix="c" uri="jakarta.tags.core"%>


<!DOCTYPE html>


<html>


<head>


<meta charset="UTF-8">


<title>Insert title here</title>


</head>


<body>


	<section class="row">


		<div class="col-6 offset-3 mt-4">


			<form action="<c:url value="/admin/categories/saveOrUpdate" />"
				method="POST">


				<div class="card">


					<div class="card-header">


						<h2>Add New Category</h2>


					</div>


					<div class="card-body">


						<div class="mb-3">


							<label for="categoryId" class="form-label">Category ID:</label> <input
								type="text" readonly="readonly" class="form-control"
								value="${category.id}" id="id" name="id"
								aria-describedby="categoryIdid" placeholder="Category Id">



						</div>


						<div class="mb-3">


							<label for="categoryname" class="form-label">Category
								Name:</label> <input type="text" class="form-control"
								value="${category.categoryName}" id="name" name="categoryName"
								aria-describedby="nameid" placeholder="Category Name">



						</div>


					</div>


				</div>


				<div class="card-footer text-muted">


					<a href="<c:url value="/admin/categories/add"/>"
						class="btn btn-secondary"><i class="fas fa-new"></i> New</a> <a
						href="<c:url value="/admin/categories" />" class="btn btn-success"><i
						class="fas fa-bars"></i> List Categories</a>


					<button class="btn btn-primary" type="submit">
						<i class="fas fa-save"></i> <span>Submit</span>





					</button>



				</div>




			</form>




		</div>



	</section>


</body>


</html>