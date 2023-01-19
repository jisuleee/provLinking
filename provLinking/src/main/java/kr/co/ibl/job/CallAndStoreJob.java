package kr.co.ibl.job;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.ibl.HomeController;
import kr.co.ibl.dbfunc.service.DbFuncService;
import kr.co.ibl.info.service.InfoService;
import kr.co.ibl.util.EncryptUtil;

import com.ibleaders.utility.ib_json.JSONArray;
import com.ibleaders.utility.ib_json.JSONObject;
import com.ibleaders.utility.ib_json.parser.JSONParser;

@Service("callAndStoreJob")
public class CallAndStoreJob {

	@Autowired
	private InfoService infoService; 
	private static final Logger logger = LoggerFactory.getLogger(CallAndStoreJob.class);
	
	@Autowired
	private DbFuncService dbFuncService;
	
    EncryptUtil aes256 = new EncryptUtil();
	
	String apiString = "";
	public int count = 1; 
	String linkHstNo = "";
	String linkHstNo2 = "";
	String saveDate = "";
	String linkCycleHr = "";
	String linkDatYmd = "";
	String linkDatBginTm = "";
	String linkDatEndTm = "";
	String nowTmDate = "";
	
	HashMap testMap = new HashMap();
	
	//연계작업 실행시간 여부 확인
	public boolean checkLinkExecTimeYN(String linkCycleHr, Date lastExcnTm) throws ParseException, Exception{ //checkLinkCycleYN checkLinkTimeYN checkExecTimeYN

		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //HH:mm:ss
		Calendar lastExcnTmCal = Calendar.getInstance();
		Date lastExcnTmNoSs = sdformat.parse(lastExcnTm.toString());
		lastExcnTmCal.setTime(lastExcnTmNoSs);
		
		lastExcnTmCal.add(Calendar.MINUTE, Integer.parseInt(linkCycleHr.trim())); 
		String lastExcnTmDate = sdformat.format(lastExcnTmCal.getTime());

		Date nowDate = new Date();
		Calendar nowTmCal = Calendar.getInstance();
		nowTmCal.setTime(nowDate);
		
		String nowTmDate = sdformat.format(nowTmCal.getTime());
		this.nowTmDate = nowTmDate;
		//System.out.println("날짜최신실행시간나오는거 + "+lastExcnTmDate +"지금은" +nowTmDate);
		
		if(lastExcnTmCal.before(nowTmCal)&& !lastExcnTmDate.equals(nowTmDate)){
			lastExcnTmCal.setTime(lastExcnTmNoSs);
			this.linkCycleHr = getLastLinkCycleHr().trim();//연계이력 저장 위함
			lastExcnTmCal.add(Calendar.MINUTE, Integer.parseInt(getLastLinkCycleHr().trim()));
			lastExcnTmDate = sdformat.format(lastExcnTmCal.getTime());
			
			if(lastExcnTmDate.equals(nowTmDate)){ 
				this.nowTmDate = lastExcnTmDate; 
				return true;
			}else{
				return false;
			}
		}else{
			if(lastExcnTmDate.equals(nowTmDate)){
				this.nowTmDate = lastExcnTmDate;
				return true;
			}else{
				return false;
			}
		}
	}

	public String getLinkCycleHr() throws SQLException, Exception{
		HashMap map1 = new HashMap();
		map1.put("link_knd_cd", "1");
		map1.put("link_yr", this.linkDatYmd.substring(0,4));
		return (String) infoService.selectLinkCycleHr(map1).get("LINK_CYCLE_HR");
	}
	
	public String getLastLinkCycleHr() throws Exception{
		HashMap map1 = new HashMap();
		map1.put("link_knd_cd", "1");	
		return (String) infoService.selectLastLinkCycleHr((String)map1.get("link_knd_cd")).get("LINK_CYCLE_HR");
	}
	
	public Date getLastExcnTime() throws Exception{
		String link_knd_cd = "1";
		//자정분기점인 경우 시간 계산 제외  
		return (Date) infoService.selectLastExcnTime(link_knd_cd).get("EXCN_DT");
	}
	
	public String getLastDataEndTime() throws Exception{		
		String link_knd_cd = "1";
		return (String) infoService.selectLastDataEndTime(link_knd_cd).get("LINK_DAT_END_TM"); //받아옴 
	}
	
	public String getLastDataYmd() throws Exception{
		String link_knd_cd = "1";
		return (String) infoService.selectLastDataYmd(link_knd_cd).get("LINK_DAT_YMD"); //받아옴 
	}
	
	public String getLinkDatEndTm(String linkDatBginTm, String linkCycleHr) throws ParseException,Exception{
		SimpleDateFormat sdformat = new SimpleDateFormat("HH:mm:ss"); 
		Date linkDatBginDt = sdformat.parse(linkDatBginTm.trim());
		
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(linkDatBginDt);
		cal.add(Calendar.MINUTE,Integer.parseInt(linkCycleHr.trim()));
		
		String linkDatEndTm = sdformat.format(cal.getTime());
		return linkDatEndTm;
	}
	
	public String getTomorrowYmd(String linkDatYmd) throws ParseException,Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date linkDat = sdf.parse(linkDatYmd);
        Calendar cal = Calendar.getInstance();
        cal.setTime(linkDat);
        cal.add(Calendar.DATE, 1);
        
        String tomorrow = sdf.format(cal.getTime());
        return tomorrow;
	}
	
	public void setThisLinkHstNo(String link_knd_cd) throws Exception{
		if(link_knd_cd.equals("1")){
			this.linkHstNo = (String) infoService.selectLinkHstNo(link_knd_cd);
		}
		if(link_knd_cd.equals("2")){
			this.linkHstNo2 = (String) infoService.selectLinkHstNo(link_knd_cd);
		}
	}
	
    public void insertProvLinkHst(String linkKndCd, String successYn){
    	try{
//	    	this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
    		setThisLinkHstNo(linkKndCd);
    		HashMap map = new HashMap();    
	       
	        String linkCycleHr = "";
	        map.put("link_cycle_hr", this.linkCycleHr); 
	        map.put("link_knd_cd", linkKndCd);
	        map.put("link_dat_ymd", this.linkDatYmd);
	        map.put("link_dat_bgin_tm", this.linkDatBginTm);
	        map.put("link_dat_end_tm", this.linkDatEndTm);
	        map.put("excn_cnt", "1");
	        map.put("excn_dt", this.nowTmDate);
	        map.put("scs_yn", successYn);
	        infoService.insertProvLinkHst(map);      
    	}
        catch(SQLException e){
     	   logger.error("[CallAndStoreJob insertProvLinkHst()] SQL Exception 발생"+e.getMessage());    	   
        }
        catch(Exception e){
     	   logger.error("[CallAndStoreJob insertProvLinkHst()] Exception 발생"+e.getMessage());
 		}
    }
	   
    public void insertLinkErrLog(String linkHstNo,String errKndCd, String message){
	   
	   if(message.length()>300){
		   message = message.substring(0,300);
	   }
	   
       HashMap map = new HashMap();
       map.put("linkHstNo", linkHstNo);
       map.put("err_knd_cd", errKndCd);
       map.put("err_cn", message);
       try{
    	   infoService.insertProvLinkErrLog(map);
       }
       catch(SQLException e){
    	   logger.error("[CallAndStoreJob insertLinkErrLog()] SQLException 발생"+e.getMessage());    	    	   
       }
       catch(Exception e){
    	   logger.error("[CallAndStoreJob insertLinkErrLog()] Exception 발생"+e.getMessage());    	
	   }
    }
	   
   
	public String createProvApiUrl(String linkDatBginTm, String linkDatEndTm, String linkDatYmd) throws IOException{				
	    org.springframework.core.io.Resource resource = new ClassPathResource("/properties/globals.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);		
        
//      http://localhost:9094/middleProvApi/req.do?linkDatYmd=2022-02-02&linkDatBginTm=12:00:00&linkDatEndTm=13:00:00	
		StringBuilder urlBuilder = new StringBuilder(props.getProperty("Globals.MiddleApiUrl")); 
        urlBuilder.append(URLEncoder.encode("linkDatYmd","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("linkDatBginTm","UTF-8") + "=" + URLEncoder.encode(this.linkDatBginTm, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("linkDatEndTm","UTF-8") + "=" + URLEncoder.encode(this.linkDatEndTm, "UTF-8"));
	
        
        return urlBuilder.toString().replace("%3A", ":"); //시간데이터에서 : 문자 치환
	}
	
	public String excnCallProvRowData(int count) throws SQLException, Exception{
		String linkType = "";
      	linkType = "1";
        String responseMsg = ""; 
        
        BufferedReader rd = null;
        HttpURLConnection conn = null;
        
      	try{
	        URL url = new URL(createProvApiUrl(this.linkDatBginTm, this.linkDatEndTm, this.linkDatYmd));        
	        
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-type", "application/json");
	        conn.setConnectTimeout(5000); // 연결 타임아웃 설정(5초) 
	        int responseCode = conn.getResponseCode();
//	        System.out.println("Response code: " + responseCode);
	        
	        
	        if(responseCode >= 200 && responseCode <= 300) 
	        {		        
	        	rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		        StringBuilder sb = new StringBuilder();
		        String line;
		        while ((line = rd.readLine()) != null) {
		            sb.append(line);
		        }
		        
		        apiString = sb.toString();       	
		        /*
		         * 실증화 시스템에서 보내는 api 형태 
		         * {"msg":"정상호출","code":"success","data":데이터배열,"size":데이터사이즈}
		         * {"msg":"필수항목 누락","code":"error","data":null,"size":0}
		         */
		        //정상호출 확인
		     	responseMsg = apiString.substring(8,12);	 
		     	
		        //받아온 JSON이 정상 데이터 일때 
		        if(responseMsg.equals("정상호출")){
		            //통신연결에 성공한경우 
		            if(count != 1) //재귀함수를 탄 경우
		            {//호출횟수 업데이트
		            	infoService.increExcnCount(this.linkHstNo); //or linkHstNo
		            }
		            
			        if(count == 1){
			        	//최근 연계이력정보로 호출성공 이력을 저장한다. 
		                linkType = "1"; //1-호출 2-적재 3-변환
			        	insertProvLinkHst(linkType,"Y");
			        }
		        }
		        else{		        	
		        	//200코드이나 정상메시지가 아닐 때 파싱 시도 
			        JSONParser jsonParser = new JSONParser();   
			        JSONObject obj = (JSONObject) jsonParser.parse(apiString);   
			        responseMsg = (String) obj.get("msg");
			        
		        	//통신연결에 실패한 경우 
		        	//에러이력 추가
		        	if(count == 1){
		        		insertProvLinkHst(linkType,"N");
		        	}
		        	//에러 데이터 insert
		            insertLinkErrLog(linkHstNo,"1","response 메시지 에러- "+responseMsg);
		        	
		            if(count != 1) //재귀함수를 탄 경우
		            {
		            	infoService.increExcnCount(linkHstNo); //or linkHstNo
		            }

		           //다섯번동안 재시도 (재귀함수)
		           if(count != 5){
		        	   count++;
		        	   excnCallProvRowData(count);
		        	   return "5"; //종료신호 (재귀함수 탈출)
		           }	           
	        	    return "0";
		        }
		        
			  return apiString; 

	        }else {
	           //통신연결에 실패한 경우 
	        	//에러이력 추가
	        	if(count == 1){
	                linkType = "1"; //1-호출 2-적재 3-변환
	        		insertProvLinkHst(linkType,"N");
	        	}
	        	//에러 데이터 insert
	            insertLinkErrLog(linkHstNo,"1","responseCode 에러- "+ responseCode);
	            
	            if(count != 1) //재귀함수를 탄 경우
	            {	
	            	infoService.increExcnCount(linkHstNo); //or linkHstNo
	            }

	           //다섯번동안 재시도 (재귀함수)
	            if(count != 5){
	        	   count++;
	        	   excnCallProvRowData(count);
	        	   return "5"; //종료신호 (재귀함수 탈출)
	            }	           
        	    return "0";
	           
	        }	
		}catch(ConnectException e){

			//통신연결에 실패한 경우 
        	//에러이력 추가
        	if(count == 1){
                linkType = "1"; //1-호출 2-적재 3-변환
        		insertProvLinkHst(linkType,"N");
        	}
            insertLinkErrLog(linkHstNo,"1","[CallAndStoreJob excnCallProvRowData()] ConnectException 발생"+e.getMessage());
            
            if(count != 1) //재귀함수를 탄 경우
            {
            	infoService.increExcnCount(linkHstNo); //or linkHstNo
            }

           //다섯번동안 재시도 (재귀함수)
           if(count != 5){
        	   count++;
        	   excnCallProvRowData(count);
        	   return "5"; //종료 신호(재귀함수 탈출)
           }
    	   return "0"; 
		}catch(Exception e){

			//통신연결에 실패한 경우 
        	//에러이력 추가
        	if(count == 1){
                linkType = "1"; //1-호출 2-적재 3-변환
        		insertProvLinkHst(linkType,"N");
        	}
            insertLinkErrLog(linkHstNo,"1","[CallAndStoreJob excnCallProvRowData()] Exception 발생"+e.getMessage());
            
            if(count != 1) //재귀함수를 탄 경우
            {
            	infoService.increExcnCount(linkHstNo); //or linkHstNo
            }

           //다섯번동안 재시도 (재귀함수)
           if(count != 5){
        	   count++;
        	   excnCallProvRowData(count);
        	   return "5"; //종료 신호(재귀함수 탈출)
           }
    	   return "0"; 
		}
      	finally{
    		if (rd != null) try {rd.close();}catch(IOException ex){	logger.error("CallAndStoreJob rd close 이슈발생!!"); }
            if (conn != null) try {conn.disconnect();} catch(NullPointerException ex){	logger.error("CallAndStoreJob conn disconnect 이슈발생!!"); }
      	}

	}
	
	
	public JSONObject parseStringToJson(String provStringData) throws ParseException,Exception{
		
		JSONParser jsonParser = new JSONParser();
		JSONObject obj = new JSONObject();
        	
        obj = (JSONObject) jsonParser.parse(provStringData);
		return obj;
	}
	
	
	//실증화 로우 데이터 리스트를 뽑는 메소드
	public List<HashMap<String,Object>> getProvDataList(String provStringData) throws ParseException,Exception {
		
		JSONObject provDataJson = parseStringToJson(provStringData);
        JSONArray parse_items = (JSONArray) provDataJson.get("data");
        List<HashMap<String,Object>> provDataList = new ArrayList<HashMap<String,Object>>();
		
        
		//단독으로 데이터 생성
		for(int i=0; i<parse_items.size(); i++) { //parse_items
			
			HashMap<String,Object> provData = new HashMap<String,Object>();
			JSONObject firstItem = (JSONObject)parse_items.get(i);//parse_items
			
			String dviceId = (String) firstItem.get("TAG_NAME");
			String msurVal = Double.toString((Double)firstItem.get("XVALUE"));
			String crtDt = (String) firstItem.get("SAVE_TIME");
			String crtYmd = (String) firstItem.get("SAVE_DATE");
			
			provData.put("dvice_id", dviceId);
			provData.put("msur_val", msurVal);
			provData.put("crt_dt", crtDt);
			provData.put("crt_ymd", crtYmd);
			
			provDataList.add(provData);
		}	
				
		return provDataList;
	}	

    public List<HashMap<String,Object>> getProvDataListByJackson(String provStringData) throws JsonParseException, JsonMappingException, IOException{
	   ObjectMapper objm = new ObjectMapper();
	   Map<String, Object> provData = objm.readValue(provStringData, new TypeReference<Map<String, Object>>() {});
	   List<HashMap<String, Object>> data = (List<HashMap<String, Object>>) provData.get("data");

	   return data;
    }
	   
    //적재 메소드
	public boolean storeProvRowDataToDB(List<HashMap<String,Object>> provDataList) throws SQLException, Exception{   
	  
	//임시 제거(실증화 운영서버 반영 전)		
    //if(provDataList.size() == 0){
    //	 throw new StringIndexOutOfBoundsException(); 
    //}

	  org.springframework.core.io.Resource resource = new ClassPathResource("/properties/globals.properties");
      Properties props = PropertiesLoaderUtils.loadProperties(resource);
      String dbUrl = props.getProperty("Globals.Url");
      String dbUser = props.getProperty("Globals.UserName");     
      String dbPass = aes256.decrypt(props.getProperty("Globals.PasswordRSAEncrypt"));
      
//    String dbPass = props.getProperty("Globals.Password");
//    TB_PROV_DAT_2022
      String sql = "INSERT INTO TB_PROV_RAW_DAT (DVICE_ID, MSUR_VAL, CRT_DT, CRT_YMD ) VALUES (?, ?, ?, ?)";
      
      Class.forName("oracle.jdbc.OracleDriver"); 
      
      Connection con = null;
      PreparedStatement pstmt = null ;
  
      con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
      con.setAutoCommit(false);  // 자동 커밋 해제
     
         pstmt = con.prepareStatement(sql) ;
         
         for(int i=0; i<provDataList.size(); i++){           

        	 pstmt.setString(1,(String) provDataList.get(i).get("TAG_NAME"));
        	 pstmt.setString(2, Double.toString((Double)provDataList.get(i).get("XVALUE")));
        	 pstmt.setString(3, (String) provDataList.get(i).get("SAVE_TIME"));
        	 pstmt.setString(4, (String) provDataList.get(i).get("SAVE_DATE"));
             
            // addBatch에 담기
            pstmt.addBatch();
            
            // 파라미터 Clear
            pstmt.clearParameters() ;

                     
            // OutOfMemory를 고려하여 만건 단위로 커밋
            if( (i % 10000) == 0){
               
               // Batch 실행
               pstmt.executeBatch() ;
               
               // Batch 초기화
               pstmt.clearBatch();
               
               // 커밋
               con.commit() ;
            }            
         }       
         
         // 커밋되지 못한 나머지 구문에 대하여 커밋
         pstmt.executeBatch() ;
         con.commit() ;
   	  
         //insertProvLinkHst("2", "Y");
         
         try {
            con.rollback() ;
         } catch (SQLException e1) {
        	 logger.error("CallAndStoreJob storeProvRowDataToDB - conn rollback SQLException 발생");
        	 throw new SQLException(); 
         }           
         finally{
            if (pstmt != null) try {pstmt.close();pstmt = null;} catch(SQLException ex){ System.out.println("CallAndStoreJob storeProvRowDataToDB() - pstmt close SQLException 발생"); throw new SQLException(); }
            if (con != null) try {con.close();con = null;} catch(SQLException ex){ System.out.println("CallAndStoreJob storeProvRowDataToDB() - con close SQLException 발생"); throw new SQLException(); }
         }
      return true;
	}	

	//전체 호출+적재 실행 메인 메소드
	public void excnCallAndStoreJob(){
		
		String linkDatYmd = "";
		String linkDatBginTm ="";
		String linkDatEndTm = "";		
		String linkCycleHr = "";
		Date lastExcnTm;

		try{			
			//직전 데이터 연월일이 기준
			linkDatYmd = getLastDataYmd();
			this.linkDatYmd = linkDatYmd;
			
	        LocalDate now = LocalDate.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        String formatedNow = now.format(formatter);
	        String saveDate = formatedNow;
	        this.saveDate = saveDate;
	        
			//현재설정된 연계작업 주기 확인
			linkCycleHr = getLinkCycleHr();
			lastExcnTm = getLastExcnTime();
			linkDatBginTm = getLastDataEndTime();
			linkDatEndTm = getLinkDatEndTm(linkDatBginTm,linkCycleHr);
			
			this.linkCycleHr = linkCycleHr;
	        this.linkDatBginTm = linkDatBginTm;
	        this.linkDatEndTm = linkDatEndTm;

	        
			//설정한 연계주기에 따라 현재 실행하는 시간인지 체크
			if(checkLinkExecTimeYN(linkCycleHr, lastExcnTm)){

				//만약 연계주기를 더했을 때 자정을 넘겼다면 ★★
				if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){ //3 대신 endTm 문자열화 ★★★
		            String tempLinkDatEndTm = this.linkDatEndTm;
		            
					for(int i=0; i<2; i++){
						if(i==0)
						{
							this.linkDatEndTm = "23:59:59";
							
							String provStringData = excnCallProvRowData(1);
							if(provStringData != "5"){ //계속 진행하기 위함 return삭제
								try{ //전체예외로 안빠지고 계속 진행하기위함
									storeProvRowDataToDB(getProvDataListByJackson(provStringData));	//파싱해서 적재 									
							        //http://localhost:9090/addLink/excn.do?linkDatYmd=2022-06-01&linkDatBginTm=00:00:00&linkDatEndTm=04:00:00nkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용 
							        insertProvLinkHst("2", "Y");
								}
								catch(StringIndexOutOfBoundsException e){
							    	 //this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
							         insertProvLinkHst("2", "N"); 
							         insertLinkErrLog(this.linkHstNo2,"3",String.valueOf("CallAndStoreJob excnCallAndStoreJob() 분기점, 받아온 데이터에 값이 없습니다"));													
								}catch(Exception e){
							    	 //this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
							         insertProvLinkHst("2", "N"); 
							         insertLinkErrLog(this.linkHstNo2,"4","CallAndStoreJob excnCallAndStoreJob() 분기점, Exception 발생"+e.getMessage());
								}
							}
						}
						if(i==1){
							
			                this.linkDatBginTm = "00:00:59";
			                this.linkDatEndTm = tempLinkDatEndTm;
			                this.linkDatYmd = getTomorrowYmd(this.linkDatYmd);						
							
							String provStringData = excnCallProvRowData(1);
							if(provStringData != "5"){
								//파싱해서 적재 
								storeProvRowDataToDB(getProvDataListByJackson(provStringData));		
							}else{
								return;
							}			
							
					        //this.linkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용 
					        insertProvLinkHst("2", "Y");	
						}					
					}	
				}else{
					String provStringData = excnCallProvRowData(1);
					if(provStringData == "5"){return;}
					storeProvRowDataToDB(getProvDataListByJackson(provStringData));
			        //this.linkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용 
			        insertProvLinkHst("2", "Y");
				}
		         
			}else{
				//연계주기에 해당하지 않아 스케줄러 작업 중단
				return;
			}
		}catch(SQLException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"2","CallAndStoreJob excnCallAndStoreJob() SQLException 발생 " +e.getMessage()); //+e.getMessage()
		
		}catch(ParseException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"3","CallAndStoreJob excnCallAndStoreJob() ParseException 발생" +e.getMessage());			
		}
		catch(StringIndexOutOfBoundsException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"3","CallAndStoreJob excnCallAndStoreJob(), 받아온 데이터에 값이 없습니다");					
		}
		catch(Exception e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"4","CallAndStoreJob excnCallAndStoreJob() Exception 발생" +e.getMessage()); //+e.getMessage()
		}
	}	
	
}
