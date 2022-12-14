package kr.co.ibl.add.web;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibleaders.utility.ib_json.JSONArray;
import com.ibleaders.utility.ib_json.JSONObject;
import com.ibleaders.utility.ib_json.parser.JSONParser;

import kr.co.ibl.dbfunc.service.DbFuncService;
import kr.co.ibl.info.service.InfoService;


@Component
public class AddLinkComponent {

   @Autowired
   public InfoService infoService;
   
   @Autowired
   public DbFuncService dbFuncService;
      
   String apiString = "";
   public int count = 1; 
   
   String linkHstNo2 = "";
   String linkHstNo3 = ""; 
   String linkHstNo4 = ""; 
   String calllinkCycleHr = "";
   String storelinkCycleHr = "";
   String chglinkCycleHr = "";

   String linkDatBginTm = "";
   String linkDatEndTm = "";
   String linkDatYmd = "";

   
   public String getCallLinkCycleHr() throws SQLException, Exception{
      HashMap map1 = new HashMap();
      map1.put("link_knd_cd", "1");
      map1.put("link_yr", this.linkDatYmd.substring(0,4));		
      return (String) infoService.selectLinkCycleHr(map1).get("LINK_CYCLE_HR");
   }
   
   public String getStoreLinkCycleHr(){ //??????????????? ??????????????? ?????? 
      return this.calllinkCycleHr;
   }
   
   public String getChgLinkCycleHr() throws SQLException, Exception{
      HashMap map1 = new HashMap();
      map1.put("link_knd_cd", "3");
      map1.put("link_yr", this.linkDatYmd.substring(0,4));		
      return (String) infoService.selectLinkCycleHr(map1).get("LINK_CYCLE_HR");
   }   
   
	public void setThisLinkHstNo(String linkKndCd) throws Exception{
		if(linkKndCd.equals("4")){
			this.linkHstNo2 = (String) infoService.selectLinkHstNo(linkKndCd);
		}
		if(linkKndCd.equals("5")){
			this.linkHstNo3 = (String) infoService.selectLinkHstNo(linkKndCd);
		}
		if(linkKndCd.equals("6")){
			this.linkHstNo4 = (String) infoService.selectLinkHstNo(linkKndCd);
		}		
	}
	
   public void insertProvLinkHst(String linkKndCd, String successYn){
       try{    	   
    	   setThisLinkHstNo(linkKndCd);
    	   
		   HashMap map = new HashMap();    	       		   
	       String linkCycleHr = "";
	       //??????????????? ?????? ?????? x---------------------------------
	       if(linkKndCd.equals("1") || linkKndCd.equals("2"))
	       { linkCycleHr = this.calllinkCycleHr;}
	       if(linkKndCd.equals("3")){
	          linkCycleHr = this.chglinkCycleHr;
	       }
	       //-------------------------------------------	       	
	        map.put("link_cycle_hr", linkCycleHr); 
	        map.put("link_knd_cd", linkKndCd);
	        map.put("link_dat_ymd", linkDatYmd);
	        map.put("link_dat_bgin_tm", linkDatBginTm);
	        map.put("link_dat_end_tm", linkDatEndTm);
	        map.put("excn_cnt", "1");
	        map.put("scs_yn", successYn);
	        
	    	infoService.insertProvLinkHst(map);
       }catch (Exception e){
    	   String message = e.getMessage();
		   if(message.length()>300){
			   message = message.substring(0,300);
		   }
    		System.out.println("(????????????)???????????? ????????????: " + message);
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
       }catch (Exception e){
    	   System.out.println("(????????????)?????? ???????????????????????? : "+ message);
	   }
   }
   

   public String createProvApiUrl() throws IOException{
                   
	   org.springframework.core.io.Resource resource = new ClassPathResource("/properties/globals.properties");
       Properties props = PropertiesLoaderUtils.loadProperties(resource);
       
       /*
        StringBuilder urlBuilder = new StringBuilder("http://192.168.0.61:8080/waterclusterData/webService/json/dbsTerms.do?"); 
        urlBuilder.append(URLEncoder.encode("saveDate","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd, "UTF-8")); 
        urlBuilder.append("&" + URLEncoder.encode("startTime","UTF-8") + "=" + URLEncoder.encode(this.linkDatBginTm, "UTF-8")); 
        urlBuilder.append("&" + URLEncoder.encode("endTime","UTF-8") + "=" + URLEncoder.encode(this.linkDatEndTm, "UTF-8")); 
       */ 
        /*
		StringBuilder urlBuilder = new StringBuilder("http://211.55.195.130:18080/webService/json/dbsTerms.do?"); 
		
		urlBuilder.append(URLEncoder.encode("userId","UTF-8") + "=" + URLEncoder.encode("ibleader", "UTF-8")); 
		urlBuilder.append("&" + URLEncoder.encode("userPwd","UTF-8") + "=" + URLEncoder.encode("BE8E49F0616C91C1A8092E79DB5ABA28EBDB0596C00FE8A965A622E30BEFFF79", "UTF-8")); 
		urlBuilder.append("&" + URLEncoder.encode("startDt","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd.replace("-",""), "UTF-8")); 
		urlBuilder.append("&" + URLEncoder.encode("endDt","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd.replace("-",""), "UTF-8")); 
		urlBuilder.append("&" + URLEncoder.encode("tagId","UTF-8") + "=" + URLEncoder.encode("??????.RCS11.PHIT_FM_103_CV", "UTF-8")); 
		*/
		
		StringBuilder urlBuilder = new StringBuilder(props.getProperty("Globals.MiddleApiUrl")); /*URL*/ 		
        urlBuilder.append(URLEncoder.encode("linkDatYmd","UTF-8") + "=" + URLEncoder.encode(this.linkDatYmd, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("linkDatBginTm","UTF-8") + "=" + URLEncoder.encode(this.linkDatBginTm, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("linkDatEndTm","UTF-8") + "=" + URLEncoder.encode(this.linkDatEndTm, "UTF-8"));		
        
        return urlBuilder.toString().replace("%3A", ":");
   }
   
   
   public String excnCallProvRowData(int count) throws SQLException, Exception{
      
	  String linkType = "";
      linkType = "4";
      
      try {
           URL url = new URL(createProvApiUrl());
           
           HttpURLConnection conn = (HttpURLConnection) url.openConnection();
           conn.setRequestMethod("GET");
           conn.setRequestProperty("Content-type", "application/json");
           conn.setConnectTimeout(2000); // ?????? ???????????? ??????(2???) 
           System.out.println("Response code: " + conn.getResponseCode());
           
           
           BufferedReader rd;     
           
           if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) 
           { //conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300  1>2
               rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
               
               //??????????????? ??????????????? 
               if(count != 1) //??????????????? ??? ??????
               {
                  //???????????? ????????????
                  infoService.increExcnCount(linkHstNo2); //or linkHstNo2
               }
               
              if(count == 1){
                 //?????????????????? ?????? seq??? ????????????.
                 linkType = "4"; //4-???????????? 5-???????????? 3-????????????
                 //this.linkHstNo2 = infoService.selectLinkHstNo(linkType);
                 //?????? ????????????????????? ???????????? ????????? ????????????. 
                 insertProvLinkHst(linkType,"Y");
              }
              
              StringBuilder sb = new StringBuilder();
              String line;
              while ((line = rd.readLine()) != null) {
                  sb.append(line);
              }
      
              apiString = sb.toString();     //???????????? 
              
              rd.close();
              conn.disconnect();
                         
           return apiString; 
               
               
           } else {
               //??????????????? ????????? ?????? 
              //???????????? ??????
              if(count == 1 ){   
                 //this.linkHstNo2 = infoService.selectLinkHstNo(linkType);
                 insertProvLinkHst(linkType,"N");
              }       
              
              insertLinkErrLog(linkHstNo2,"1","responseCode ??????");
               
              if(count != 1) //??????????????? ??? ??????
              {
            	  infoService.increExcnCount(linkHstNo2); 
              }

              //??????????????? ????????? (????????????)
              if(count != 5){
                 count++;                 
                 excnCallProvRowData(count);
                 return "5"; //???????????? (???????????? ??????)
              }              
              return "0";
              
           }          
       //???????????? ????????? ????????? ???????????? ???
      }catch(Exception e){
         //??????????????? ????????? ?????? 
           //???????????? ??????
           if(count == 1 )
           {     
              //this.linkHstNo2 = infoService.selectLinkHstNo(linkType);
              insertProvLinkHst(linkType,"N");
           }          
           insertLinkErrLog(linkHstNo2,"1",e.getMessage());
            
           if(count != 1) //??????????????? ??? ??????
           {
               infoService.increExcnCount(linkHstNo2); 
           }

           //??????????????? ????????? (????????????)
           if(count != 5){
              count++;            
              excnCallProvRowData(count);
              return "5"; //?????? ??????(???????????? ??????)
           }              
          return "0"; 
      }
   }
   
   
   public JSONObject parseStringToJson(String provStringData) throws Exception, ParseException{
      
      JSONParser jsonParser = new JSONParser();   
      JSONObject obj = new JSONObject();          
  
       obj = (JSONObject) jsonParser.parse(provStringData);                          

      return obj;      
   }
    
   
   //????????? ?????? ????????? ???????????? ?????? ?????????
   public List<HashMap<String,Object>> getProvDataList(String provStringData) throws Exception {
      

	  JSONObject provDataJson = parseStringToJson(provStringData);   
	  JSONArray parse_items = (JSONArray) provDataJson.get("data");         
	  List<HashMap<String,Object>> provDataList = new ArrayList<HashMap<String,Object>>();
        
      for(int i=0; i<parse_items.size(); i++) { //parse_items
         
         HashMap<String,Object> provData = new HashMap<String,Object>();
         JSONObject firstItem = (JSONObject)parse_items.get(i);    //parse_items
         
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
   
   public int storeProvRowDataToDB2(List<HashMap<String,Object>> provDataList) throws Exception, SQLException{   
       
       if(provDataList.size() == 0){
    	  throw new StringIndexOutOfBoundsException(); 
       }
       
	  //?????? ?????? ??????
      //this.linkHstNo3 = linkProvDataService.getLinkHstNo("2"); //???????????????   
      
      Connection con = null;
      PreparedStatement pstmt = null ;
      
      String sql = "INSERT INTO TB_PROV_ROW_DAT_TMPR (DVICE_ID, MSUR_VAL, CRT_DT, CRT_YMD ) VALUES (?, ?, ?, ?)";
      
      
      Class.forName("oracle.jdbc.OracleDriver"); //oracle.jdbc.OracleDriver
//	    con = DriverManager.getConnection(" jdbc:mysql://127.0.0.1:3306/TEST_DB", "test_user", "12345");
//	    con = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.196:1521:ORCLCDB", "watercluster", "water12");
        con = DriverManager.getConnection("jdbc:oracle:thin:@si.ibleaders.co.kr:1522:KWCDB", "watercluster", "water12##");
     
        con.setAutoCommit(false);
        
         pstmt = con.prepareStatement(sql) ;
         
         for(int i=0; i<provDataList.size(); i++){           
        	    /*     	 
            pstmt.setString(1, String.valueOf(provDataList.get(i).get("dvice_id")));
            pstmt.setString(2, String.valueOf(provDataList.get(i).get("msur_val")));
            pstmt.setString(3, String.valueOf(provDataList.get(i).get("crt_dt")));
            pstmt.setString(4, String.valueOf(provDataList.get(i).get("crt_ymd")));
*/
        	 pstmt.setString(1,(String) provDataList.get(i).get("TAG_NAME"));
        	 pstmt.setString(2, Double.toString((Double)provDataList.get(i).get("XVALUE")));
        	 pstmt.setString(3, (String) provDataList.get(i).get("SAVE_TIME"));
        	 pstmt.setString(4, (String) provDataList.get(i).get("SAVE_DATE"));
             
            // addBatch??? ??????
            pstmt.addBatch();
            
            // ???????????? Clear
            pstmt.clearParameters() ;

                     
            // OutOfMemory??? ???????????? ?????? ????????? ??????
            if( (i % 5000) == 0){
               
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
   	  
         //insertProvLinkHst("5", "Y");
         
         try {
            con.rollback() ;
         } catch (SQLException e1) {
               e1.printStackTrace();
         }           
         finally{
            if (pstmt != null) try {pstmt.close();pstmt = null;} catch(SQLException ex){}
            if (con != null) try {con.close();con = null;} catch(SQLException ex){}
         }
         
         //??????????????? ??????????????? ??????
         infoService.mergeProvRowData(this.linkDatYmd);
                
      return 1;
   }
  
   
   public boolean excnStoreProvRowData(String linkDatBginTm,String linkDatEndTm, String linkDatYmd){
      
      try{       
         this.linkDatBginTm = linkDatBginTm;
         this.linkDatEndTm = linkDatEndTm;
         this.linkDatYmd = linkDatYmd;    	  
         //????????????????????? ???????????? ????????? ????????? get
         //??????????????? ???????????? ?????? ??????
         this.calllinkCycleHr = getCallLinkCycleHr();
         
//	       linkDatYmd = "2022-01-07";
//	       linkDatBginTm = "23:00:00";
//	       linkDatEndTm = "00:10:00";
         
         //?????? ??????????????? ????????? ??? ????????? ???????????? ??????
         if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){ //3 ?????? endTm ???????????? ?????????
            
            String tempLinkDatEndTm = this.linkDatEndTm;
            for(int i=0; i<2; i++){
               if(i==0)
               {
				  this.linkDatEndTm = "23:59:00";
					
				  String provStringData = excnCallProvRowData(1);
				  if(provStringData != "5"){
						//???????????? ?????? 
						storeProvRowDataToDB2(getProvDataListByJackson(provStringData));			
			      }else{
						
				  }            	   
               }
               if(i==1){
                  this.linkDatBginTm = "00:00:00";
                  this.linkDatEndTm = tempLinkDatEndTm;
                  
                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                  Date linkDat = sdf.parse(linkDatYmd);
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(linkDat);
                  cal.add(Calendar.DATE, 1);
                  String tomorrow = sdf.format(cal.getTime());
                  this.linkDatYmd = tomorrow;
                  
                  String provStringData = excnCallProvRowData(1);
				  if(provStringData != "5"){
						//???????????? ?????? 
				      storeProvRowDataToDB2(getProvDataListByJackson(provStringData));													
			      }else{
						return false;
				  }		  
               }
            }   
         }else{
            
        	String provStringData = excnCallProvRowData(1);
            if(provStringData == "5"){return false;}
			storeProvRowDataToDB2(getProvDataListByJackson(provStringData));
         }
         
         //this.linkHstNo3 = infoService.selectLinkHstNo("2"); //???????????????  //????????? ???????????? ???????????????
         insertProvLinkHst("5", "Y");
         
         return true;
         
      }
      catch(SQLException e){
	  
         insertProvLinkHst("5", "N"); 
         insertLinkErrLog(this.linkHstNo3,"2",String.valueOf(e.getMessage()));
         
         return false;
      }
      catch(ParseException e){
	  
         insertProvLinkHst("5", "N"); 
         insertLinkErrLog(this.linkHstNo3,"3",String.valueOf(e.getMessage()));        
         return false;
      }
	  catch(StringIndexOutOfBoundsException e){
	     insertProvLinkHst("5", "N"); 
	     insertLinkErrLog(this.linkHstNo3,"3",String.valueOf("????????? ???????????? ?????? ????????????"));		
	     return false;
	  }      
      catch(Exception e){
         
    	 //throw??? ?????? ????????????...  	  
    	 //throw Exception ??? ????????????
         insertProvLinkHst("5", "N"); 
         insertLinkErrLog(this.linkHstNo3,"4", String.valueOf(e.getMessage()));
         
         return false;
      }
   }
      
   
   public void chgRowDataFuncCall() throws Exception{
      HashMap map = new HashMap();   
      map.put("link_dat_ymd", linkDatYmd);
      map.put("link_dat_bgin_tm",linkDatBginTm);
      map.put("link_dat_end_tm",linkDatEndTm);
      map.put("link_cycle_hr", this.chglinkCycleHr.trim());
      map.put("save_date", linkDatYmd); 
      
      dbFuncService.chgRowDataFuncCall(map);
   }
   
   public void chgAddRowDataFuncCall() throws Exception{
	      HashMap map = new HashMap();   
	      
	      String bgin_tm_rnk = "";
	      String bgin_time_rnk = "";
	      map.put("link_dat_ymd", linkDatYmd);
	      map.put("link_dat_bgin_tm",linkDatBginTm);
	      map.put("link_dat_end_tm",linkDatEndTm);
	      map.put("link_cycle_hr", this.chglinkCycleHr.trim());
	      
	      if(linkDatBginTm.charAt(0)=='0'){
	    	  bgin_time_rnk = linkDatBginTm.substring(1,2);
	      }else{
	    	  bgin_time_rnk = linkDatBginTm.substring(0,2);
	      }
	      map.put("bgin_tm_rnk", bgin_time_rnk);
	      map.put("save_date", linkDatYmd); 
	      
	      dbFuncService.chgAddRowDataFuncCall(map);
   }
   
   
   public void excnChgProvRowData(String linkDatBginTm,String linkDatEndTm, String linkDatYmd){ //excnChgProvRowData()  callChgRowDataFunc

      try{        
    	  
         this.linkDatBginTm = linkDatBginTm;
         this.linkDatEndTm = linkDatEndTm;
         this.linkDatYmd = linkDatYmd;
         //??????????????? ???????????? ?????? ??????
         this.chglinkCycleHr = getChgLinkCycleHr();
         
         //linkDatYmd = "2022-01-07";
         //linkDatBginTm = "23:00:00";
         //linkDatEndTm = "00:10:00";
         //??????
         
         if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){
            String tempLinkDatEndTm = this.linkDatEndTm;
            
            for(int i=0; i<2; i++){
               if(i==0){
                  this.linkDatEndTm = "23:59:99";   
                  chgRowDataFuncCall();   
               }
               if(i==1){
                  this.linkDatBginTm = "00:00:00";
                  this.linkDatEndTm = tempLinkDatEndTm;
                  
                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                  Date linkDat = sdf.parse(linkDatYmd);
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(linkDat);
                  cal.add(Calendar.DATE, 1);
                  String tomorrow = sdf.format(cal.getTime());
                  
                  this.linkDatYmd = tomorrow;
                  chgRowDataFuncCall();   
               }
            }               
         }else{
        	 chgRowDataFuncCall();   
         }

      }
      catch(SQLException e){           
         insertProvLinkHst("6", "N");   
         insertLinkErrLog(linkHstNo4,"2",String.valueOf(e.getMessage()));      
       }      
      catch(Exception e){         	  
         insertProvLinkHst("6", "N");   
         insertLinkErrLog(linkHstNo4,"4",String.valueOf(e.getMessage()));      
      }
   }

   
   public void excnChgAddRowData(String linkDatBginTm,String linkDatEndTm, String linkDatYmd){ 

	      try{                     
	         //??????????????? ???????????? ?????? ??????
	         this.chglinkCycleHr = getChgLinkCycleHr();
	         
	         //linkDatYmd = "2022-01-07";
	         //linkDatBginTm = "23:00:00";
	         //linkDatEndTm = "00:10:00";
	         //??????
	         this.linkDatBginTm = linkDatBginTm;
	         this.linkDatEndTm = linkDatEndTm;
	         this.linkDatYmd = linkDatYmd;
	         
	         if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){
	            String tempLinkDatEndTm = this.linkDatEndTm;
	            
	            for(int i=0; i<2; i++){
	               if(i==0){
	                  this.linkDatEndTm = "23:59:99";   
	                  chgAddRowDataFuncCall();   
	               }
	               if(i==1){
	                  this.linkDatBginTm = "00:00:00";
	                  this.linkDatEndTm = tempLinkDatEndTm;
	                  
	                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	                  Date linkDat = sdf.parse(linkDatYmd);
	                  Calendar cal = Calendar.getInstance();
	                  cal.setTime(linkDat);
	                  cal.add(Calendar.DATE, 1);
	                  String tomorrow = sdf.format(cal.getTime());
	                  
	                  this.linkDatYmd = tomorrow;
	                  chgAddRowDataFuncCall();   
	               }
	            }               
	         }else{
	        	 chgAddRowDataFuncCall();
	         }

	      }	  
	      catch(SQLException e){     
	         insertProvLinkHst("6", "N");   
	         insertLinkErrLog(linkHstNo4,"2",String.valueOf(e.getMessage()));
	      }	      
	      catch(Exception e){     
	         insertProvLinkHst("6", "N");   
	         insertLinkErrLog(linkHstNo4,"4",String.valueOf(e.getMessage()));
	      }
	   }       
}
