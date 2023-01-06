package com.chagok.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chagok.apiDomain.AccountHistoryRequestVO;
import com.chagok.apiDomain.AccountHistoryResponseVO;
import com.chagok.apiDomain.AccountHistoryVO;
import com.chagok.apiDomain.AccountVO;
import com.chagok.apiDomain.CardHistoryVO;
import com.chagok.apiDomain.CardInfoRequestVO;
import com.chagok.apiDomain.CardInfoResponseVO;
import com.chagok.apiDomain.CardInfoVO;
import com.chagok.apiDomain.RequestTokenVO;
import com.chagok.apiDomain.ResponseTokenVO;
import com.chagok.apiDomain.UserInfoResponseVO;
import com.chagok.domain.AbookVO;
import com.chagok.domain.CategoryVO;
import com.chagok.domain.ChallengeVO;
import com.chagok.domain.JsonObj;
import com.chagok.domain.PropCardVO;
import com.chagok.domain.UserVO;
import com.chagok.persistence.PlanVO;
import com.chagok.service.AbookService;
import com.chagok.service.AccountService;
import com.chagok.service.OpenBankingService;
import com.chagok.service.ReportService;
import com.chagok.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/asset/*")
public class AssetController {
	
	private static final Logger mylog = LoggerFactory.getLogger(AssetController.class);

	
	///////////////////영민////////////////////
	// http://localhost:8080/asset/myAsset
	@Inject
	private OpenBankingService openBankingService;
	
	@Inject
	private AccountService accountService; 
	
	@Inject
	private UserService userService;
	
	@GetMapping("/myAsset")
	public String myAssetGET(HttpSession session, Model model) throws Exception{
		if (session.getAttribute("mno") != null) {
			int mno = (int)session.getAttribute("mno");
			
			UserVO userVO = userService.getUser(mno);
			model.addAttribute("userVO", userVO);
			
			// 현재 년월
			LocalDate now = LocalDate.now();
			String now_date = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
			model.addAttribute("now_date", now_date);
			
			// 계좌 리스트 조회
			List<AccountVO> accountList = accountService.getAccountInfo(mno);
			model.addAttribute("accountList", accountList);
			
			// 카드 리스트 조회
			List<CardInfoVO> cardList = accountService.getCardInfo(userVO.getUser_seq_no());
			model.addAttribute("cardList", cardList);
			
			// 카드 내역/금액 조회
			List<List<CardHistoryVO>> cardHistoryList = accountService.getCardHistory(cardList);
			model.addAttribute("cardHistoryList", cardHistoryList);
		}
		
		
		return "/asset/myAsset";
	}
	
	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	public String getToken(RequestTokenVO requestTokenVO, Model model, CardInfoRequestVO cardInfoRequestVO, 
			HttpSession session) throws Exception {
		//////////////// 사용자인증 API (3-legged) ////////////////
		
		// 현재 시간정보
		LocalDate now = LocalDate.now();
		String from_date = now.minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String to_date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		
		// 토큰 생성
		ResponseTokenVO responseTokenVO = openBankingService.requestToken(requestTokenVO);
		
		// 정보를 들고 jsp 이동 (model 객체)
		model.addAttribute("responseTokenVO", responseTokenVO);
			
		if (responseTokenVO != null) {
			int mno = (int)session.getAttribute("mno");
			
			// isChecked N => Y update
			userService.updateIsCheck(mno);
			
			// user_seq_no 저장
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("mno", mno);
			userMap.put("user_seq_no", responseTokenVO.getUser_seq_no());
			userService.updateSeqNo(userMap);
			
			//////////////// 사용자 정보, 계좌정보 조회 => DB(user, account 테이블)에 저장 ////////////////
			UserInfoResponseVO userInfoResponseVO = openBankingService.getUserInfo(responseTokenVO);
			for (int k = 0; k < userInfoResponseVO.getRes_list().size(); k++) {
				userInfoResponseVO.getRes_list().get(k).setMno(mno);
			}
			// 사용자 정보
			model.addAttribute("userInfoResponseVO", userInfoResponseVO);
			// 계좌 정보
			List<AccountVO> accountList = userInfoResponseVO.getRes_list();
			model.addAttribute("accountList", accountList);
			
			if (userInfoResponseVO.getRes_list() != null) {
				accountService.insertAccountInfo(userInfoResponseVO.getRes_list()); // 디비 저장
			}
			
			mylog.debug("계좌 리스트 : " + accountList);
			
			//////////////// 계좌 거래내역 조회 => DB(account_history 테이블)에 저장 ////////////////
			List<AccountHistoryRequestVO> accountHistoryRequestList = new ArrayList<AccountHistoryRequestVO>();
			for (int i = 0; i < accountList.size(); i++) {
				AccountHistoryRequestVO accountHistoryRequestVO = new AccountHistoryRequestVO();
				
				accountHistoryRequestVO.setAccess_token(responseTokenVO.getAccess_token());
				accountHistoryRequestVO.setBank_tran_id("M202202513U"+(int)((Math.random()+1)*100000000));
				mylog.debug("@@@@@@@@@@@@@@"+i+accountHistoryRequestVO.getBank_tran_id());
				accountHistoryRequestVO.setFintech_use_num(accountList.get(i).getFintech_use_num());
				accountHistoryRequestVO.setInquiry_type("A");
				accountHistoryRequestVO.setInquiry_base("D");
				accountHistoryRequestVO.setFrom_date(from_date);
				accountHistoryRequestVO.setTo_date(to_date);
				accountHistoryRequestVO.setSort_order("D");
				
				accountHistoryRequestList.add(accountHistoryRequestVO);
			}
			
			List<AccountHistoryResponseVO> accountHistoryResponseList = openBankingService.getAccountHistory(accountHistoryRequestList);
			model.addAttribute("accountHistoryResponseList", accountHistoryResponseList);
			
			if (accountHistoryRequestList != null) {
				accountService.insertAccountHistory(accountHistoryResponseList); // 디비 저장
			}
			
			//////////////// 카드목록 조회 => DB(card 테이블)에 저장 ////////////////
			
			cardInfoRequestVO.setAccess_token(responseTokenVO.getAccess_token());
			cardInfoRequestVO.setBank_tran_id("M202202513U"+(int)((Math.random()+1)*100000000));
			cardInfoRequestVO.setUser_seq_no(responseTokenVO.getUser_seq_no());
			cardInfoRequestVO.setBank_code_std("399"); // fix, 오픈뱅킹만 사용가능
			cardInfoRequestVO.setMember_bank_code("399"); // fix, 오픈뱅킹만 사용가능
			CardInfoResponseVO cardInfoResponseVO = openBankingService.getCardInfo(cardInfoRequestVO);
			
			model.addAttribute("cardInfoResponseVO", cardInfoResponseVO);
			if (cardInfoResponseVO != null) {
				accountService.insertCardInfo(cardInfoResponseVO);
			}
			
			// 계좌 금액 조회
			if (userInfoResponseVO.getRes_list() != null) {
				// mno에 해당하는 계좌들의 가장최근 거래내역 조회
				List<AccountHistoryVO> accountHistoryList = accountService.getBalanceAmt(mno);
				model.addAttribute("accountHistoryList", accountHistoryList);
				
				// 최근 거래금액을 각각 계좌에 업데이트
				List<AccountVO> updateBalanceList = new ArrayList<AccountVO>();
				for (int i = 0; i < accountHistoryList.size(); i++) {
					if (accountHistoryList.get(i) != null) {
						AccountVO accountVO = new AccountVO();
						accountVO.setFintech_use_num(accountHistoryList.get(i).getFintech_use_num());
						accountVO.setBalance_amt(accountHistoryList.get(i).getAfter_balance_amt());
						updateBalanceList.add(accountVO);
					}
				}
				accountService.updateBalanceAmt(updateBalanceList);
			}
			
			
			
			////////////////카드청구기본정보 조회 => DB(card_history 테이블)에 저장 ////////////////
			
			
//			bank_tran_id Y AN(20) 거래고유번호(참가기관) - 「3.11. 거래고유번호(참가기관) 생성 안내」 참조
//			user_seq_no Y AN(10) 사용자일련번호
//			bank_code_std Y AN(3) 카드사 대표코드 (금융기관 공동코드) - 「3.3. 금융기관코드」 의 ‘카드사’ 참조
//			member_bank_code Y AN(3) 회원 금융회사 코드 (금융기관 공동코드) - 「3.3. 금융기관코드」 의 ‘카드사’ 참조
//			card_id 
			
		}
//		return "/asset/apiTest"; // 출력 테스트
		return "redirect:/asset/myAsset";
	}
	
	@GetMapping("/callbackCard")
	public String registCard(RequestTokenVO requestTokenVO, Model model) throws Exception{
		
		model.addAttribute("requestTokenVO", requestTokenVO);
		
		return "/asset/cardTest";
	}
	
	
	
	
	
	@RequestMapping(value = "/callbackCenter", method = RequestMethod.GET)
	public String getTokenCenter(RequestTokenVO requestTokenVO, Model model) throws Exception{
		////////////////사용자인증 API (2-legged) ////////////////
				
		ResponseTokenVO responseTokenVO = openBankingService.requestTokenCenter(requestTokenVO);
		
		// 정보를 들고 jsp 이동 (model 객체)
		model.addAttribute("responseTokenVO", responseTokenVO);
		
		if (responseTokenVO != null) {
			//////////////// 사용자 정보 조회 => DB(member 테이블)에 저장 ////////////////
			UserInfoResponseVO userInfoResponseVO = openBankingService.getUserInfo(responseTokenVO);
			// 사용자 정보
			model.addAttribute("userInfoResponseVO", userInfoResponseVO);
			// 계좌 정보
			model.addAttribute("accountList", userInfoResponseVO.getRes_list());
			//////////////// 계좌 정보 조회 => DB(member 테이블)에 저장 ////////////////
			
		}
		return "/asset/apiTest";
	}
	
	
	///////////////////영민////////////////////

	
	
	///////////////////세영//////////////////////
	//서비스 객체 주입
	@Inject
	private AbookService service;
	
	
	@ResponseBody
	@RequestMapping("/gtest")
	public JsonObj test (
			@RequestParam(value = "page", required=false) String page,//page : 몇번째 페이지를 요청했는지
			@RequestParam(value = "rows", required=false) String rows,//rows : 페이지 당 몇개의 행이 보여질건지
			@RequestParam(value  = "sidx", required=false) String sidx,//sidx : 소팅하는 기준이 되는 인덱스
			@RequestParam(value = "sord", required=false) String sord
			) throws Exception {//sord : 내림차순 또는 오름차순
	    	
		mylog.debug("json controller 실행 시작");
//		mylog.debug("mno@@@@@@@@:"+mno);
		
		JsonObj obj = new JsonObj();
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
// List<AbookVO> abookList = service.getAbookList(mno);
		List<?> list2 = service.AbookList();
		mylog.debug("list2 출력해보기"+list2);
		
		int int_page = Integer.parseInt(page);// 1 2 3
		int perPageNum = (int)Double.parseDouble(rows);
		
		// db에서 가져온 데이터의 갯수가 10개라고 가정하고 임의로 수행한다. 	그럼 이 키값들을 멤버로 하는 클래스를 가지고 있어야 할 것같다..
//		for(int i= (int_page-1)*perPageNum+1 ; i<(int_page*perPageNum) ; i++){
		for(int i=0; i<list2.size(); i++){
			AbookVO vo = (AbookVO) list2.get(i);
			// mno 돌아가게 하기 
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("abno", vo.getAbno());
			map.put("ab_inout", vo.getAb_inout());
			map.put("ab_date", vo.getAb_date());
			map.put("ab_content", vo.getAb_content());
			map.put("ab_amount", vo.getAb_amount());
			map.put("ct_top", vo.getCt_top());
			map.put("ct_bottom", vo.getCt_bottom());
			map.put("ab_memo", vo.getAb_memo());
			
			list.add(map);
	}
//		}
	       
	    obj.setRows(list);  // list<map>형태의 받아온 데이터를 가공해서 셋( 그리드에 뿌려줄 행 데이터들 )
	    	    
	    //page : 현재 페이지
	    obj.setPage(int_page);// 현재 페이지를 매개변수로 넘어온 page로 지정해준다. 
		
	    //records : 보여지는 데이터 개수
	    obj.setRecords(list.size());//?
		
	    //total : rows에 의한 총 페이지수
		// 총 페이지 갯수는 데이터 갯수 / 한페이지에 보여줄 갯수 이런 식
		int totalPage = (int)Math.ceil(list.size()/Double.parseDouble(rows));
		obj.setTotal( totalPage ); // 총 페이지 수 (마지막 페이지 번호)
		mylog.debug("실험 중"+obj);
		
	    return obj;
	}
	


	/////////////////////////////////////////////////// 실험중

//	http://localhost:8080/asset/abookList?mno=1
//	http://localhost:8080/asset/abookList
	@GetMapping("/abookList")
//	@ResponseBody
	public String abookList(@RequestParam("mno") int mno, HttpSession session,Model model, HttpServletRequest req, HttpServletResponse res) throws Exception {
		mylog.debug(" /abooklist 호출 -> DB 출력 ");
		
//		// 로그인 확인
//		if (session.getAttribute("mno") != null) {
//			int mno = (int)session.getAttribute("mno");
//		
//		UserVO userVO = userService.getUser(mno);
//		model.addAttribute("userVO", userVO);
//		}
//		
		// 서비스 -> DAO 게시판 리스트 가져오기
		List<AbookVO> abookList = service.getAbookList(mno);
		List<CategoryVO> cateList = service.CateList();
//		mylog.debug("Controller+@@@@@@@@@@@@@@@@@@@2"+cateList);		
		
		///////////////////////////////////objmapper, gson 시도
		ObjectMapper mapper = new ObjectMapper();
//
		String jsonAbook = mapper.writeValueAsString(abookList);
		String jsonCate = mapper.writeValueAsString(cateList);
	
        //////////////////////////////////////////////////////
//		model.addAttribute("abookList", gson.toJson(abookList));
		model.addAttribute("cateList", cateList);
		model.addAttribute("jsonAbook",jsonAbook);
		model.addAttribute("jsonCate",jsonCate);
//		model.addAttribute("j_abookList",j_abookList);
		
		return "asset/abookList"; 
//		return map;
	}

	@RequestMapping(value = "/test",method = RequestMethod.GET)
	public void restTest1GET() throws Exception {
		mylog.debug("test(get)호출");
	}

	@RequestMapping(value = "/testhome",method = RequestMethod.POST)
	public void restTest1POST(AbookVO vo) throws Exception {
		mylog.debug(" test.jsp 갔다가 옴 + @@@@@@@@@@@@@@@");
		mylog.debug("**"+vo);
		
	}
	
//	@RequestMapping(value="saveJqgrid.do")
//    @ResponseBody
//    public String saveJqgrid(@RequestParam String param1) throws Exception {
//        String result = "";
//        try {
//            param1 = param1.replaceAll("&quot;", "\"");
//            
//            JSONArray jsonArray = new JSONArray(param1);
//                                            
//            jqgridService.saveJqGridTx(jsonArray);
//            
//            result = "SUCCESS";
//                    
//        } catch (Exception e) {
//            result = "FAIL";
//        }
//        
//        return result;
//    }
	
	@RequestMapping(value="/getGrid", method = RequestMethod.POST, produces="application/json;charset=utf-8")
	@ResponseBody
		public void getGrid (@RequestParam Map<String,Object> rows, RedirectAttributes rttr) throws Exception {
			
			mylog.debug("getGrid@@@@@"+rows);	        
		 
		 ObjectMapper mapper = new ObjectMapper();
//		 AbookVO vo = mapper.readValue(param, AbookVO.class);
		 AbookVO vo = mapper.convertValue(rows, AbookVO.class);
		 
		 // 서비스 - DAO : 정보 수정 메서드 호출
//		 Integer result = service.updateAbook(vo);
//		 
//			if(result > 0) {
//				// "수정완료" - 정보 전달 
//					rttr.addFlashAttribute("result", "modOK");
//				}
//				// 페이지 이동(/board/list) 
				
			mylog.debug("수정 처리 완료!!");
		 
	}
	
	
	@RequestMapping("/getGrid2")
	 @ResponseBody
	 public Object testList(HttpServletRequest request,@RequestBody List<Map<String, Object>> list) throws Exception {
		
//		mylog.debug("**map이 되냐?"+param);
		mylog.debug("*****2지금하는방식"+list);
		  String result = "ok";
		  String resultMsg = "";
		  JSONArray arr = new JSONArray();
		  
//			ObjectMapper mapper = new ObjectMapper();
//			List<Map<String, Object>> list = mapper.readValue(companiesJsonStr, new TypeReference<List<Map<String, Object>>>() {});
//
//
//			for (AbookVO vo :) {
//				mylog.debug("mapper"+vo.get(index));
//			}

		  
//		  mylog.debug("0번째 배열!!!!"+list.get(0));
		  mylog.debug("0번째 배열!!!!"+list.get(3).get("ab_content").toString());
		  
//		  for(i=0;i<list.size();i++) {
//			  list.get(i).get("abno");
//		  }
		  
		List<AbookVO> list7 = new ArrayList<AbookVO>();
		
		for(int i=0;i<list.size();i++) {
	
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("abno", list.get(0).get("abno"));
		map.put("ab_inout", list.get(0).get("ab_inout"));
		map.put("ab_date", list.get(0).get("ab_date"));
		map.put("ab_content", list.get(0).get("ab_content"));
		map.put("ab_amount", list.get(0).get("ab_amount"));
		map.put("ct_top", list.get(0).get("ct_top"));
		map.put("ct_bottom",list.get(0).get("ct_bottom"));
		map.put("ab_memo", list.get(0).get("ab_memo"));
		
//		list7.add(map);
		}
			
		mylog.debug("maplist 실험 중"+list7);
		mylog.debug("끝");
			
			
	Object a = new Object();
	
	Map<String,Object> map2 = new HashMap<String,Object>();
	  try {   
	   for(Map<String, Object> tList : list) {
	    AbookVO vo = new AbookVO() ;
	    vo.setAbno(Integer.parseInt(tList.get("abno").toString()));
//	    vo.setMno(Integer.parseInt(tList.get("mno").toString()));
	    vo.setAb_inout(Integer.parseInt(tList.get("ab_inout").toString()));
	    
	    vo.setAb_date(tList.get("ab_date").toString());
	    vo.setAb_content(tList.get("ab_content").toString());
	    vo.setAb_memo(tList.get("ab_memo").toString());
	    vo.setAb_amount(Integer.parseInt(tList.get("ab_amount").toString()));
	    vo.setAb_method(tList.get("ab_method").toString());
	    vo.setCt_top(tList.get("ct_top").toString());
	    vo.setCt_bottom(tList.get("ct_bottom").toString());
	    
	    
	    mylog.debug("%%%%%%%%%%%%"+vo);
	    
	    
	    
//	    	    aVO.setMenu_id(tList.get("menu_id").toString());
//	    aVO.setUse_yn(tList.get("use_yn").toString());
	 
//	    authMgmtService.mergeAuthMgmtList(aVO) ;
	   }
//	    mylog.debug("d"+vo);
//	   result = "success";
//	   resultMsg = "성공" ;
//	    
	  } catch (Exception e) {
//	   result = "failure";
//	   resultMsg = "실패" ;
	  }
//	   
//	  resultMap.put("result", result);
//	  resultMap.put("resultMsg", resultMsg);
	   
	  return a;
	}
	
	
	
	
	///////////////////MJ////////////////////
	
	@Inject
	private ReportService rptService;
	@Inject
	private AbookService abService;
//	http://localhost:8080/assetmain
//	http://localhost:8080/asset/dtRpt
	@GetMapping(value = "/dtRpt")
	public String dateReport(HttpSession session, Model model) throws Exception {
		// 로그인 확인
		if(session.getAttribute("mno")==null) {
			return "/chagok/login";
		}
		int mno = (int)session.getAttribute("mno");
		UserVO userVO = userService.getUser(mno);
		
		mylog.debug("mno : "+mno);
//		int mno = (int)session.getAttribute("mno");
		
		/////////////// 1. service에서 DB 가져오기 ///////////////
		// 1. 이번달 총 지출
		Integer dtSum1 = rptService.dtSum1(mno);
		mylog.debug("dtSum1 : "+dtSum1);
		
		// 2. 지난달 총 지출
		Integer dtSum2 = rptService.dtSum2(mno);
		mylog.debug("dtSum2 : "+dtSum2);
		
		// 3. 이번달 평균 지출
		Integer dtAvg1 = rptService.dtAvg1(mno);
		mylog.debug("dtAvg1 : "+dtAvg1);
		
		// 4. 지난달 평균 지출
		Integer dtAvg2 = rptService.dtAvg2(mno);
		mylog.debug("dtAvg2 : "+dtAvg2);
		
		// 5. 이번달 예상 지출
		Integer expSum = rptService.expSum(mno);
		mylog.debug("expSum : "+expSum);
		
		// 6. 이번달 총 수입
		Integer dtSumIn = rptService.dtSumIn(mno);
		mylog.debug("dtSumIn : "+dtSumIn);
		
		// 7. 이번달 무지출 일수
		Integer noOut = rptService.noOut(mno);
		mylog.debug("noOut : "+noOut);
		
		// 8. 이번달 결제 건수(지출 횟수)
		Integer outCnt = rptService.outCnt(mno);
		mylog.debug("outCnt : "+outCnt);
		
		// 9. 이번달 누적 지출
		List<Map<String, Object>> outCum = rptService.outCum(mno);
		mylog.debug("outCum : "+outCum.size());
		
		// 10. 일간 통계
		List<Map<String, Object>> day = rptService.day(mno);
		mylog.debug("day : "+day.size());
		
		// 11. 주간 통계
		List<Map<String, Object>> week = rptService.week(mno);
		mylog.debug("week : "+week.size());
		
		// 12. 월간 통계
		List<Map<String, Object>> month = rptService.month(mno);
		mylog.debug("month : "+month.size());
		
		// 13. 지출액 TOP 4
		List<Map<String, Object>> amtTop = rptService.amtTop(mno);
		mylog.debug("amtTop : "+amtTop.size());
		
		// 14. 지출횟수 TOP 4
		List<Map<String, Object>> cntTop = rptService.cntTop(mno);
		mylog.debug("cntTop : "+cntTop.size());		
		
		/////////////// 2. List<Map> -> JsonArray ///////////////
		String outCumjson = rptService.listMapToJson(outCum);
		String dayjson = rptService.listMapToJson(day);
		String weekjson = rptService.listMapToJson(week);
		String monthjson = rptService.listMapToJson(month);
		String amtTopjson = rptService.listMapToJson(amtTop);
		String cntTopjson = rptService.listMapToJson(cntTop);
		
		/////////////// 3. model로 전달 ///////////////
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dtSum1", dtSum1);
		map.put("dtSum2", dtSum2);
		map.put("dtAvg1", dtAvg1);
		map.put("dtAvg2", dtAvg2);
		map.put("expSum", expSum);
		map.put("dtSumIn", dtSumIn);
		map.put("noOut", noOut);
		map.put("outCnt", outCnt);
		map.put("outCumjson", outCumjson);
		map.put("dayjson", dayjson);
		map.put("weekjson", weekjson);
		map.put("monthjson", monthjson);
		map.put("amtTopjson", amtTopjson);
		map.put("cntTopjson", cntTopjson);
		model.addAttribute("map", map);
		model.addAttribute("userVO", userVO);
		
		return "/asset/dateReport";
	}	
	
	
//	http://localhost:8080/asset/ctRpt
	@GetMapping(value = "/ctRpt")
	public String cateReport(HttpSession session, Model model) throws Exception {
		// 로그인 확인
		int mno = (int)session.getAttribute("mno");
		UserVO userVO = userService.getUser(mno);
		if(mno==0) {
			return "/chagok/login";
		}
		mylog.debug("mno : "+mno);
		
		/////////////// 1. service에서 DB 가져오기 ///////////////
		// 1. 최다 지출 카테고리
		List<Map<String, Object>> cateCntList = rptService.cateCnt(mno);
		mylog.debug("cateCntList : "+cateCntList.size());
//		
		// 2. 최대 지출 카테고리
		List<Map<String, Object>> cateSumList = rptService.cateSum(mno);
		mylog.debug("cateSumList : "+cateSumList.size());
		
		// 3. 챌린지 추천
		List<ChallengeVO> chRandList = rptService.chRand(mno);
		mylog.debug("chRandList : "+chRandList.size());
		
		// 4. 카드 추천
		List<PropCardVO> cardRandList = rptService.cardRand(mno);
		mylog.debug("cardRandList : "+cardRandList.size());
		
		/////////////// 2. List<Map> -> JsonArray ///////////////
		String cateCntjson = rptService.listMapToJson(cateCntList);
		String cateSumjson = rptService.listMapToJson(cateSumList);
		
		/////////////// 3. model로 전달 ///////////////
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cateCntjson", cateCntjson);
		map.put("cateSumjson", cateSumjson);
		map.put("chRandList", chRandList);
		map.put("cardRandList", cardRandList);
		model.addAttribute("map", map);
		model.addAttribute("userVO", userVO);
		return "/asset/cateReport";
	}
	
	
//	http://localhost:8080/asset/budget
//	http://localhost:8080/asset/budget?mm=1
	@GetMapping(value = "/budget")
	public String budget(@RequestParam("mm") int mm, HttpSession session, Model model) throws Exception {	
		int mno = (int)session.getAttribute("mno");
		
		// getctTop
		List<String> ctTopList = abService.getctTop();
		mylog.debug("ctTopList : "+ctTopList.size());
		model.addAttribute("ctTopList", ctTopList);
		
		// chkBud
		String pMonth = abService.getPMonth(mm);
		mylog.debug("조회시점(pMonth) : "+pMonth);
		model.addAttribute("pMonth", pMonth);
		
		int chkBud = abService.chkBud(mno, pMonth);
		mylog.debug("chkBud : "+chkBud);
		
		if(chkBud==0) {
			return "/asset/nbudget";
		} else {
			return "/asset/ybudget";
		}
	}
	
	// 예산 조회 (조회시점으로부터 한 달 전)
	@ResponseBody
	@PostMapping(value = "/budcopy")
	public List<Map<String, Object>> budcopy(@RequestParam("mm") int mm, HttpSession session) throws Exception {
		int mno = (int)session.getAttribute("mno");
		
		// pMonth
		int mm2 = mm+1;
		String pMonth = abService.getPMonth(mm2);
		mylog.debug("조회시점 한 달 전(pMonth) : "+pMonth);
		
		// 예산 조회
		List<Map<String, Object>> budList = abService.getBud(mno, pMonth);
		if(budList.isEmpty()) {
			mylog.debug("예산 없음");
		}
		return budList;
		
	}
	
	@PostMapping(value = "/budget")
	public String budget(PlanVO pvo, HttpSession session) {
		// vo : mno, pMonth, p_amount, ctno
		int mno = (int)session.getAttribute("mno");
		pvo.setMno(mno);
		
		mylog.debug("폼 등록");
		return "";
	}
	///////////////////MJ////////////////////
}