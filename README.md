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

### 4-1 커뮤니티 CURD 기능 
<br>

#### 4-1-1 자유게시판 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/freeboard.jsp)

<img src="https://github.com/msh45660/Final_Project_Team2/assets/116853287/57581691-4965-4fa5-a693-8a690f9fed44">
자유게시판의 경우 날짜별 타임라인 형식으로 글이 개제됩니다.<br><br>

수정은 별도의 폼을 통해 수정이 이루어집니다. [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/freeboardupdate.jsp)<br>

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

````
</details>

#### 4-1-2 경제 게시판 [코드확인](https://github.com/msh45660/Final_Project_Team2/blob/master/Chagok/src/main/webapp/WEB-INF/views/community/economy.jsp)
경제게시판은 

### 4-2 네이버 검색 API를 통한 경제 뉴스

### 4-3 댓글


