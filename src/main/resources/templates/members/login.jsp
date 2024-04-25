<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<html>
	<head> <meta charset="utf-8"> 
		<title>Login Pages</title>
		<style>
		hr {
			margin:0%;
		}
		textarea {
			width: 100%;
			height: 40px;
			border: none;
			background-color: white;
			border-radius: 10px;
			font-size: 20px;
		}
		input {
			width: 100%;
			height: 50px;
			font-size: 20px;
			border: none;
			border-radius: 10px;
			background-color: white;
		}
		.header {
			border-bottom: 8px solid #483D8B;
			text-align: center;
			margin-bottom: 30px;
		}
		.loginBtn {
			width: 150px;
			height: 100px;
			background-color: blue;
			font-size: 30px;
			color: white;
			border: none;
			border-radius: 10px;
			margin-top: 50px;
		}
		.memberBtn {
			width: 100%;
			height: 50px;
			font-size: 20px;
			border: none;
			border-radius: 10px;
			background-color: white;
		}
		.container {
			display: flex;
			justify-content: center; /* 수평 가운데 정렬 */
		}
		.loginDiv {
			display: inline-block;
			max-width: 500px;
			width: 50%;
			padding: 5px 10px;
			margin: 5px 5px;
		}
		.member {
			display: inline-block;
			max-width: 500px;
			width: 50%;
			height: 70vh;
			background-color: white;
			padding: 5px 10px;
			border-radius: 10px;
			margin: 5px 5px;
			overflow-y: scroll;
		}
		</style>
	</head>
	<body style="background-color:#1E90FF;">
		<div class="header">
			<h1>로그인</h1>
		</div>
		<div class="container">
			<div class="loginDiv">
				<form action="/login" method="post" style="margin-top: 50px;">
						<h3>아이디 :</h3>
						<input type="text" name="memberId" id="memberId">
						<h3>이름 :</h3>
						<input type="text" name="name" id="name" autoComplete="on">
						<h3>비밀번호 :</h3>
						<input type="password" name="password" id="password">
						<div style="display:flex; justify-content: center; align-items: center;">
							<button class="loginBtn" type="submit"> 로그인 </button>
						</div>
				</form>
			
			</div>
			<div class="member">
				<a th:each="member : ${members}">
					<button class="memberBtn" type="button" th:id="${member.id}" onclick="userChoose( '${member.memberId}', '${member.name}')">${member.name}</button><hr>
				</a>
			</div>
		</div>
		<script>
			function userChoose(id, name) {
				document.getElementById("memberId").value = id;
				document.getElementById("name").value = name;
			}
		</script>
	</body>
</html>
