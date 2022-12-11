package kr.co.ibl.dbfunc.impl;

import java.sql.SQLException;
import java.util.HashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import kr.co.ibl.dbfunc.service.DbFuncService;

@Service("DbFuncServiceImpl")
public class DbFuncServiceImpl implements DbFuncService{

	@Resource(name="DbFuncMapper")
	private DbFuncMapper dbFuncMapper;
	
	@Override
	public int chgRowDataFuncCall(HashMap map) throws Exception{
		return dbFuncMapper.chgRowDataFuncCall(map);
	}

	@Override
	public int chgAddRowDataFuncCall(HashMap map) throws SQLException, Exception {
		return dbFuncMapper.chgAddRowDataFuncCall(map);
	}
}
