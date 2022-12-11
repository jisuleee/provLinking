package kr.co.ibl.info.service;

import java.util.HashMap;

public interface InfoService {

	/**
	 * 실행할 연계작업 연계주기 정보
	 * @return Map<String, String>
	 */
	public HashMap<String, Object> selectLinkCycleHr(HashMap map) throws Exception; 
	
	/**
	 * 가장 최근에 실행된 데이터 마지막 시각 정보
	 * @return Map<String, String>
	 */
	public HashMap<String, Object> selectLastExcnTime(String linkType) throws Exception;

	/**
	 * 최근에 실행된 데이터 연월일 정보
	 * @return Map<String, String>
	 */
	public HashMap<String, Object> selectLastDataYmd(String linkType) throws Exception;
	
	/**
	 * 최근에 실행된 데이터 종료시각 정보
	 * @return Map<String, String>
	 */
	public HashMap<String, Object> selectLastDataEndTime(String linkType) throws Exception;
	
	/**
	 * 실행 횟수 증가 
	 * @return Map<String, String>
	 */		
	public void increExcnCount(String linkHstNo) throws Exception;
		
	/**
	 * 최근 연계이력 넘버 정보 
	 * @param String (연계종류) 1:호출 2:적재 3:변환
	 * @return Map<String, String>
	 */		
	public String selectLinkHstNo(String linkType) throws Exception;
	
	/**
	 * 연계 이력 저장
	 * @return Map<String, String>
	 */	
	public void insertProvLinkHst(HashMap map) throws Exception; //String linkHstNo,int count, int linkKndCd
	
	/*
	 * 실증화 연계 에러로그 저장
	 * (프로그램 에러)
	 */
	public void insertProvLinkErrLog(HashMap map) throws Exception;

	
	public void mergeProvRowData(String linkDatYmd) throws Exception;
}
