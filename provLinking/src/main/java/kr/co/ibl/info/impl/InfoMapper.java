package kr.co.ibl.info.impl;

import java.util.HashMap;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("InfoMapper")
public interface InfoMapper {
	
	public HashMap<String, Object> selectLinkCycleHr(HashMap map) throws Exception; 
	
	public HashMap<String, Object> selectLastExcnTime(String linkType) throws Exception;

	public HashMap<String, Object> selectLastDataYmd(String linkType) throws Exception;
	
	public HashMap<String, Object> selectLastDataEndTime(String linkType) throws Exception;
	
	public int increExcnCount(String linkHstNo) throws Exception;
		
	public String selectLinkHstNo(String linkType) throws Exception;

	public int insertProvLinkHst(HashMap map) throws Exception; //String linkHstNo,int count, int linkKndCd

	public int insertProvLinkErrLog(HashMap map) throws Exception;

	public void mergeProvRowData(String linkDatYmd) throws Exception;

	public void deleteProvMergeData() throws Exception;
}
