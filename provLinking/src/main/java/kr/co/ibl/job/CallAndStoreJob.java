package kr.co.ibl.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ibl.dbfunc.service.DbFuncService;
import kr.co.ibl.info.service.InfoService;
import com.ibleaders.utility.ib_json.JSONArray;
import com.ibleaders.utility.ib_json.JSONObject;
import com.ibleaders.utility.ib_json.parser.JSONParser;

@Service("storeProvDataService")
public class CallAndStoreJob {

	@Autowired
	private InfoService infoService;
	
	@Autowired
	private DbFuncService dbFuncService;
	
	String apiString = "";
	public int count = 1; 
	String linkHstNo = "";
	String linkHstNo2 = "";
	String saveDate = "";
	String linkCycleHr = "";
	String linkDatYmd = "";
	String linkDatBginTm = "";
	String linkDatEndTm = "";
	
	HashMap testMap = new HashMap();
	
	
	//연계작업 실행시간 여부 확인
	public boolean checkLinkExecTimeYN(String linkCycleHr, Date lastExcnTm){ //checkLinkCycleYN checkLinkTimeYN checkExecTimeYN
				
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //HH:mm:ss
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastExcnTm);
		
		cal.add(Calendar.MINUTE, Integer.parseInt(linkCycleHr.trim())); 
		String lastExcnTmDate = sdformat.format(cal.getTime());

		Date nowDate = new Date();
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(nowDate);
		
		String nowTmDate = sdformat.format(cal2.getTime());
		
		if(lastExcnTmDate.equals(nowTmDate)){ //이거 ss단위까지 맞아야함; 수정필요 
			return true;
		}else{
			return false;
		}
	}

	public String getLinkCycleHr() throws SQLException, Exception{
		HashMap map1 = new HashMap();
		map1.put("link_knd_cd", "1");
		map1.put("link_yr", this.linkDatYmd.substring(0,4));
		return (String) infoService.selectLinkCycleHr(map1).get("LINK_CYCLE_HR");
	}
	
	public Date getLastExcnTime() throws Exception{
		String link_knd_cd = "1";
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
	
    public void insertProvLinkHst(String linkKndCd, String successYn){
    	try{
    	this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
    	HashMap map = new HashMap();    
       
        String linkCycleHr = "";
        map.put("link_cycle_hr", this.linkCycleHr); 
        map.put("link_knd_cd", linkKndCd);
        map.put("link_dat_ymd", this.linkDatYmd);
        map.put("link_dat_bgin_tm", this.linkDatBginTm);
        map.put("link_dat_end_tm", this.linkDatEndTm);
        map.put("excn_cnt", "1");
        map.put("scs_yn", successYn);
        infoService.insertProvLinkHst(map);      
    	}
    	catch(Exception e){
    		String message = e.getMessage();
		    if(message.length()>300){
			   message = message.substring(0,300);
		    }
    		System.out.println("이력정보등록에러: " + message);
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
       }catch(Exception e){
   			System.out.println("에러정보등록에러 : "+ message);
       }
    }
	   
   
	public String createProvApiUrl(String linkDatBginTm, String linkDatEndTm, String linkDatYmd) throws IOException{
					
		StringBuilder urlBuilder = new StringBuilder("실증화url"); /*URL*/ 
        urlBuilder.append(URLEncoder.encode("saveDate","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd, "UTF-8")); /*검색하고자하는 등록일시 또는 변경일시 조회시작일시 */
        urlBuilder.append("&" + URLEncoder.encode("startTime","UTF-8") + "=" + URLEncoder.encode(this.linkDatBginTm, "UTF-8")); /*검색하고자하는 등록일시 또는 변경일시 조회시작일시 */
        urlBuilder.append("&" + URLEncoder.encode("endTime","UTF-8") + "=" + URLEncoder.encode(this.linkDatEndTm, "UTF-8")); /*검색하고자하는 등록일시 또는 변경일시 조회종료일시 */
        
        return urlBuilder.toString().replace("%3A", ":"); //시간데이터에서 : 문자 치환
	}
	
	public String excnCallProvRowData(int count) throws SQLException, Exception{
		String linkType = "";
      	linkType = "1";
      	
      	try {	           		            
	        URL url = new URL(createProvApiUrl(linkDatBginTm, linkDatEndTm, linkDatYmd));        
	        
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-type", "application/json");
	        conn.setConnectTimeout(5000); // 연결 타임아웃 설정(5초) 
	        System.out.println("Response code: " + conn.getResponseCode());	
	        
	        
	        BufferedReader rd;     
	        
	        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) 
	        { //conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300  1>2
		        
	        	rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            
	            //통신연결에 성공한경우 
	            //장비명,데이터시간,실행시간으로 데이터 insert
	            if(count != 1) //재귀함수를 탄 경우
	            {
	            	//호출횟수 업데이트
	            	infoService.increExcnCount(linkHstNo); //or linkHstNo
	            }
	            
		        if(count == 1){
		        	//최근 연계이력정보로 호출성공 이력을 저장한다. 
	                linkType = "1"; //1-호출 2-적재 3-변환
	                this.linkHstNo = infoService.selectLinkHstNo(linkType);
		        	insertProvLinkHst(linkType,"Y");
		        }
		        
		        StringBuilder sb = new StringBuilder();
		        String line;
		        while ((line = rd.readLine()) != null) {
		            sb.append(line);
		        }
		
		        apiString = sb.toString();      //문자열화 
		        
		        rd.close();
		        conn.disconnect();
		        	        
			  return apiString; 
	            
	            
	        } else {
	            //rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	            
	           //통신연결에 실패한 경우 
	        	//에러이력 추가
	        	if(count == 1){
	                linkType = "1"; //1-호출 2-적재 3-변환
	                this.linkHstNo = infoService.selectLinkHstNo(linkType);
	        		insertProvLinkHst(linkType,"N");
	        	}
	        	//에러 데이터 insert
	            insertLinkErrLog(linkHstNo,"1","responseCode 에러");
	            
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
		/*} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "0";
		}catch(IOException e){
			e.printStackTrace();		
			return "0";
			*/
		}catch(Exception e){
			//통신연결에 실패한 경우 
        	//에러이력 추가
        	if(count == 1){
                linkType = "1"; //1-호출 2-적재 3-변환
                this.linkHstNo = infoService.selectLinkHstNo(linkType);
        		insertProvLinkHst(linkType,"N");
        	}
            insertLinkErrLog(linkHstNo,"1",e.getMessage());
            
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
			JSONObject firstItem = (JSONObject)parse_items.get(i);	 //parse_items
			
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
	       
       if(provDataList.size() == 0){
    	  throw new RuntimeException(); 
       }
       
	  //연계 이력 저장
      //this.linkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용   
      
      Connection con = null;
      PreparedStatement pstmt = null ;
      
      String sql = "INSERT INTO TB_PROV_ROW_DAT (DVICE_ID, MSUR_VAL, CRT_DT, CRT_YMD ) VALUES (?, ?, ?, ?)";
      
	  org.springframework.core.io.Resource resource = new ClassPathResource("/properties/globals.properties");
      Properties props = PropertiesLoaderUtils.loadProperties(resource);
      
      Class.forName("oracle.jdbc.OracleDriver"); 
      con = DriverManager.getConnection(props.getProperty("Url"), props.getProperty("UserName"), props.getProperty("Password"));
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
               e1.printStackTrace();
         }           
         finally{
            if (pstmt != null) try {pstmt.close();pstmt = null;} catch(SQLException ex){}
            if (con != null) try {con.close();con = null;} catch(SQLException ex){}
         }
      return true;
	}	

	//전체 호출+적재 실행 메인 메소드
	public void excnStoreProvRowData(){

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
			if(!checkLinkExecTimeYN(linkCycleHr, lastExcnTm)){
	
				//만약 연계주기를 더했을 때 자정을 넘겼다면 ★★
				if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){ //3 대신 endTm 문자열화 ★★★
		            String tempLinkDatEndTm = this.linkDatEndTm;
		            
					for(int i=0; i<2; i++){
						if(i==0)
						{
							this.linkDatEndTm = "23:59:00";
							
							String provStringData = excnCallProvRowData(1);
							if(provStringData != "5"){ //계속 진행하기 위함 return삭제
								try{ //전체예외로 안빠지고 계속 진행하기위함
									storeProvRowDataToDB(getProvDataListByJackson(provStringData));	//파싱해서 적재 									
							        this.linkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용 
							        insertProvLinkHst("2", "Y");
								}
								catch(RuntimeException e){
							    	 this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
							         insertProvLinkHst("2", "N"); 
							         insertLinkErrLog(this.linkHstNo2,"3",String.valueOf("받아온 데이터에 값이 없습니다"));													
								}catch(Exception e){
							    	 this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
							         insertProvLinkHst("2", "N"); 
							         insertLinkErrLog(this.linkHstNo2,"4",String.valueOf(e.getMessage()));
								}
							}
						}
						if(i==1){
							
			                this.linkDatBginTm = "00:00:00";
			                this.linkDatEndTm = tempLinkDatEndTm;
			                this.linkDatYmd = getTomorrowYmd(this.linkDatYmd);						
							
							String provStringData = excnCallProvRowData(1);
							if(provStringData != "5"){
								//파싱해서 적재 
								storeProvRowDataToDB(getProvDataListByJackson(provStringData));		
							}else{
								return;
							}			
							
					        this.linkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용 
					        insertProvLinkHst("2", "Y");	
						}					
					}	
				}else{
					
					String provStringData = excnCallProvRowData(1);
					if(provStringData == "5"){return;}
					storeProvRowDataToDB(getProvDataListByJackson(provStringData));
					
			        this.linkHstNo2 = infoService.selectLinkHstNo("2"); //전역변수용 
			        insertProvLinkHst("2", "Y");
				}
		         
			}else{
				//연계주기에 해당하지 않아 스케줄러 작업 중단
				return;
			}
		}catch(SQLException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"2",String.valueOf(e.getMessage()));
		
		}catch(ParseException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"3",String.valueOf(e.getMessage()));			
		}
		catch(RuntimeException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"3",String.valueOf("받아온 데이터에 값이 없습니다"));					
		}
		catch(Exception e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"4",String.valueOf(e.getMessage()));
		}
	}	
	
}
