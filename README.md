# Final_Project_Team2

개인 자산관리 (가계부) 기능과 함께 도전하는 챌린지, 커뮤니티 기능을 전부 사용할 수 있는 자산관리 프로젝트입니다.

- 배포 주소 : [http://itwillbs10.cafe24.com/main](http://itwillbs10.cafe24.com/main)

****

## 목차
1. 프로젝트 소개
2. 사용한 기술 스택
3. 프로젝트 설계
4. 설계한 기능

****

### 1.프로젝트 소개
- 프로젝트 참여 인원 : 8명 (자산관리 3명/커뮤니티5명)
- 프로젝트 소개
````
    혼자 기록하고 관리하던 기존의 가계부에는 두가지 문제점이 있다고 생각했습니다.
    저축에 대한 큰 동기부여를 주기 어렵다라는 점
    저축 & 절약의 목표가 비슷한 여럿이서 함께 자산을 관리할 수 없다 라는 점
    두가지의 문제점을 고민하여
    유저들 간 서로 소통하며 함께 도전 할 수 있는 자산관리 사이트 구축을 목표로 하였습니다.
````
- 맡은 역할 : 커뮤니티 CRUD 기능, 네이버 검색 API를 이용한 뉴스 링크, 게시물 댓글 CRUD



****

### 2.사용한 기술 스택
Java 8 / Spring / Maven / MySQL / MyBatis <br>
Javascript / HTML5 / CSS3 / AJAX / Jquery <br>

****

### 3.프로젝트 설계
<br>

#### ERD 설계
<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/f42e90ed-e46e-476b-991e-054ff4246e79">
<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/0e83701e-efd3-41c1-8e94-0cab00df1fc2">
<br>

#### 요구사항 명세서
<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/c44cd153-8100-44ce-9d8d-68641dd571f7">
<br>

#### 유스케이스
<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/bad114a2-6b5e-4480-9bc8-2389e1b9e7d1">

****

### 4.설계한 기능
함께하는 자산관리 Chagok - 자산관리&커뮤니티 프로젝트에서 제가 설계한 기능입니다.
1. 커뮤니티 CRUD 기능
2. 네이버 검색 API를 통한 경제 관련 뉴스 링크 출력
3. 댓글

### 4-1 커뮤니티 CURD 기능 <br>
공지사항,자유게시판,뉴스·재테크 게시판(경제),후기게시판 및 커뮤니티 메인 페이지를 설계했습니다.<br>
각 게시판은 DB의 한 테이블에 같이 관리하며 각 게시판마다 sort 값을 통해 각 커뮤니티에 해당하는 글을 분류했습니다.<br>

#### 커뮤니티 메인 페이지 [코드보기](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/chagok/boardMain.jsp)

<br>
<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/02281c2d-f0d9-42c3-bbd6-bec5c38439e2">
커뮤니티 메인 페이지에서 모든 게시판의 최신글 5개를 확인할 수 있으며, 더보기를 통해 해당 게시판으로 갈 수 있게 설계했습니다.
<br>

#### 4-1-1 후기게시판 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/reviewboard.jsp)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/b41e4d7b-15d4-44c6-8499-b7278d965324">
커뮤니티의 메인인 챌린지에 대해 후기를 남길 수 있는 게시판입니다.<br>
챌린지가 끝난 후 결과 창에서도 후기를 남길 수 있으며, 

#### 4-1-2 자유게시판 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/freeboard.jsp)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/57581691-4965-4fa5-a693-8a690f9fed44">
자유게시판의 경우 날짜별 타임라인 형식으로 글이 개제됩니다.<br><br>

수정은 별도의 폼을 통해 수정이 이루어집니다. [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/freeboardupdate.jsp)<br>
세션 ID와 작성한 글의 ID와 일치할 시 수정 및 삭제 기능을 이용할 수 있습니다.<br>

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/1484a700-2540-4d9a-84bf-fc5244b9de84">
삭제 기능은 별도의 폼없이 삭제가 이루어집니다 <br><br>
<details>
    <summary>삭제 코드확인</summary>
	
````
	자유 게시판 삭제
@GetMapping(value = "/freedelete")
public String freedeleteGET(int bno,RedirectAttributes rttr,HttpSession session) throws Exception {
		
	Bservice.deleteBoard(bno);
				
	rttr.addFlashAttribute("result", "delOK");
				
	return "redirect:/freeboard";
}

서비스
public void deleteBoard(Integer bno) throws Exception;

임플리먼트
@Override
public void deleteBoard(Integer bno) throws Exception {
	mylog.debug(" deleteBoard() 호출 ");
		
	dao.deleteBoard(bno);
		
}	

매퍼
<delete id="deleteBoard">
	delete from board
	where bno = #{bno}
</delete>

글의 bno를 통해 해당글을 삭제하는 로직입니다.


````
</details>
<br><br>

#### 4-1-3 경제 게시판 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/economy.jsp)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/a52ed6e9-1a41-4790-b525-88e91b3ba4a9">
경제게시판은 API를 통한 '경제' 관련 뉴스를 검색하여 제목링크를 출력한 부분과<br>
경제 관련 글을 작성하는 게시판 부분으로 이루어져 있습니다.<br>
제목 클릭시 제목에 링크되어 있는 기사가 적힌 사이트로 넘어갑니다.<br>
<br><br><br>

경제 게시판 글 내용에서 세션ID와 작성자 ID가 동일할 시 수정 및 삭제 기능을 이용할 수 있습니다. [코드보기](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/economycontent.jsp)
 
<br>
또한, 관리자 또한 임의로 글을 삭제하거나 수정을 할 수 있습니다.<br>
<img src="(https://github.com/msh45660/Final_Project_Team2/assets/116853287/67bcdaa0-7d46-4415-bcaf-238ec7065b0a">

글 수정 시 별도의 폼을 통해 수정을 할 수 있으며, [코드보기](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/economyupdate.jsp)<br>

삭제는 클릭시 삭제가 완료됩니다.
<details>
    <summary>삭제 코드확인</summary>
	
````	
 경제 게시판 삭제
	
컨트롤러
@GetMapping(value = "/economydelete")
public String economydeleteGET(int bno,RedirectAttributes rttr,HttpSession session) throws Exception {
			
	Bservice.deleteBoard(bno);
					
	rttr.addFlashAttribute("result", "delOK");
					
	return "redirect:/economy?page=1";
}

서비스와 임플리먼트, 매퍼는 같은 로직을 사용했습니다.

````
</details>

#### 4-1-4 공지사항 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/notice.jsp)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/b66e0aa5-75d3-4e30-95d3-5fe985e562c2">
공지사항입니다. 일반적인 CRUD 기능을 사용했습니다.<br>
<br>

공지사항 상세 페이지 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/noticecontent.jsp)
<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/3a4c361f-f5e9-4d69-983f-b2e3898d8356">
기본적인 상세글보기 페이지입니다.<br>
세션ID와 글 작성자 ID를 비교하여 관리자일 경우에만 수정,삭제 기능이 보이도록 제어했습니다.<br>

수정 [코드보기](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/noticeupdate.jsp)

<details>
    <summary>삭제 코드확인</summary>

```` 
@PostMapping(value = "/noticedelete")
public String noticedeleteGET(int bno,RedirectAttributes rttr,HttpSession session) throws Exception {
			
	Bservice.deleteBoard(bno);
					
	rttr.addFlashAttribute("result", "delOK");
					
	return "redirect:/notice?page=1";
}

자유,경제 게시판과 같이 같은 코드를 사용합니다.

````
</details>


### 4-1-4 내가 쓴 글 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/chagok/myBoardWrite.jsp)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/78e83e1a-4aae-4956-a559-be3b1e77fdc3">
내가 쓴 글을 모아두는 페이지입니다.<br>
게시판 종류와 수정,삭제 유무를 선택할 수 있습니다.<br>
또한, 수정과 삭제 기능을 마이페이지에서 수행 할 수 있습니다.<br>

### 4-2 네이버 검색 API [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/java/com/chagok/controller/NewsAPIController.java)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/fc65d627-92e5-4892-b7d2-c7a1e7e43680">
네이버 검색 API 를 이용하여 "경제" 단어를 검색하여 해당 내용의 기사만 출력합니다.<br>
또한, 최신순으로 검색하여 보여줍니다.<br>
기사 클릭시 해당 네이버 기사 페이지로 자동 이동합니다.<br>

<details>
    <summary>검색 API 적용 페이지 코드</summary>
	
````
<table class="table table-hover" style="margin-left:10px; margin-right:10px; border-bottom: 1px solid #ddd;border-top: 2px solid #66BB7A;">
			  <thead style="background-color: #dddddd30;font-size: 16px;">
			    <tr>
			      <th scope="col" style="width:300px; padding: 15px 0;">경제 뉴스 기사</th>
			    </tr>
			  </thead>
			<tbody>
				  <tr>
				  	<td style="text-align:center; padding: 15px 0;">
					   <a href="#" id="news_test"></a>
						<div id="result"></div>
					</td>
				  </tr>
				
			</tbody>
	</table>

해당 코드를 통해 javascript 를 호출합니다.
호출한 스크립트는 아래와 같습니다.

<script type="text/javascript">
	$(document).ready(function(){
		var query = { "query":"경제" };
		
		$.ajax({
			type: 'get',
			url: '/news',
			contentType: "application/json; charset=UTF-8",
			dataType: 'text',
// 			data: JSON.stringify(query),
			success: function(data){
// 			alert("ajax 성공!");
			var job = JSON.parse(data);
			console.log(job);
			
			$('#result').empty();
			
			$.each(job, function(index, item){
				
				$('#result').append("<a href="+item.link+">"+item.title+"</a><br><br>");
				$('#result').append(item.description+"<br>");
				$('#result').append(item.pubDate+"<br>");
				$('#result').append("<hr>");
			});
			
		}, error: function(data){
			console.log("ajax 실패!");
			console.log(data);
		}
	});
		
	});

</script>	
````

</details>



