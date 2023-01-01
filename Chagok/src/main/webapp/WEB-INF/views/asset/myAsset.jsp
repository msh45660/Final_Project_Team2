<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<%@ include file="../include/header.jsp" %>
<%@ include file="../include/sidebarAsset.jsp" %>
	
<script>
	function userCheck() {
		var cfm = confirm(" 사용자 인증이 필요한 서비스입니다.\n 인증 서비스로 이동 하시겠습니까? ");
		if (cfm) {
			return true;
		} else {
			return false;
		}
		
	}
</script>
	
	<c:if test="${userVO.isCheck.equals('N') }">
		<section class="content">
			<div class="error-page">
				<div class="error-content">
					<h3>
						<i class="fa fa-warning text-yellow"></i> 등록된 자산정보가 없습니다!
					</h3>
					<p style="margin-left: auto; margin-right: auto;">
						자산 정보를 불러오시겠습니까?
					</p>
					<form action="https://testapi.openbanking.or.kr/oauth/2.0/authorize" method="get" onsubmit="return userCheck();">
						<!-- 고정값 : code -->
						<input type="hidden" name="response_type" value="code">
						<!-- 오픈뱅킹에서 발급한 이용기관 앱의 Client ID -->
						<input type="hidden" name="client_id" value="d357debb-9ab8-401d-a882-ce684fb5b3f7">
						<!-- 사용자 인증이 성공하면 이용기관으로 연결되는 URL callback_uri -->
						<input type="hidden" name="redirect_uri" value="http://localhost:8080/asset/callback">
						<!-- Access Token 권한 범위 -->
						<input type="hidden" name="scope" value="login inquiry transfer cardinfo">
						<!-- 보안위협에 대응하기 위해 세팅하는 난수값 (32자리) -->
						<input type="hidden" name="state" value="12345678123456781234567812345678">
						<!-- 사용자인증타입 구분주 2) (0:최초인증, 1:재인증, 2:인증생략) -->
						<input type="hidden" name="auth_type" value="0">
						
						<input type="submit" class="btn btn-block btn-success btn-lg" id="assetCheck" 
						style="width: 200px; margin: 20px 40px; background-color: #FFDB83; border-color: #1e282c" value="내 자산 불러오기">	
					</form>
				</div>
			</div>
		</section>
	</c:if>
	
	<c:if test="${userVO.isCheck.equals('Y') }">
		카드 / 계좌 정보 불러와서 출력
	</c:if>


<h1 style="margin: 100px 20px 0 0"> 여기는 임시 </h1>
	
	<form action="https://testapi.openbanking.or.kr/oauth/2.0/authorize" method="get" onsubmit="return userCheck();">
		<!-- 고정값 : code -->
		<input type="hidden" name="response_type" value="code">
		<!-- 오픈뱅킹에서 발급한 이용기관 앱의 Client ID -->
		<input type="hidden" name="client_id" value="d357debb-9ab8-401d-a882-ce684fb5b3f7">
		<!-- 사용자 인증이 성공하면 이용기관으로 연결되는 URL callback_uri -->
		<input type="hidden" name="redirect_uri" value="http://localhost:8080/asset/callback">
		<!-- Access Token 권한 범위 -->
		<input type="hidden" name="scope" value="login inquiry transfer cardinfo">
		<!-- 보안위협에 대응하기 위해 세팅하는 난수값 (32자리) -->
		<input type="hidden" name="state" value="12345678123456781234567812345678">
		<!-- 사용자인증타입 구분주 2) (0:최초인증, 1:재인증, 2:인증생략) -->
		<input type="hidden" name="auth_type" value="0">
		
		<input type="submit" class="btn btn-block btn-success btn-lg" id="assetCheck" 
		style="width: 200px; margin: 20px 40px" value="내 자산 불러오기">	
	</form>
	
	
	<form action="https://testapi.openbanking.or.kr/oauth/2.0/authorize" method="get" onsubmit="return userCheck();">
		<!-- 고정값 : code -->
		<input type="hidden" name="response_type" value="code">
		<!-- 오픈뱅킹에서 발급한 이용기관 앱의 Client ID -->
		<input type="hidden" name="client_id" value="d357debb-9ab8-401d-a882-ce684fb5b3f7">
		<!-- 사용자 인증이 성공하면 이용기관으로 연결되는 URL callback_uri -->
		<input type="hidden" name="redirect_uri" value="http://localhost:8080/asset/callbackCard">
		<!-- Access Token 권한 범위 -->
		<input type="hidden" name="scope" value="login cardinfo">
		<!-- 보안위협에 대응하기 위해 세팅하는 난수값 (32자리) -->
		<input type="hidden" name="state" value="12345678123456781234567812345678">
		<!-- 사용자인증타입 구분주 2) (0:최초인증, 1:재인증, 2:인증생략) -->
		<input type="hidden" name="auth_type" value="0">
		<!-- 카드 인증 -->
		<input type="hidden" name="register_info" value="C">
		
		<input type="submit" class="btn btn-block btn-success btn-lg" id="assetCheck" 
		style="width: 200px; margin: 20px 40px" value="카드 불러오기(미완성)">	
	</form>
	
	
	
	<form action="https://testapi.openbanking.or.kr/oauth/2.0/authorize" method="get" onsubmit="return userCheck();">
		<!-- 고정값 : code -->
		<input type="hidden" name="response_type" value="code">
		<!-- 오픈뱅킹에서 발급한 이용기관 앱의 Client ID -->
		<input type="hidden" name="client_id" value="d357debb-9ab8-401d-a882-ce684fb5b3f7">
		<!-- 사용자 인증이 성공하면 이용기관으로 연결되는 URL callback_uri -->
		<input type="hidden" name="redirect_uri" value="http://localhost:8080/asset/callbackCenter">
		<!-- Access Token 권한 범위 -->
		<input type="hidden" name="scope" value="login inquiry transfer">
		<!-- 보안위협에 대응하기 위해 세팅하는 난수값 (32자리) -->
		<input type="hidden" name="state" value="12345678123456781234567812345678">
		<!-- 사용자인증타입 구분주 2) (0:최초인증, 1:재인증, 2:인증생략) -->
		<input type="hidden" name="auth_type" value="0">
		
		<input type="submit" class="btn btn-block btn-success btn-lg" id="assetCheck" 
		style="width: 200px; margin: 20px 40px" value="센터 인증 기관용(미완성)">	
	</form>
	
</div>
<%@ include file="../include/footer.jsp" %>