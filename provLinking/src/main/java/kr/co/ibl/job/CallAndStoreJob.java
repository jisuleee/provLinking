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
import com.ibleaders.utility.ib_json.JSONArray;
import com.ibleaders.utility.ib_json.JSONObject;
import com.ibleaders.utility.ib_json.parser.JSONParser;

@Service("callAndStoreJob")
public class CallAndStoreJob {

	@Autowired
	private InfoService infoService; private static final Logger logger = LoggerFactory.getLogger(CallAndStoreJob.class);
	
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
	String nowTmDate = "";
	
	HashMap testMap = new HashMap();
	
	
	//???????????? ???????????? ?????? ??????
	public boolean checkLinkExecTimeYN(String linkCycleHr, Date lastExcnTm) throws ParseException{ //checkLinkCycleYN checkLinkTimeYN checkExecTimeYN
				
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //HH:mm:ss
		Calendar cal = Calendar.getInstance();
		Date lastExcnTmNoSs = sdformat.parse(lastExcnTm.toString());
		cal.setTime(lastExcnTmNoSs);
		
		cal.add(Calendar.MINUTE, Integer.parseInt(linkCycleHr.trim())); 
		String lastExcnTmDate = sdformat.format(cal.getTime());

		Date nowDate = new Date();
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(nowDate);
		
		String nowTmDate = sdformat.format(cal2.getTime());
		this.nowTmDate = nowTmDate;
		
		if(lastExcnTmDate.equals(nowTmDate)){  
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
		//?????????????????? ?????? ?????? ?????? ??????  
		return (Date) infoService.selectLastExcnTime(link_knd_cd).get("EXCN_DT");
	}
	
	public String getLastDataEndTime() throws Exception{		
		String link_knd_cd = "1";
		return (String) infoService.selectLastDataEndTime(link_knd_cd).get("LINK_DAT_END_TM"); //????????? 
	}
	
	public String getLastDataYmd() throws Exception{
		String link_knd_cd = "1";
		return (String) infoService.selectLastDataYmd(link_knd_cd).get("LINK_DAT_YMD"); //????????? 
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
     	   logger.debug("[CallAndStoreJob insertProvLinkHst()] SQL Exception ??????");    	   
        }
        catch(Exception e){
     	   logger.debug("[CallAndStoreJob insertProvLinkHst()] Exception ??????");
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
    	   logger.debug("[CallAndStoreJob insertLinkErrLog()] SQLException ??????");    	    	   
       }
       catch(Exception e){
    	   logger.debug("[CallAndStoreJob insertLinkErrLog()] Exception ??????");    	
	   }
    }
	   
   
	public String createProvApiUrl(String linkDatBginTm, String linkDatEndTm, String linkDatYmd) throws IOException{				
	    org.springframework.core.io.Resource resource = new ClassPathResource("/properties/globals.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);		
		
        //http://localhost:9094/middleProvApi/req.do?linkDatYmd=2022-02-02&linkDatBginTm=12:00:00&linkDatEndTm=13:00:00	
		StringBuilder urlBuilder = new StringBuilder(props.getProperty("Globals.MiddleApiUrl")); 
        urlBuilder.append(URLEncoder.encode("linkDatYmd","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("linkDatBginTm","UTF-8") + "=" + URLEncoder.encode(this.linkDatBginTm, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("linkDatEndTm","UTF-8") + "=" + URLEncoder.encode(this.linkDatEndTm, "UTF-8"));
        
        return urlBuilder.toString().replace("%3A", ":"); //????????????????????? : ?????? ??????
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
	        conn.setConnectTimeout(5000); // ?????? ???????????? ??????(5???) 
	        int responseCode = conn.getResponseCode();
	        System.out.println("Response code: " + responseCode);
	        
	        
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
		         * ????????? ??????????????? ????????? api ?????? 
		         * {"msg":"????????????","code":"success","data":???????????????,"size":??????????????????}
		         * {"msg":"???????????? ??????","code":"error","data":null,"size":0}
		         */
		        //???????????? ??????
		     	responseMsg = apiString.substring(8,12);	 
		     	
		        //????????? JSON??? ?????? ????????? ?????? 
		        if(responseMsg.equals("????????????")){
		            //??????????????? ??????????????? 
		            if(count != 1) //??????????????? ??? ??????
		            {//???????????? ????????????
		            	infoService.increExcnCount(this.linkHstNo); //or linkHstNo
		            }
		            
			        if(count == 1){
			        	//?????? ????????????????????? ???????????? ????????? ????????????. 
		                linkType = "1"; //1-?????? 2-?????? 3-??????
			        	insertProvLinkHst(linkType,"Y");
			        }
		        }
		        else{		        	
		        	//200???????????? ?????????????????? ?????? ??? ?????? ?????? 
			        JSONParser jsonParser = new JSONParser();   
			        JSONObject obj = (JSONObject) jsonParser.parse(apiString);   
			        responseMsg = (String) obj.get("msg");
			        
		        	//??????????????? ????????? ?????? 
		        	//???????????? ??????
		        	if(count == 1){
		        		insertProvLinkHst(linkType,"N");
		        	}
		        	//?????? ????????? insert
		            insertLinkErrLog(linkHstNo,"1","response ????????? ??????- "+responseMsg);
		        	
		            if(count != 1) //??????????????? ??? ??????
		            {
		            	infoService.increExcnCount(linkHstNo); //or linkHstNo
		            }

		           //??????????????? ????????? (????????????)
		           if(count != 5){
		        	   count++;
		        	   excnCallProvRowData(count);
		        	   return "5"; //???????????? (???????????? ??????)
		           }	           
	        	    return "0";
		        }
		        
			  return apiString; 

	        }else {
	           //??????????????? ????????? ?????? 
	        	//???????????? ??????
	        	if(count == 1){
	                linkType = "1"; //1-?????? 2-?????? 3-??????
	        		insertProvLinkHst(linkType,"N");
	        	}
	        	//?????? ????????? insert
	            insertLinkErrLog(linkHstNo,"1","responseCode ??????- "+ responseCode);
	            
	            if(count != 1) //??????????????? ??? ??????
	            {	
	            	infoService.increExcnCount(linkHstNo); //or linkHstNo
	            }

	           //??????????????? ????????? (????????????)
	            if(count != 5){
	        	   count++;
	        	   excnCallProvRowData(count);
	        	   return "5"; //???????????? (???????????? ??????)
	            }	           
        	    return "0";
	           
	        }	
		}catch(ConnectException e){

			//??????????????? ????????? ?????? 
        	//???????????? ??????
        	if(count == 1){
                linkType = "1"; //1-?????? 2-?????? 3-??????
        		insertProvLinkHst(linkType,"N");
        	}
            insertLinkErrLog(linkHstNo,"1","[CallAndStoreJob excnCallProvRowData()] ConnectException ??????");
            
            if(count != 1) //??????????????? ??? ??????
            {
            	infoService.increExcnCount(linkHstNo); //or linkHstNo
            }

           //??????????????? ????????? (????????????)
           if(count != 5){
        	   count++;
        	   excnCallProvRowData(count);
        	   return "5"; //?????? ??????(???????????? ??????)
           }
    	   return "0"; 
		}catch(Exception e){

			//??????????????? ????????? ?????? 
        	//???????????? ??????
        	if(count == 1){
                linkType = "1"; //1-?????? 2-?????? 3-??????
        		insertProvLinkHst(linkType,"N");
        	}
            insertLinkErrLog(linkHstNo,"1","[CallAndStoreJob excnCallProvRowData()] Exception ??????");
            
            if(count != 1) //??????????????? ??? ??????
            {
            	infoService.increExcnCount(linkHstNo); //or linkHstNo
            }

           //??????????????? ????????? (????????????)
           if(count != 5){
        	   count++;
        	   excnCallProvRowData(count);
        	   return "5"; //?????? ??????(???????????? ??????)
           }
    	   return "0"; 
		}
      	finally{
    		if (rd != null) try {rd.close();}catch(IOException ex){	logger.error("CallAndStoreJob rd close ????????????!!"); }
            if (conn != null) try {conn.disconnect();} catch(NullPointerException ex){	logger.error("CallAndStoreJob conn disconnect ????????????!!"); }
      	}

	}
	
	
	public JSONObject parseStringToJson(String provStringData) throws ParseException,Exception{
		
		JSONParser jsonParser = new JSONParser();
		JSONObject obj = new JSONObject();
        	
        obj = (JSONObject) jsonParser.parse(provStringData);
		return obj;
	}
	
	
	//????????? ?????? ????????? ???????????? ?????? ?????????
	public List<HashMap<String,Object>> getProvDataList(String provStringData) throws ParseException,Exception {
		
		JSONObject provDataJson = parseStringToJson(provStringData);
        JSONArray parse_items = (JSONArray) provDataJson.get("data");
        List<HashMap<String,Object>> provDataList = new ArrayList<HashMap<String,Object>>();
		
        
		//???????????? ????????? ??????
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
	   
    //?????? ?????????
	public boolean storeProvRowDataToDB(List<HashMap<String,Object>> provDataList) throws SQLException, Exception{   
	  
	//?????? ??????(????????? ???????????? ?????? ???)		
    //if(provDataList.size() == 0){
    //	 throw new StringIndexOutOfBoundsException(); 
    //}

	  org.springframework.core.io.Resource resource = new ClassPathResource("/properties/globals.properties");
      Properties props = PropertiesLoaderUtils.loadProperties(resource);
      String dbUrl = props.getProperty("Globals.Url");
      String dbUser = props.getProperty("Globals.UserName");
      String dbPass = props.getProperty("Globals.Password");

      String sql = "INSERT INTO TB_PROV_DAT_2022 (DVICE_ID, MSUR_VAL, CRT_DT, CRT_YMD ) VALUES (?, ?, ?, ?)";
      
      Class.forName("oracle.jdbc.OracleDriver"); 
      
      Connection con = null;
      PreparedStatement pstmt = null ;
  
      con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
      con.setAutoCommit(false);  // ?????? ?????? ??????
     
         pstmt = con.prepareStatement(sql) ;
         
         for(int i=0; i<provDataList.size(); i++){           

        	 pstmt.setString(1,(String) provDataList.get(i).get("TAG_NAME"));
        	 pstmt.setString(2, Double.toString((Double)provDataList.get(i).get("XVALUE")));
        	 pstmt.setString(3, (String) provDataList.get(i).get("SAVE_TIME"));
        	 pstmt.setString(4, (String) provDataList.get(i).get("SAVE_DATE"));
             
            // addBatch??? ??????
            pstmt.addBatch();
            
            // ???????????? Clear
            pstmt.clearParameters() ;

                     
            // OutOfMemory??? ???????????? ?????? ????????? ??????
            if( (i % 10000) == 0){
               
               // Batch ??????
               pstmt.executeBatch() ;
               
               // Batch ?????????
               pstmt.clearBatch();
               
               // ??????
               con.commit() ;
            }            
         }       
         
         // ???????????? ?????? ????????? ????????? ????????? ??????
         pstmt.executeBatch() ;
         con.commit() ;
   	  
         //insertProvLinkHst("2", "Y");
         
         try {
            con.rollback() ;
         } catch (SQLException e1) {
        	 logger.debug("CallAndStoreJob storeProvRowDataToDB - conn rollback SQLException ??????");
        	 throw new SQLException(); 
         }           
         finally{
            if (pstmt != null) try {pstmt.close();pstmt = null;} catch(SQLException ex){ logger.debug("CallAndStoreJob storeProvRowDataToDB() - pstmt close SQLException ??????"); throw new SQLException(); }
            if (con != null) try {con.close();con = null;} catch(SQLException ex){ logger.debug("CallAndStoreJob storeProvRowDataToDB() - con close SQLException ??????"); throw new SQLException(); }
         }
      return true;
	}	

	//?????? ??????+?????? ?????? ?????? ?????????
	public void excnCallAndStoreJob(){
		
		String linkDatYmd = "";
		String linkDatBginTm ="";
		String linkDatEndTm = "";		
		String linkCycleHr = "";
		Date lastExcnTm;

		try{			
			//?????? ????????? ???????????? ??????
			linkDatYmd = getLastDataYmd();
			this.linkDatYmd = linkDatYmd;
			
	        LocalDate now = LocalDate.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        String formatedNow = now.format(formatter);
	        String saveDate = formatedNow;
	        this.saveDate = saveDate;
	        
			//??????????????? ???????????? ?????? ??????
			linkCycleHr = getLinkCycleHr();
			lastExcnTm = getLastExcnTime();
			linkDatBginTm = getLastDataEndTime();
			linkDatEndTm = getLinkDatEndTm(linkDatBginTm,linkCycleHr);
			
			this.linkCycleHr = linkCycleHr;
	        this.linkDatBginTm = linkDatBginTm;
	        this.linkDatEndTm = linkDatEndTm;

			
			//????????? ??????????????? ?????? ?????? ???????????? ???????????? ??????
			if(checkLinkExecTimeYN(linkCycleHr, lastExcnTm)){

				//?????? ??????????????? ????????? ??? ????????? ???????????? ??????
				if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){ //3 ?????? endTm ???????????? ?????????
		            String tempLinkDatEndTm = this.linkDatEndTm;
		            
					for(int i=0; i<2; i++){
						if(i==0)
						{
							this.linkDatEndTm = "23:59:59";
							
							String provStringData = excnCallProvRowData(1);
							if(provStringData != "5"){ //?????? ???????????? ?????? return??????
								try{ //??????????????? ???????????? ?????? ??????????????????
									storeProvRowDataToDB(getProvDataListByJackson(provStringData));	//???????????? ?????? 									
							        //this.linkHstNo2 = infoService.selectLinkHstNo("2"); //??????????????? 
							        insertProvLinkHst("2", "Y");
								}
								catch(StringIndexOutOfBoundsException e){
							    	 //this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
							         insertProvLinkHst("2", "N"); 
							         insertLinkErrLog(this.linkHstNo2,"3",String.valueOf("CallAndStoreJob excnCallAndStoreJob() ?????????, ????????? ???????????? ?????? ????????????"));													
								}catch(Exception e){
							    	 //this.linkHstNo2 = infoService.selectLinkHstNo("2"); 
							         insertProvLinkHst("2", "N"); 
							         insertLinkErrLog(this.linkHstNo2,"4","CallAndStoreJob excnCallAndStoreJob() ?????????, Exception ??????");
								}
							}
						}
						if(i==1){
							
			                this.linkDatBginTm = "00:00:59";
			                this.linkDatEndTm = tempLinkDatEndTm;
			                this.linkDatYmd = getTomorrowYmd(this.linkDatYmd);						
							
							String provStringData = excnCallProvRowData(1);
							if(provStringData != "5"){
								//???????????? ?????? 
								storeProvRowDataToDB(getProvDataListByJackson(provStringData));		
							}else{
								return;
							}			
							
					        //this.linkHstNo2 = infoService.selectLinkHstNo("2"); //??????????????? 
					        insertProvLinkHst("2", "Y");	
						}					
					}	
				}else{
					
					String provStringData = excnCallProvRowData(1);
					if(provStringData == "5"){return;}
					storeProvRowDataToDB(getProvDataListByJackson(provStringData));
					
			        //this.linkHstNo2 = infoService.selectLinkHstNo("2"); //??????????????? 
			        insertProvLinkHst("2", "Y");
				}
		         
			}else{
				//??????????????? ???????????? ?????? ???????????? ?????? ??????
				return;
			}
		}catch(SQLException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"2","CallAndStoreJob excnCallAndStoreJob() SQLException ?????? "); //+e.getMessage()
		
		}catch(ParseException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"3","CallAndStoreJob excnCallAndStoreJob() ParseException ??????");			
		}
		catch(StringIndexOutOfBoundsException e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"3","CallAndStoreJob excnCallAndStoreJob(), ????????? ???????????? ?????? ????????????");					
		}
		catch(Exception e){
	         insertProvLinkHst("2", "N"); 
	         insertLinkErrLog(this.linkHstNo2,"4","CallAndStoreJob excnCallAndStoreJob() Exception ??????"); //+e.getMessage()
		}
	}	
	
}
