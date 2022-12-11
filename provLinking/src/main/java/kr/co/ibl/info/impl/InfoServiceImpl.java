package kr.co.ibl.info.impl;

import java.util.HashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import kr.co.ibl.info.service.InfoService;

@Service("InfoService")
public class InfoServiceImpl implements InfoService{

	@Resource(name="InfoMapper")
	private InfoMapper infoMapper;
	
	@Override
	public HashMap<String, Object> selectLinkCycleHr(HashMap map) throws Exception {
		return infoMapper.selectLinkCycleHr(map);
	}

	@Override
	public HashMap<String, Object> selectLastExcnTime(String linkType) throws Exception {
		return infoMapper.selectLastExcnTime(linkType);
	}

	@Override
	public HashMap<String, Object> selectLastDataYmd(String linkType) throws Exception {
		return infoMapper.selectLastDataYmd(linkType);
	}

	@Override
	public HashMap<String, Object> selectLastDataEndTime(String linkType) throws Exception {
		return infoMapper.selectLastDataEndTime(linkType);
	}

	@Override
	public void increExcnCount(String linkHstNo) throws Exception{
		infoMapper.increExcnCount(linkHstNo);
	}

	@Override
	public String selectLinkHstNo(String linkType) throws Exception{
		return infoMapper.selectLinkHstNo(linkType);
	}

	@Override
	public void insertProvLinkHst(HashMap map) throws Exception{
		infoMapper.insertProvLinkHst(map);
	}

	@Override
	public void insertProvLinkErrLog(HashMap map) throws Exception{
		infoMapper.insertProvLinkErrLog(map);
	}

	@Override
	public void mergeProvRowData(String linkDatYmd) throws Exception {
		infoMapper.mergeProvRowData(linkDatYmd);
		infoMapper.deleteProvMergeData();
	}
	
}
