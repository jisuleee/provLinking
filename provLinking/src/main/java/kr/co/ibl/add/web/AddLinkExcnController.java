package kr.co.ibl.add.web;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(value="/addLink/")
@Controller
public class AddLinkExcnController {
	
	@Autowired 
	private AddLinkComponent addLinkComponent;
	
	/**
     * 실증화 연계 재실행/추가연계
     * @param unknown
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="excn.do", method={RequestMethod.GET,RequestMethod.POST})
    public Map<String, Object> excnRedoLink(@RequestParam HashMap<String,Object> map, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception{

    	
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
    	String linkDatYmd = request.getParameter("linkDatYmd");
    	String linkDatBginTm = request.getParameter("linkDatBginTm");
    	String linkDatEndTm = request.getParameter("linkDatEndTm");
    	
    	try{
	    	if(!addLinkComponent.excnStoreProvRowData(linkDatBginTm,linkDatEndTm,linkDatYmd)){
	    		resultMap.put("error", "Y");
	    		//변환작업 진행방지
	    		return resultMap;
	    	};
	    	resultMap.put("error", "N");
	    	
	    	addLinkComponent.excnChgAddRowData(linkDatBginTm,linkDatEndTm,linkDatYmd);
	    	resultMap.put("error", "");
	    	
    	}catch(Exception e){
    		resultMap.put("error", "exception발생");    
    	}
    	
        return resultMap;
    }	

}
