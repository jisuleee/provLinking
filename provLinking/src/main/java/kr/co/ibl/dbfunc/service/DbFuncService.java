package kr.co.ibl.dbfunc.service;

import java.sql.SQLException;
import java.util.HashMap;

public interface DbFuncService {
	public int chgRowDataFuncCall(HashMap map) throws SQLException,Exception;
	
	public int chgAddRowDataFuncCall(HashMap map) throws SQLException,Exception;
}
