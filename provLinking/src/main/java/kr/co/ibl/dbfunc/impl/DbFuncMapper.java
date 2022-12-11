package kr.co.ibl.dbfunc.impl;

import java.util.HashMap;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("DbFuncMapper")
public interface DbFuncMapper {
	int chgRowDataFuncCall(HashMap map) throws Exception;

	int chgAddRowDataFuncCall(HashMap map) throws Exception;
}


