package com.foursoft.selenium.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.foursoft.selenium.AutoTestingFramework.valueobject.AssertionResultBean;
import com.foursoft.selenium.AutoTestingFramework.valueobject.DataProviderValueObject;

/**
 * @author AkarshA
 * */
public final class DAOUtil {
	private String[][] fileNames = null;
private static DAOUtil dao=null;
private final String beanPackage = "com.foursoft.selenium.AutoTestingFramework.bean.";
private String dbSchemaName=null;
private DAOUtil(){}


//for reports
public static int reportMainScenarioNo;
public static int reportSubScenarioNo;
public static Long reportSWBDataSetID;
public static int reportSWBFunctionID;
public static int reportSWBValidationsID;


/**
 * This method returns the instance of DAOUtil class
 * */
public static DAOUtil getInstance(){
	if(dao==null){
		dao=new DAOUtil();
	}
	return dao;
}
	/*public String[][] getFileNames() {
		return fileNames;
	}

	public void setFileNames(String[][] fileNames) {
		this.fileNames = fileNames;
	}*/
	private int calTestCaseCount(Connection con,String fName){
		String updateQuery="SELECT MAX(testCaseSn) FROM "+fName;
		System.out.println("update query::"+updateQuery);
		int count=0;
		ResultSet rs = null;
		try {
			PreparedStatement preparedStatement = con.prepareStatement(updateQuery);
			//preparedStatement.setString(1, fileName);
			
			rs = preparedStatement.executeQuery();
			if(rs.next()){
				count=rs.getInt(1);	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return count;
	}
	/**
	 * This method fetch data from all the tables specified, maps to corresponding bean.
	 * <br/>NOTE:This method is used in Abstract layer of framework not to be used while writing test cases. Use getAllFromTable.
	 * @param vo 
	 * @return object array with one column and rows equal to test iteration count
	 *
	 * */
	ArrayList<String> arrayList;
	public Object[][] getTestCaseData(DataProviderValueObject vo){
		String[][] fileNames = vo.getFileName();
		Connection con = ConnectionUtil.getConnection(vo.getDbSchemaName());
		dbSchemaName=vo.getDbSchemaName();
		System.out.println("rows in filename array:"+fileNames.length);
		
		String query = "SELECT DISTINCT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME='DATA_SET_NO' AND TABLE_SCHEMA='"+dbSchemaName+"'";
		arrayList = new ArrayList<String>();
		try {
			ResultSet rs = con.prepareStatement(query).executeQuery();
			while (rs.next()) {              
		        int i = 1;
		        while(i <= 1) {
		            arrayList.add(rs.getString(i++));
		        }
		        /*System.out.println(rs.getString("Col 1"));
		        System.out.println(rs.getString("Col 2"));
		        System.out.println(rs.getString("Col 3"));                    
		        System.out.println(rs.getString("Col n"));*/
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//int testDataCount = Integer.parseInt(fileNames[0][1]);
		String[] fname = fileNames[0];
		
		ArrayList<Object> fileData = new ArrayList<Object>();
		int testDataCount=0;
		int nextIndexToParse=0;
		for (String[] tempName  : fileNames) {
			fname=tempName;
			nextIndexToParse++;
			fileData = selectDataFromFile(fname[0],con,vo.getSubScenarioNo(),vo.getDataSetNo()); //Modified by Dineshs added sub scenario
			 testDataCount = calTestCaseCount(con, fname[0]);
			if(testDataCount==0&&fileData.size()>0){
				testDataCount=fileData.size();
			}
			if(testDataCount>0){
				break;
			}
		}
		//Object[][] testDataList = new Object[fileData.size()/Integer.parseInt(fileNames[0][1])][fileNames.length];
		//Object[][] testDataMap = new Object[fileData.size()/testDataCount][1];
		Object[][] testDataMap = new Object[testDataCount][2];
		Iterator<Object> it=null;
		for(int i=0;i<testDataMap.length;i++){
			testDataMap[i][0] = new HashMap();
			testDataMap[i][1] = 0;
		}
		for(int i=nextIndexToParse;i<=fileNames.length;i++){
			boolean flag =false;
			int c = 0;
			
			int rec = 1;
			
			if(fname[1]!=null&&!fname[1].equals("")){
			 rec = Integer.parseInt(fname[1]);
			 //flag=true;
			 }
			Object[] temp=null;
			//if(flag==false){
			if(fileData.size()>0){
				it = fileData.iterator();
				Object dt = it.next();
				String value = invokeGetter(dt, "testCaseSn");
				if(value!=null&&!value.equals("")){
					
					for(int j=1;j<=testDataCount;j++){
						ArrayList al = new ArrayList(10);
						while(value!=null&&Integer.parseInt(value)==j){
							/*if(Integer.parseInt(value)!=j){
								break;
							}*/
							al.add(dt);
							if(it.hasNext()){
							dt=it.next();
							value = invokeGetter(dt, "testCaseSn");
							}else{
								value=null;
							}
							
						}
						temp = al.toArray();
						HashMap hm = (HashMap)testDataMap[c++][0]; 
						hm.put(fname[0], temp);
					}
				}else{
					flag=true;
				}
			//}
			 if(flag==true){
				 it = fileData.iterator();
			while(it.hasNext()){
				
				 temp = new Object[rec];
			for(int j=0;j<rec;j++){
				temp[j] = it.next();
				
			}
			
			HashMap hm = (HashMap)testDataMap[c++][0]; 
			hm.put(fname[0], temp);
			//testDataList[c++][i-1]=temp;
			}}
			 }
			if((i)<fileNames.length){
				fname = fileNames[i];
				fileData = selectDataFromFile(fname[0],con,vo.getSubScenarioNo(),vo.getDataSetNo()); //Modified by Dineshs added sub scenario
			}
		}
		ConnectionUtil.closeSqlResourse(con, null, null);
		return testDataMap;
	}
	/**
	 * This method fetches all the data from specified schemaName and table name.
	 * @param tableName 
	 * @param schemaName
	 * @return List of object(corresponding generated bean) each object represents a table row
	 * 
	 * */
	public static ArrayList<Object> getAllFromTable(String tableName, String schemaName){
		Connection con = ConnectionUtil.getConnection(schemaName);
		return getInstance().selectDataFromFile(tableName, con);
	}
	private ArrayList<Object> selectDataFromFile(String fileName, Connection con){
		 String selectQuery = "select * from ";
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		Object  bean=null;
		ArrayList<Object> list = new ArrayList<Object>();
		try {
			preparedStatement = con.prepareStatement(selectQuery+fileName);
			//preparedStatement.setString(1, fileName);
			rs = preparedStatement.executeQuery();
			while(rs.next()){
				bean = mapBeanFromResultSet(rs,fileName);
				list.add(bean);
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			ConnectionUtil.closeSqlResourse(null, preparedStatement, rs);
		}
		return list;
	}
	
	
	
	/*public int updateMainNSubScenarios(String seleniumTestId, String mainScenarioName,String subScenarioName,int dataSetNo) {
		Session session1=null;
		try
		{
		//saving the main scenario
		SwbMainScenarioHome swbMainScenarioHome=new SwbMainScenarioHome();
		SwbMainScenario swbMainScenario=new SwbMainScenario();
		SessionFactory sessionFactory=HibernateConnection.getInstance().getSessionFactory();
		//TODO .....replace this with the generic connection factory
		session1= sessionFactory.openSession();
		
		if(!checkMainScenario(mainScenarioName))
		{
		session1.beginTransaction();
		
		swbMainScenario.setSwbMainScenariocolName(mainScenarioName);
		swbMainScenarioHome.saveInstance(session1,swbMainScenario);
		session1.getTransaction().commit();
		}
		
		//saving the sub scenario
		SwbSubScenarioHome swbSubScenarioHome=new SwbSubScenarioHome();
		session1.beginTransaction();
		reportMainScenarioNo=swbMainScenarioHome.getIdSwbMainScenario(session1,mainScenarioName);
		SwbSubScenario swbSubScenario=new SwbSubScenario();
		swbSubScenario.setSwbSubScenariocolName(subScenarioName);
		swbMainScenario=new SwbMainScenario();
		swbMainScenario.setIdSwbMainScenario(reportMainScenarioNo);
		swbSubScenario.setSwbMainScenario(swbMainScenario);
		swbSubScenarioHome.saveInstance(session1, swbSubScenario);
		session1.getTransaction().commit();
		reportSubScenarioNo=swbSubScenarioHome.getIdSwbSubScenario(session1, subScenarioName, swbMainScenario);
		}
		catch (RuntimeException re) {
			 re.printStackTrace();
			throw re;
		}
		finally{
			session1.close();
		}
		
		//Saving the main scenario
		Connection swbReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
		String insertMainScenarioQueryString = ("insert into swb_reports.swb_main_scenario (SWB_Main_Scenariocol_Name,selenium_test_id) values (?,?)");;
		PreparedStatement insertMainScenarioPreparedStatement = null;
		boolean mainScenarioInserted=false;
		try
		{
			if(!checkMainScenario(mainScenarioName))
			{
			insertMainScenarioPreparedStatement=swbReportsCon.prepareStatement(insertMainScenarioQueryString);
			insertMainScenarioPreparedStatement.setString(1, mainScenarioName);
			insertMainScenarioPreparedStatement.setString(2, seleniumTestId);
			insertMainScenarioPreparedStatement.executeUpdate();
			mainScenarioInserted=true;
			}
		}
		catch (SQLException e) {
			e.printStackTrace(); 
		
		} finally {
 			ConnectionUtil.closeSqlResourse(null, insertMainScenarioPreparedStatement, null);
		}
		
		String insertSubScenarioQueryString = "insert into swb_reports.swb_sub_scenario (SWB_Sub_Scenariocol_Name,SWB_Main_Scenario_idSWB_Main_Scenario) values (?,?)";
		String insertDataSetQueryString=" insert into swb_reports.swb_dataset  (SWB_DATASET_NUM,SWB_Sub_Scenario_idSWB_Sub_Scenario)  values (?,?) ";
		
		PreparedStatement insertSubScenarioPreparedStatement = null;
		PreparedStatement insertDataSetPreparedStatement = null;
		
		boolean subScenarioInserted=false;
		boolean dataSetInserted=false;
		try
		{
			reportMainScenarioNo=returnMainScenarioNum(mainScenarioName);
			insertSubScenarioPreparedStatement=swbReportsCon.prepareStatement(insertSubScenarioQueryString);
			insertSubScenarioPreparedStatement.setString(1,subScenarioName);
			insertSubScenarioPreparedStatement.setInt(2,reportMainScenarioNo);
			insertSubScenarioPreparedStatement.executeUpdate();
			subScenarioInserted=true;
			returnSubScenarioNum(subScenarioName, reportMainScenarioNo);
			if(subScenarioInserted)
			{
				insertDataSetPreparedStatement=swbReportsCon.prepareStatement(insertDataSetQueryString);
				insertDataSetPreparedStatement.setInt(1, dataSetNo);
				insertDataSetPreparedStatement.setInt(2, reportSubScenarioNo);
				insertDataSetPreparedStatement.executeUpdate();
				dataSetInserted=true;
				
			}
			
			
		}
		catch (SQLException e) {
			e.printStackTrace(); 
		
		} finally {
 			ConnectionUtil.closeSqlResourse(swbReportsCon, insertSubScenarioPreparedStatement, null);
		}
		
		
		return 0;
	}*/
	
	
	
	
	
public int updateMainSubNDataSetDtls(String seleniumTestId, String mainScenarioName,String subScenarioName,int dataSetNo) {
		
		//Saving the main scenario
		Connection swbReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");;
		String insertMainScenarioQueryString = ("insert into swb_reports.swb_main_scenario (SWB_Main_Scenariocol_Name,selenium_test_id) values (?,?)");;
		PreparedStatement insertMainScenarioPreparedStatement = null;
		boolean mainScenarioInserted=false;
		try
		{
			if(!checkMainScenario(mainScenarioName))
			{
			insertMainScenarioPreparedStatement=swbReportsCon.prepareStatement(insertMainScenarioQueryString);
			insertMainScenarioPreparedStatement.setString(1, mainScenarioName);
			insertMainScenarioPreparedStatement.setString(2, seleniumTestId);
			insertMainScenarioPreparedStatement.executeUpdate();
			mainScenarioInserted=true;
			}
		}
		catch (SQLException e) {
			e.printStackTrace(); 
		
		} finally {
 			ConnectionUtil.closeSqlResourse(null, insertMainScenarioPreparedStatement, null);
		}
		
		String insertSubScenarioQueryString = "insert into swb_reports.swb_sub_scenario (SWB_Sub_Scenariocol_Name,SWB_Main_Scenario_idSWB_Main_Scenario) values (?,?)";
		String insertDataSetQueryString=" insert into swb_reports.swb_dataset  (SWB_DATASET_NUM,SWB_Sub_Scenario_idSWB_Sub_Scenario)  values (?,?) ";
		
		PreparedStatement insertSubScenarioPreparedStatement = null;
		PreparedStatement insertDataSetPreparedStatement = null;
		
		ResultSet insertDataSetResultSet=null;
		
		boolean subScenarioInserted=false;
		boolean dataSetInserted=false;
		try
		{
			reportMainScenarioNo=returnMainScenarioNum(mainScenarioName);
			insertSubScenarioPreparedStatement=swbReportsCon.prepareStatement(insertSubScenarioQueryString);
			insertSubScenarioPreparedStatement.setString(1,subScenarioName);
			insertSubScenarioPreparedStatement.setInt(2,reportMainScenarioNo);
			insertSubScenarioPreparedStatement.executeUpdate();
			subScenarioInserted=true;
			returnSubScenarioNum(subScenarioName, reportMainScenarioNo);
			if(subScenarioInserted)
			{
				insertDataSetPreparedStatement=swbReportsCon.prepareStatement(insertDataSetQueryString,PreparedStatement.RETURN_GENERATED_KEYS);
				insertDataSetPreparedStatement.setInt(1, dataSetNo);
				insertDataSetPreparedStatement.setInt(2, reportSubScenarioNo);
				insertDataSetPreparedStatement.executeUpdate();
				insertDataSetResultSet=insertDataSetPreparedStatement.getGeneratedKeys();
				
				if(insertDataSetResultSet.next())
				{
					reportSWBDataSetID=insertDataSetResultSet.getLong(1);
				}
				else
				{
					throw new SQLException("inserting the data set has blown up");
				}
				
				dataSetInserted=true;
				
			}
			
			
		}
		catch (SQLException e) {
			e.printStackTrace(); 
		
		} finally {
			ConnectionUtil.closeSqlResourse(null, insertDataSetPreparedStatement, null);
 			ConnectionUtil.closeSqlResourse(swbReportsCon, insertSubScenarioPreparedStatement, null);
		}
		
		
		return 0;
	}
	
	
	
	
	public boolean cleanReportsSchema() {
		/*try {
			SessionFactory sessionFactory=HibernateConnection.getInstance().getSessionFactory();
			Session session1 = sessionFactory.openSession();
			session1.beginTransaction();
			PreparedStatement deleteSPPreparedStatement;

			deleteSPPreparedStatement = session1.connection().prepareStatement("{call del_table_list()}");
			boolean truncateFlag = deleteSPPreparedStatement.execute();
			session1.getTransaction().commit();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		} catch (RuntimeException re) {
			re.printStackTrace();
			throw re;
		} */
		
		
		
		
		boolean reportsSchemaCleaned = false;
		Connection swbReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
		
		CallableStatement callableStatement = null;
		String delTableListQueryString = "call del_table_list()";
		
		
		try {
			callableStatement = swbReportsCon.prepareCall(delTableListQueryString);
			callableStatement.executeUpdate();
		} catch (SQLException e) {
			//e.printStackTrace();
			reportsSchemaCleaned=true;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally {
			if(callableStatement!=null)
			{
				try {
					callableStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ConnectionUtil.closeSqlResourse(swbReportsCon, null, null);
		}
		
		return reportsSchemaCleaned;
	}
	
	
	public static void main(String [] args)
	{
		DAOUtil d1=new DAOUtil();
		//d1.updateMainNSubScenarios("m1","m1","5");
		//d1.cleanReportsSchema();
		//d1.updateElisionDetails();
		
		
		try
		{
		//	d1.updateMainNSubScenarios(null,"mainScenario1","22");
			d1.cleanReportsSchema();
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/*private boolean deleteSWBReports()
	{
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
	}*/
	
	public static boolean checkMainScenario(String mainScenarioName) {
		boolean mainScenarioExists = false;
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");

		String selectQuery = ("select idSWB_Main_Scenario from swb_reports.swb_main_scenario where SWB_Main_Scenariocol_Name=?");
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		int i = 0;
		int mainScenarioNameID = 0;
		try {
				selectPreparedStatement = sebReportsCon.prepareStatement(selectQuery);
				selectPreparedStatement.setString(1, mainScenarioName);
				selectResultSet = selectPreparedStatement.executeQuery();

				while (selectResultSet.next()) {
					mainScenarioNameID = selectResultSet.getInt("idSWB_Main_Scenario");
				}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}

		if (mainScenarioNameID != 0) {
			mainScenarioExists = true;
		}
		
		return mainScenarioExists;
	}
	
	
	
	
	public static int returnMainScenarioNum(String mainScenarioName) {
		
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
		String selectQuery = ("select idSWB_Main_Scenario from swb_reports.swb_main_scenario where SWB_Main_Scenariocol_Name=?");
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		int i = 0;
		int mainScenarioNameID = 0;
		try {
				selectPreparedStatement = sebReportsCon.prepareStatement(selectQuery);
				selectPreparedStatement.setString(1, mainScenarioName);
				selectResultSet = selectPreparedStatement.executeQuery();

				while (selectResultSet.next()) {
					mainScenarioNameID = selectResultSet.getInt("idSWB_Main_Scenario");
				}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}

		reportMainScenarioNo=mainScenarioNameID;
		return mainScenarioNameID;
	}
	
	public static int returnSubScenarioNum(String subScenarioName,int fkFromSub2Main) {
		
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
		String selectQuery = ("SELECT idSWB_Sub_Scenario FROM swb_reports.swb_sub_scenario  where SWB_Sub_Scenariocol_Name=? and SWB_Main_Scenario_idSWB_Main_Scenario=?");
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		int i = 0;
		int subScenarioNameID = 0;
		try {
				selectPreparedStatement = sebReportsCon.prepareStatement(selectQuery);
				selectPreparedStatement.setString(1, subScenarioName);
				selectPreparedStatement.setInt(2, fkFromSub2Main);
				selectResultSet = selectPreparedStatement.executeQuery();

				while (selectResultSet.next()) {
					subScenarioNameID = selectResultSet.getInt("idSWB_Sub_Scenario");
				}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}

		reportSubScenarioNo=subScenarioNameID;
		return subScenarioNameID;
	}
	
	
	
	public static int returnValidationID() {
		
		Connection swbReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
		String selectQuery = ("select max(idSWB_validations) from swb_reports.swb_validations");
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		//int i = 0;
		int validationID = 0;
		try {
			selectPreparedStatement = swbReportsCon.prepareStatement(selectQuery);
				selectResultSet = selectPreparedStatement.executeQuery(selectQuery);

				while (selectResultSet.next()) {
					validationID = selectResultSet.getInt("max(idSWB_validations)");
				}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}

		reportSWBValidationsID=validationID;
		return validationID;
	}
	
	//select max(idSWB_validations) from swb_reports.swb_validations
	
	
	
	/*public int returnMainScenarioNum(String mainScenarioName)
	{
		
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");

		String selectQuery = ("select idSWB_Main_Scenario from swb_reports.swb_main_scenario where SWB_Main_Scenariocol_Name=?");

		
		
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		int i = 0;
		int mainScenarioNameID = 0;
		try {
				selectPreparedStatement = sebReportsCon.prepareStatement(selectQuery);
				selectPreparedStatement.setString(1, mainScenarioName);
				selectResultSet = selectPreparedStatement.executeQuery();

				while (selectResultSet.next()) {
					mainScenarioNameID = selectResultSet.getInt("idSWB_Main_Scenario");
				}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}

		if (mainScenarioNameID != 0) {
			mainScenarioExists = true;
		}

		return mainScenarioExists;
	}
	*/
	
	
	/*public int insertSubScenario(int mainScenarioNo, int subScenarioNo) {
		
		Connection swbReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");
		
		String insertQuery = "insert into swb_reports.swb_sub_scenario (SWB_Main_Scenario_idSWB_Main_Scenario,SWB_Sub_Scenariocol_Name) values (?,?)";
		String selectQuery = ("select idSWB_Sub_Scenario from swb_reports.swb_sub_scenario where SWB_Sub_Scenariocol_Name=?");
		

		PreparedStatement insertPreparedStatement = null;
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		boolean insertStatus = false;
		int i = 0;
		int subScenarioNameID = 0;
		try {

			insertPreparedStatement = swbReportsCon.prepareStatement(insertQuery);
			insertPreparedStatement.setInt(1, mainScenarioNo);
			insertPreparedStatement.setInt(2, subScenarioNo);

			i = insertPreparedStatement.executeUpdate();

			if (i > 0) {
				selectPreparedStatement = swbReportsCon.prepareStatement(selectQuery);
				selectPreparedStatement.setInt(1, subScenarioNo);
				selectResultSet = selectPreparedStatement.executeQuery();

				while (selectResultSet.next()) {
					subScenarioNameID = selectResultSet.getInt("idSWB_Sub_Scenario");
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, insertPreparedStatement, null);
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}
		return subScenarioNameID;
	}
	*/
	
	public boolean checkSubScenario(int subScenarioNo) {
		boolean subScenarioExists = false;
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");

		String selectQuery = ("select idSWB_Sub_Scenario from swb_reports.swb_sub_scenario where SWB_Sub_Scenariocol_Name=?");

		
		PreparedStatement selectPreparedStatement = null;
		ResultSet selectResultSet = null;

		int i = 0;
		int subScenarioNameID = 0;
		try {

			if (i > 0) {
				selectPreparedStatement = sebReportsCon.prepareStatement(selectQuery);
				selectPreparedStatement.setInt(1, subScenarioNo);
				selectResultSet = selectPreparedStatement.executeQuery();

				while (selectResultSet.next()) {
					subScenarioNameID = selectResultSet.getInt("idSWB_Sub_Scenario");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionUtil.closeSqlResourse(null, selectPreparedStatement, null);
		}

		if (subScenarioNameID != 0) {
			subScenarioExists = true;
		}
		return subScenarioExists;
	}
	
	
	public boolean updateValidationReportData(AssertionResultBean arb)
	{
		
		boolean validationRptDatUpdated = false;
		Connection sebReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");

		String insertQuery = "insert into `swb_reports`.`swb_validations`  " +
				"(validation_Element_ID,validation_Description,Expected_Value,Actual_Value,validation_Result,validation_time, " +
				"SWB_Function_idSWB_Function ) values (?,?,?,?,?,now(),?)";

		
		PreparedStatement insertPreparedStatement = null;
		
		int i=0;

		try {

			insertPreparedStatement = sebReportsCon.prepareStatement(insertQuery);
			insertPreparedStatement.setString(1, arb.getElementId());
			insertPreparedStatement.setString(2, arb.getAssertionDescription());
			insertPreparedStatement.setString(3, arb.getExpectedValue());
			insertPreparedStatement.setString(4, arb.getActualValue());
			insertPreparedStatement.setString(5, arb.getResult());
			insertPreparedStatement.setInt(6, reportSWBFunctionID);

			i = insertPreparedStatement.executeUpdate();

			if (i > 0) {
				validationRptDatUpdated=true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}catch (RuntimeException re) {
			re.printStackTrace();
			throw re;
		} 
		finally {
			ConnectionUtil.closeSqlResourse(null, insertPreparedStatement, null);
			
		}
		return validationRptDatUpdated;
		
	}
	
	//TODO convert this to jdbc
	public boolean updateElisionDetails(String exceptionMessage,String screenShotFileName)
	{
		/*Session session1=null;
		boolean updateStatus=false;
		try
		{
		SessionFactory sessionFactory=HibernateConnection.getInstance().getSessionFactory();
		session1= sessionFactory.openSession();
		
		session1.beginTransaction();
		
		Criteria maxValidationIDCriteria = session1.createCriteria(SwbValidations.class);
		maxValidationIDCriteria.setProjection(Projections.max("idSwbValidations"));
		
		List maxValidationIDLilst=maxValidationIDCriteria.list();
		
		if(maxValidationIDLilst != null)
		{
        Iterator maxValidationIDLilstIterator=maxValidationIDLilst.iterator();
 
        
        
        while(maxValidationIDLilstIterator.hasNext())
        {
        	reportSWBValidationsID=(Integer)maxValidationIDLilstIterator.next();
        }
        
        
        SwbValidationsHome swbValidationsHome=new SwbValidationsHome();
        
        SwbValidations swbValidations=swbValidationsHome.findById(reportSWBValidationsID,session1);
        swbValidations.setIdSwbValidations(reportSWBValidationsID);
        swbValidations.setExceptionMessage(exceptionMessage);
        swbValidations.setExceptionScreenShot(screenShotFileName);
		
        swbValidationsHome.attachDirty(swbValidations, session1);
        
		session1.getTransaction().commit();
		updateStatus=true;
		}
		}
		
		catch (RuntimeException re) {
			
			throw re;
		}
		finally{
			if(session1 != null)
			{
			session1.close();
			}
		}
		return updateStatus;*/
		
		
		boolean validationRptDatUpdated = false;
		Connection swbReportsCon = ConnectionUtil.getConnection("SWB_REPORTS");

		String updateQuery = "UPDATE swb_reports.swb_validations SET Exception_Message=? WHERE idSWB_validations=?";

		
		PreparedStatement updatePreparedStatement = null;
		
		int i=0;

		try {

			updatePreparedStatement = swbReportsCon.prepareStatement(updateQuery);
			updatePreparedStatement.setString(1, exceptionMessage);
			updatePreparedStatement.setInt(2, returnValidationID());
					i = updatePreparedStatement.executeUpdate();

			if (i > 0) {
				validationRptDatUpdated=true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}catch (RuntimeException re) {
			re.printStackTrace();
			//throw re;
		} 
		catch (Exception  e1) {
			e1.printStackTrace();
		} 
		finally {
			ConnectionUtil.closeSqlResourse(null, updatePreparedStatement, null);
			
		}
		return validationRptDatUpdated;
		
	}
	
	
	
	
	private Object mapBeanFromResultSet(ResultSet rs,String beanName){
		Class<?> c=getClassByClassName(beanPackage+beanName);
		Object ob = getinstanceByClass(c);
		ResultSetMetaData rsmt = null;
		int count=0;
		try {
			 rsmt=rs.getMetaData();
			 count = rsmt.getColumnCount();
			 String label=null;
			 Method m=null;
			 String columnVal=null;
			 for(int i=1;i<=count;i++){
				  label = rsmt.getColumnName(i);
				  columnVal=rs.getString(i);
				  if(columnVal!=null){
					  columnVal=columnVal.trim();
				  }
				 m=getMethodByName(c, "set"+label, String.class);
				 invokeMethod(m, ob, columnVal);
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ob;
		
	}
	/**
	 * This method sets the specified value in bean.
	 * @param obj object of bean typically generated with same name as table name
	 * @param name column name of table
	 * @param value to set
	 * */
	private void invokeSetter(Object obj,String name,String value){
		//String value=null;
		System.out.println("className of bean::"+obj.getClass().getName());
		try {
			obj.getClass().getMethod("set"+name, String.class).invoke(obj, value.trim());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
	}
	private String invokeGetter(Object obj,String name){
		String value=null;
		try {
			value = (String)obj.getClass().getMethod("get"+name, null).invoke(obj, null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(value!=null){
			value=value.trim();
		}
		return value;
	}
	
	private Method getMethodByName(Class<?> c,String methodName,Class<?> parameterType){
		Method m=null;
		try {
			m=c.getMethod(methodName, parameterType);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
	}
	
	private Object invokeMethod(Method m,Object ob,Object arg){
		
		Object ret = null;
		try {
			ret = m.invoke(ob, arg);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	private Class getClassByClassName(String className){
		Class<?> c=null;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}
	private Object getinstanceByClass(Class className){
		
		Object ob=null;
		//c = className.forName(className);
		 try {
			ob=className.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ob;
	}
	
/**
 * This method updates the object in database with the values modified using invokeSetter
 * @param ob object to update in database
 * @return true if successfully updated, false otherwise
 * */	
public boolean updateObject(Object ob){
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	String tableName = ob.getClass().getName();
	int index=tableName.lastIndexOf('.');
	tableName=tableName.substring(index+1);
	Connection con = ConnectionUtil.getConnection(dbSchemaName);
	Field fieldlist[] = ob.getClass().getDeclaredFields();
	String setString="";
	for (Field field : fieldlist) {
	String fieldVal="";
	String fieldName=null;
	try {
		fieldName = field.getName();
		fieldVal = (String)invokeGetter(ob, fieldName);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
	}
	setString = setString+fieldName+" = '"+fieldVal+"',\n ";
	}
	setString=setString.substring(0, setString.lastIndexOf(','));
		String updateQuery="UPDATE "+tableName+" SET "+setString+" WHERE SN="+invokeGetter(ob, "SN");
		
		int count=0;
		try {
			preparedStatement = con.prepareStatement(updateQuery);
			//preparedStatement.setString(1, fileName);
			
			 count = preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			System.out.println("update query::"+updateQuery);
			ConnectionUtil.closeSqlResourse(con, preparedStatement, rs);
		}
		if(count>0)
			return true;
		else
			return false;
	}

//Added by Dineshs
/**
 * This method fetches all the data from specified schemaName,table name and sub scenario number.
 * @param tableName 
 * @param schemaName
 * @param subScenario
 * @return List of object(corresponding generated bean) each object represents a table row
 * 
 * */
private ArrayList<Object> selectDataFromFile(String fileName, Connection con,String subScenario,String dataSetNo){
	
	 String selectQuery = "select * from ";
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	Object  bean=null;
	ArrayList<Object> list = new ArrayList<Object>();
	int count = 0;
	try {
		if(arrayList.contains(fileName)){
			System.out.println(fileName+"--> this");	
			preparedStatement = con.prepareStatement(selectQuery+fileName+" where SUB_SCENARIO='"+subScenario+"' and DATA_SET_NO='"+dataSetNo+"'");
			//preparedStatement.setString(1, fileName);
			rs = preparedStatement.executeQuery();
			while(rs.next()){
				bean = mapBeanFromResultSet(rs,fileName);
				list.add(bean);
				count++;
				}
		}
		/*if(!(fileName.equals("partyset") || fileName.equals("taborderhawb") || fileName.equals("nsibmaindtls") || 
			fileName.equals("nsibhawb") || fileName.equals("master") || fileName.equals("house") || 
			fileName.equals("splitdetails") || fileName.equals("chargecodeupdationtable") || fileName.equals("partyset") || fileName.equals("scenarioscontrol"))){
		preparedStatement = con.prepareStatement(selectQuery+fileName+" where SUB_SCENARIO='"+subScenario+"' and DATA_SET_NO='"+dataSetNo+"'");
		//preparedStatement.setString(1, fileName);
		rs = preparedStatement.executeQuery();
		while(rs.next()){
			bean = mapBeanFromResultSet(rs,fileName);
			list.add(bean);
			count++;
			}
		}*/
		if(count<=0){
			preparedStatement = con.prepareStatement(selectQuery+fileName);
			//preparedStatement.setString(1, fileName);
			rs = preparedStatement.executeQuery();
			while(rs.next()){
				bean = mapBeanFromResultSet(rs,fileName);
				list.add(bean);
				count++;
				}			
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}finally{
		ConnectionUtil.closeSqlResourse(null, preparedStatement, rs);
	}
	return list;
}
}
