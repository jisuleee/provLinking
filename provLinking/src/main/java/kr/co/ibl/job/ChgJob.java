package kr.co.ibl.job;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.ibl.dbfunc.service.DbFuncService;
import kr.co.ibl.info.service.InfoService;

@Service("chgJob")
public class ChgJob {
	
	@Autowired
	private InfoService infoService;
	
	@Autowired
	private DbFuncService dbFuncService;

	String linkHstNo = "";
	String saveDate = "";
	String linkCycleHr = "";
	String linkDatYmd = "";
	String linkDatBginTm = "";
	String linkDatEndTm = "";
	
	
	//변환작업 실행시간 여부 확인	(변환작업은 60분주기로 고정)
	public boolean checkChgExcnTimeYN(String linkCycleHr, Date lastExcnTm){   //checkChgTimeYN  checkExecTimeYN
		
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //HH:mm:ss
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastExcnTm);
		
		cal.add(Calendar.MINUTE, Integer.parseInt(linkCycleHr.trim())); 
		String linkExcnTmDate = sdformat.format(cal.getTime());

		Date nowDate = new Date();
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(nowDate);
		
		String nowTmDate = sdformat.format(cal2.getTime());
		
		if(linkExcnTmDate.equals(nowTmDate)){ //이거 ss단위까지 맞아야함; 수정필요 
			return true;
		}else{
			return false;
		}
	}
	
	
	public String getLinkCycleHr() throws Exception{
		HashMap map1 = new HashMap();
		map1.put("link_knd_cd", "3");
		map1.put("link_yr", this.linkDatYmd.substring(0,4));		
		return (String) infoService.selectLinkCycleHr(map1).get("LINK_CYCLE_HR");
	}
	
	
	public String getLastDataYmd() throws Exception{
		String link_knd_cd = "3";
		return (String) infoService.selectLastDataYmd(link_knd_cd).get("LINK_DAT_YMD"); //받아옴 
	}
	
	
	public Date getLastExcnTime() throws Exception{
		String link_knd_cd = "3";
		return (Date) infoService.selectLastExcnTime(link_knd_cd).get("EXCN_DT");
	}
	
	
	public String getLastDataEndTime() throws Exception{
		String link_knd_cd = "3";
		return (String) infoService.selectLastDataEndTime(link_knd_cd).get("LINK_DAT_END_TM");
	}
	
	
	public String getLinkDatEndTm(String linkDatBginTm, String linkCycleHr) throws Exception{
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
	
	public void setLinkHstNo(String link_knd_cd) throws Exception{
		this.linkHstNo = (String) infoService.selectLinkHstNo(link_knd_cd);
	}
	
    public void insertProvLinkHst(String linkKndCd, String successYn){
	    try{
	    	setLinkHstNo(linkKndCd);
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
	    	System.out.println("(주기연계 변환)연계이력 등록 오류");
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
       catch(Exception e){
       		System.out.println("(주기연계 변환)에러정보 등록 오류");
       }
    }
    
	
	public void callChgRowDataDBFunc() throws Exception{
		HashMap map = new HashMap();

		map.put("link_dat_ymd", this.linkDatYmd); //추가연계시 다름
		map.put("link_dat_bgin_tm",this.linkDatBginTm);
		map.put("link_dat_end_tm",this.linkDatEndTm);
		map.put("link_cycle_hr", this.linkCycleHr.trim());
		map.put("save_date", this.saveDate); //savedDate
		
		dbFuncService.chgRowDataFuncCall(map);
	}
		

	public void excnChgJob(){ //excnChgProvRowData()  callChgRowDataFunc
		
		String linkDatYmd = "";
		String linkDatBginTm ="";
		String linkDatEndTm = "";		
		String linkCycleHr = "";
		Date lastExcnTm;
		Date linkDatBginDt;
		Date date = new Date();
		
		try{
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
			
			linkDatEndTm = getLinkDatEndTm(linkDatBginTm, linkCycleHr);
			
			this.linkCycleHr = linkCycleHr;
	        this.linkDatBginTm = linkDatBginTm;
	        this.linkDatEndTm = linkDatEndTm;
	        
					
			//현재는 테스트를 위해 false! 설정
			if(!checkChgExcnTimeYN(linkCycleHr, lastExcnTm)){
				
				if(Integer.parseInt(linkDatBginTm.replace(":", "")) > Integer.parseInt(linkDatEndTm.replace(":", ""))){
		            String tempLinkDatEndTm = this.linkDatEndTm;
		            
					for(int i=0; i<2; i++){
						if(i==0){
		                    this.linkDatEndTm = "23:59:99";   
		                    callChgRowDataDBFunc();  
						}
						if(i==1){
			                this.linkDatBginTm = "00:00:00";
			                this.linkDatEndTm = tempLinkDatEndTm;			
			                this.linkDatYmd = getTomorrowYmd(this.linkDatYmd);
			                
		                    callChgRowDataDBFunc();  
						}
					}					
				}else{
                    callChgRowDataDBFunc();  
				}
				
			}else{
				return;
			}
		}
		//db프로시저에서 잡지못하는 에러 => try-catch로 넘길수있도록 //테이블 notnull 설정같은거 여기서받음
	    catch(SQLException e){     
	         insertProvLinkHst("3", "N");   
	         insertLinkErrLog(linkHstNo,"2",String.valueOf(e.getMessage()));      
	    }	      
	    catch(Exception e){     
	         insertProvLinkHst("3", "N");   
	         insertLinkErrLog(linkHstNo,"4",String.valueOf(e.getMessage()));      
	    }		
	}	

	
}
