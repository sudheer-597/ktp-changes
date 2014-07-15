package com.foursoft.selenium.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
/**
 * 
 * @author RameshButta
 * @version 1.0
 *
 */

public class CntrlFileGenerator {

	
	static Connection singleWorkBookConnection = SingleWorkBookDSNDataSource.getInstance().getConnection();
	
	
public static void generateCntrlFiles() {

	
		
		
		int srNo=0;
		//DatabaseMetaData singleWorkBookColumnNames = singleWorkBookConnection.getMetaData();
		
		Statement mainScenarioSTMT;
		ResultSet mainScenarioRS;
		List<String> mainScenarioList=new ArrayList<String>();
		
		
		Statement mainScenarioSubScenarioNoSTMT;
		ResultSet mainScenarioSubScenarioNoRS;
		List<Integer> mainScenariosSubScenarioList;
		Map<String,List> mainScenarioSubScenarioNoMap=new LinkedHashMap<String, List>();
		
		
		Statement mainsSubScenarioNoFunctionsSTMT;
		ResultSet mainsSubScenarioNoFunctionsRS;
		
		String mainsSubScenarioNoFunctionsString=null;
		List<String> mainsSubScenarioNoFunctionsList;
		Map<String,List> mainsSubScenarioNoFunctionsMap = new LinkedHashMap<String, List>();
		String prevMainsSubScenarioNoFunctionsString=null;
		String prePrevMainsSubScenarioNoFunctionsString=null;
		
		
		String mainsSubsfunctionsValidationString=null;
		List<String> mainsSubsfunctionsValidationList;
		Map<String,List> mainsSubsfunctionsValidationMap = new LinkedHashMap<String, List>();
		Statement mainsSubsfunctionsValidationSTMT;
		ResultSet mainsSubsfunctionsValidationRS;
		
		String mainsSubsfunctionsValidationRequiredString=null;
		Map<String,String> mainsSubsfunctionsValidationRequiredMap = new LinkedHashMap<String, String>(); 
		
		String mainsSubScenarioNoFunctionsRequiredString=null;
		//List<String> mainsSubScenarioNoFunctionsRequiredList=null;
		//Map<String,String> mainsSubScenarioNoFunctionsRequiredMap = new LinkedHashMap<String, String>();
		ListMultimap<String, String> mainsSubScenarioNoFunctionsRequiredMultiMap = ArrayListMultimap.create();

		 
		String mainsSubScenarioNoFunctionsModeString=null;
		Map<String,String> mainsSubScenarioNoFunctionsModeMap = new LinkedHashMap<String, String>();
		
		
		int mainsSubScenarioNoFunctionsNoOfRecordsString=0;
		Map<String,Integer> mainsSubScenarioNoFunctionsNoOfRecordsMap = new LinkedHashMap<String, Integer>();
		
		String mainsSubScenarioNoOriginLoginString=null;
		Map<String,String> mainsSubScenarioNoOriginLoginMap=new HashMap<String,String>();
		
		String mainsSubScenarioNoDestinationLoginString=null;
		Map<String,String> mainsSubScenarioNoDestinationLoginMap=new HashMap<String,String>();
		
		
		String mainsSubScenarioDataSetString=null;
		Map<String,String> mainsSubScenarioDataSetsMap=new HashMap<String,String>();
		
		String mainScenarioName;
		int subScenarioNum;
		
		
		try {
			mainScenarioSTMT = singleWorkBookConnection.createStatement();

			mainScenarioRS = mainScenarioSTMT.executeQuery("select distinct MAIN_SCENARIO from [ControlSheet$] where MAIN_SCENARIO_REQUIRED='YES'");
			
			
			int i=1;
			
			while (mainScenarioRS.next()) {
				
				mainScenarioName=mainScenarioRS.getString("MAIN_SCENARIO");
				if(mainScenarioName != null && !(StringUtils.isBlank(mainScenarioName)))
				{
					mainScenarioList.add(mainScenarioName);
					mainScenarioSubScenarioNoSTMT = singleWorkBookConnection.createStatement();
					mainScenarioSubScenarioNoRS = mainScenarioSubScenarioNoSTMT.executeQuery("select distinct SUB_SCENARIO_NO from [ControlSheet$] where SUB_SCENARIO_REQUIRED='YES' and MAIN_SCENARIO='"+mainScenarioName+"'");
					
					mainScenariosSubScenarioList=new ArrayList<Integer>();
					int functionSwitchCounter=1;
					
					while(mainScenarioSubScenarioNoRS.next())
					{
						subScenarioNum=mainScenarioSubScenarioNoRS.getInt("SUB_SCENARIO_NO");
						if(subScenarioNum != 0)
						{
							mainScenariosSubScenarioList.add(subScenarioNum);
							
							 
							mainsSubScenarioNoFunctionsSTMT=singleWorkBookConnection.createStatement();
							mainsSubScenarioNoFunctionsRS=mainsSubScenarioNoFunctionsSTMT.executeQuery("select Sr_No,DATA_SETS,ORIGIN_LOGIN_TERMINAL,DESTINATION_LOGIN_TERMINAL,MODE_FIELD,FUNCTIONS_FIELD,FUNCTIONS_FIELD_REQUIRED,VALIDATIONS,VALIDATIONS_REQUIRED,NO_OF_RECORDS from [ControlSheet$] where MAIN_SCENARIO='"+(mainScenarioName)+"' and SUB_SCENARIO_NO="+subScenarioNum+"");
							
							mainsSubScenarioNoFunctionsList=new ArrayList<String>();
							mainsSubsfunctionsValidationList=new ArrayList<String>();
							
							while(mainsSubScenarioNoFunctionsRS.next())
							{
								srNo=mainsSubScenarioNoFunctionsRS.getInt("Sr_No");
								
								mainsSubScenarioNoFunctionsString=mainsSubScenarioNoFunctionsRS.getString("FUNCTIONS_FIELD");
								
								mainsSubsfunctionsValidationList=new ArrayList<String>();
								
								
								//this would get invoked multiple times for a function to capture the validation
								if((prevMainsSubScenarioNoFunctionsString != null) && prevMainsSubScenarioNoFunctionsString.equalsIgnoreCase(mainsSubScenarioNoFunctionsString))
								{
									mainsSubsfunctionsValidationSTMT=singleWorkBookConnection.createStatement();
									mainsSubsfunctionsValidationRS=mainsSubsfunctionsValidationSTMT.executeQuery("select VALIDATIONS,VALIDATIONS_REQUIRED  from [ControlSheet$] where MAIN_SCENARIO='"+mainScenarioName+"' and SUB_SCENARIO_NO="+subScenarioNum+" and ORIGIN_LOGIN_TERMINAL='"+mainsSubScenarioNoOriginLoginString+"' and DESTINATION_LOGIN_TERMINAL='"+mainsSubScenarioNoDestinationLoginString+"' and MODE_FIELD='"+mainsSubScenarioNoFunctionsModeString+"' and FUNCTIONS_FIELD='"+mainsSubScenarioNoFunctionsString+"' and FUNCTIONS_FIELD_REQUIRED='YES' and NO_OF_RECORDS="+mainsSubScenarioNoFunctionsNoOfRecordsString+ "and DATA_SETS='"+mainsSubScenarioDataSetString+"'");
							
									
									while(mainsSubsfunctionsValidationRS.next())
									{
			
										mainsSubsfunctionsValidationString=mainsSubsfunctionsValidationRS.getString("VALIDATIONS");
										
										mainsSubsfunctionsValidationList.add(mainsSubsfunctionsValidationString);
										
										mainsSubsfunctionsValidationRequiredString=mainsSubsfunctionsValidationRS.getString("VALIDATIONS_REQUIRED");
										if(mainsSubsfunctionsValidationRequiredString != null && !(StringUtils.isBlank(mainsSubsfunctionsValidationRequiredString)))
										{
											mainsSubsfunctionsValidationRequiredMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString+"_"+mainsSubsfunctionsValidationString,mainsSubsfunctionsValidationRequiredString);
										}
									}
									mainsSubsfunctionsValidationMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString, mainsSubsfunctionsValidationList);
								}
								
								
								//This needs to get invoked only once per function
								
								if(!(mainsSubScenarioNoFunctionsString.equalsIgnoreCase(prevMainsSubScenarioNoFunctionsString)))
								{
									mainsSubsfunctionsValidationList=new ArrayList<String>();
									mainsSubsfunctionsValidationString=mainsSubScenarioNoFunctionsRS.getString("VALIDATIONS");
									if(mainsSubsfunctionsValidationString != null)
									mainsSubsfunctionsValidationList.add(mainsSubsfunctionsValidationString);
									mainsSubsfunctionsValidationMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString, mainsSubsfunctionsValidationList);
									
									mainsSubsfunctionsValidationRequiredString=mainsSubScenarioNoFunctionsRS.getString("VALIDATIONS_REQUIRED");
									if(mainsSubsfunctionsValidationRequiredString != null && !(StringUtils.isBlank(mainsSubsfunctionsValidationRequiredString)))
									{
										mainsSubsfunctionsValidationRequiredMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString+"_"+mainsSubsfunctionsValidationString,mainsSubsfunctionsValidationRequiredString);
									}
									
									
									/*mainsSubsfunctionsValidationMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+prevMainsSubScenarioNoFunctionsString, mainsSubsfunctionsValidationList);
									prePrevMainsSubScenarioNoFunctionsString=prevMainsSubScenarioNoFunctionsString;
									prevMainsSubScenarioNoFunctionsString=mainsSubScenarioNoFunctionsString;
									mainsSubsfunctionsValidationList=new ArrayList<String>();*/
								
									
									
								if(mainsSubScenarioNoFunctionsString != null && !(StringUtils.isBlank(mainsSubScenarioNoFunctionsString)))
								{
									mainsSubScenarioNoFunctionsList.add(mainsSubScenarioNoFunctionsString);
								}
								
								
								mainsSubScenarioNoFunctionsRequiredString=mainsSubScenarioNoFunctionsRS.getString("FUNCTIONS_FIELD_REQUIRED");
								if(mainsSubScenarioNoFunctionsRequiredString != null && !(StringUtils.isBlank(mainsSubScenarioNoFunctionsRequiredString)))
								{
									mainsSubScenarioNoFunctionsRequiredMultiMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString, mainsSubScenarioNoFunctionsRequiredString);
								}
								
								mainsSubScenarioNoFunctionsModeString=mainsSubScenarioNoFunctionsRS.getString("MODE_FIELD");
								
								if(mainsSubScenarioNoFunctionsModeString != null && !(StringUtils.isBlank(mainsSubScenarioNoFunctionsModeString)))
								{
									mainsSubScenarioNoFunctionsModeMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString, mainsSubScenarioNoFunctionsModeString);
								}
								
								
								mainsSubScenarioNoFunctionsNoOfRecordsString=mainsSubScenarioNoFunctionsRS.getInt("NO_OF_RECORDS");
								
								if(mainsSubScenarioNoFunctionsNoOfRecordsString != 0)
								{
									mainsSubScenarioNoFunctionsNoOfRecordsMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString, mainsSubScenarioNoFunctionsNoOfRecordsString);
								}
								mainsSubScenarioDataSetString=mainsSubScenarioNoFunctionsRS.getString("DATA_SETS");
								mainsSubScenarioNoOriginLoginString=mainsSubScenarioNoFunctionsRS.getString("ORIGIN_LOGIN_TERMINAL");
								mainsSubScenarioNoDestinationLoginString=mainsSubScenarioNoFunctionsRS.getString("DESTINATION_LOGIN_TERMINAL");
								}
								//This needs to get invoked as per the number of validations
								
/*								mainsSubsfunctionsValidationString=mainsSubScenarioNoFunctionsRS.getString("VALIDATIONS");
								if(mainsSubsfunctionsValidationString != null && !(StringUtils.isBlank(mainsSubsfunctionsValidationString)))
								{
									mainsSubsfunctionsValidationList.add(mainsSubsfunctionsValidationString);
								}*/
								/*mainsSubsfunctionsValidationRequiredString=mainsSubScenarioNoFunctionsRS.getString("VALIDATIONS_REQUIRED");
								if(mainsSubsfunctionsValidationRequiredString != null && !(StringUtils.isBlank(mainsSubsfunctionsValidationRequiredString)))
								{
									mainsSubsfunctionsValidationRequiredMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString+"_"+mainsSubsfunctionsValidationString,mainsSubsfunctionsValidationRequiredString);
								}*/
								
									
								prevMainsSubScenarioNoFunctionsString=mainsSubScenarioNoFunctionsString;	
																
							}
							//iterating the functions is done
							mainsSubScenarioNoFunctionsMap.put(mainScenarioName+"_SubScenario"+subScenarioNum, mainsSubScenarioNoFunctionsList);
							
							//mainsSubsfunctionsValidationMap.put(key, value)
							
							if(mainsSubScenarioDataSetString != null && !(StringUtils.isBlank(mainsSubScenarioDataSetString)))
							{
								mainsSubScenarioDataSetsMap.put(mainScenarioName+"_SubScenario"+subScenarioNum, mainsSubScenarioDataSetString);
							}
							
							
							if(mainsSubScenarioNoOriginLoginString != null && !(StringUtils.isBlank(mainsSubScenarioNoOriginLoginString)))
							{
								mainsSubScenarioNoOriginLoginMap.put(mainScenarioName+"_SubScenario"+subScenarioNum, mainsSubScenarioNoOriginLoginString);
							}
							if(mainsSubScenarioNoDestinationLoginString != null && !(StringUtils.isBlank(mainsSubScenarioNoDestinationLoginString)))
							{
								mainsSubScenarioNoDestinationLoginMap.put(mainScenarioName+"_SubScenario"+subScenarioNum, mainsSubScenarioNoDestinationLoginString);
							}
							if(mainsSubScenarioNoDestinationLoginString != null && !(StringUtils.isBlank(mainsSubScenarioNoDestinationLoginString)))
							{
								mainsSubScenarioNoDestinationLoginMap.put(mainScenarioName+"_SubScenario"+subScenarioNum, mainsSubScenarioNoDestinationLoginString);
							}
							
							
						}
						
						
					}
					//iterating the scenarios is done
					mainScenarioSubScenarioNoMap.put(mainScenarioName, mainScenariosSubScenarioList);
					
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//done parsing the excel sheet
		
		createCntrlSyncnXmlFile(mainScenarioList,mainScenarioSubScenarioNoMap,mainsSubScenarioDataSetsMap,
				mainsSubScenarioNoFunctionsMap,mainsSubsfunctionsValidationMap,mainsSubsfunctionsValidationRequiredMap,mainsSubScenarioNoFunctionsRequiredMultiMap,mainsSubScenarioNoFunctionsModeMap,mainsSubScenarioNoFunctionsNoOfRecordsMap,mainsSubScenarioNoOriginLoginMap,mainsSubScenarioNoDestinationLoginMap);
}


private static BufferedWriter singleWorkBookSyncFileBufferedWriter=null;
public static Map<String,String> singleWorkBookPropertiesMap=getControlFileAttributes();


private static com.google.common.base.Function<String, Integer> string2Integer = 
    new com.google.common.base.Function<String,Integer>() { 
		@Override
        public Integer apply(String stringNumber) { return  Integer.parseInt(stringNumber);}};
    



public static void createCntrlSyncnXmlFile(List<String> mainScenarioList,Map<String,List> mainScenarioSubScenarioNoMap,Map<String,String> mainsSubScenarioDataSetsMap,
		Map<String,List> mainsSubScenarioNoFunctionsMap,Map<String,List> mainsSubsfunctionsValidationMap,Map<String,String> mainsSubsfunctionsValidationRequiredMap,ListMultimap<String,String> mainsSubScenarioNoFunctionsRequiredMultiMap,Map<String,String> mainsSubScenarioNoFunctionsModeMap,Map<String,Integer> mainsSubScenarioNoFunctionsNoOfRecordsMap,Map<String,String> mainsSubScenarioNoOriginLoginMap,Map<String,String> mainsSubScenarioNoDestinationLoginMap) {
	
	try {
	
		initializeSingleWorkBook();
		
		//singleWorkBookPropertiesMap=getControlFileAttributes();
		
		File cntrlFile=null;
		String cntrlFileName=null;
		BufferedWriter cntrlFileBufferedWriter=null;
		int cntrlFileCounter=1;
		boolean cntrlFileCreated=false;
		
		int excludedLinesCounter=0;
		
		List<Integer> mainScenarioSubScenarioNoList=new ArrayList<Integer>();
		List<String> mainScenarioSubScenarioNoFunctionsList=new ArrayList<String>();
		List<String> mainsSubScenarioNoFunctionsRequiredMultiMapsList=null;
		List<String> mainScenarioSubScenarioNoFunctionsValidationList=new ArrayList<String>();
		
		//List<Integer> dataSetNos= Lists.transform(Arrays.asList(StringUtils.split(, ',')), .string2Integer);
		String mainsSubScenarioDataSetsString=null;
		List<Integer> mainsSubScenarioDataSetsList=new ArrayList<Integer>();

		
			for(Map.Entry<String, List> mainScenarioSubScenarioNo : mainScenarioSubScenarioNoMap.entrySet())
			{
				mainScenarioSubScenarioNoList=mainScenarioSubScenarioNo.getValue();
				
				for(Integer mainScenarioSubScenarioString:mainScenarioSubScenarioNoList)
				{
					
					String mainScenarioName=mainScenarioSubScenarioNo.getKey();
					mainsSubScenarioDataSetsList=new ArrayList<Integer>();
					
					if(mainsSubScenarioDataSetsMap.containsKey(mainScenarioName+"_SubScenario"+Integer.toString(mainScenarioSubScenarioString)))
					{
					mainsSubScenarioDataSetsString=mainsSubScenarioDataSetsMap.get(mainScenarioName+"_SubScenario"+Integer.toString(mainScenarioSubScenarioString));
					
					if(StringUtils.contains(mainsSubScenarioDataSetsString, '-'))
					{
						String [] dataSetsRangeString=StringUtils.split(mainsSubScenarioDataSetsString, '-');
						int dataSetStartRange=Integer.parseInt(dataSetsRangeString[0]);
						int dataSetEndRange=Integer.parseInt(dataSetsRangeString[1]);
						for(int i=dataSetStartRange;i<=dataSetEndRange;i++)
						{
							mainsSubScenarioDataSetsList.add(i);
						}
					
					}
					
					else
					{
					mainsSubScenarioDataSetsList=Lists.transform(Arrays.asList(StringUtils.split(mainsSubScenarioDataSetsString, ',')), string2Integer);
					}
					}
					else
					{
						mainsSubScenarioDataSetsList.add(0);
					}
					
					
					for(Integer mainsSubScenarioDataSetsListInteger:mainsSubScenarioDataSetsList)
					{
					
					cntrlFileName=mainScenarioSubScenarioNo.getKey()+"_"+mainScenarioSubScenarioString+"_"+Integer.toString(mainsSubScenarioDataSetsListInteger)+"-cntrl"+ cntrlFileCounter++ +".xml";
					
					
					//code to update the sync file would go in here
					
					
					try {
						DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder xmlDocBuilder = xmlDocFactory.newDocumentBuilder();
						Document xmlDocument = xmlDocBuilder.newDocument();
						
						
						Element mainScenarioElement = xmlDocument.createElement("MAIN_SCENARIO");
						xmlDocument.appendChild(mainScenarioElement);
						mainScenarioElement.setAttribute("name",mainScenarioName);
						
						Element subScenarioNoElement = xmlDocument.createElement("SUB_SCENARIO_NO");
						mainScenarioElement.appendChild(subScenarioNoElement);
						subScenarioNoElement.setAttribute("value",Integer.toString(mainScenarioSubScenarioString));
						
						Element originLoginTerminalElement = xmlDocument.createElement("ORIGIN_LOGIN_TERMINAL");
						originLoginTerminalElement.setAttribute("value",mainsSubScenarioNoOriginLoginMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString));
						subScenarioNoElement.appendChild(originLoginTerminalElement);
						
						Element dataSetsElement = xmlDocument.createElement("DATA_SETS");
						dataSetsElement.setAttribute("value",Integer.toString(mainsSubScenarioDataSetsListInteger));
						subScenarioNoElement.appendChild(dataSetsElement);
						
						
						Element destinationLoginTerminalElement = xmlDocument.createElement("DESTINATION_LOGIN_TERMINAL");
						destinationLoginTerminalElement.setAttribute("value",mainsSubScenarioNoDestinationLoginMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString));
						subScenarioNoElement.appendChild(destinationLoginTerminalElement);
						
						
						
						
							mainScenarioSubScenarioNoFunctionsList=mainsSubScenarioNoFunctionsMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString);
							for(String mainScenarioNoFunctionString:mainScenarioSubScenarioNoFunctionsList)
							{
								
								Element mainScenarioNoFunctionElement = xmlDocument.createElement("FUNCTIONS_FIELD");
								mainScenarioNoFunctionElement.setAttribute("name",mainScenarioNoFunctionString);
								
								mainsSubScenarioNoFunctionsRequiredMultiMapsList= mainsSubScenarioNoFunctionsRequiredMultiMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString+"_"+mainScenarioNoFunctionString); 
								mainScenarioNoFunctionElement.setAttribute("required",mainsSubScenarioNoFunctionsRequiredMultiMapsList.get(0));
								//mainsSubScenarioNoFunctionsRequiredMultiMap.remove(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString+"_"+mainScenarioNoFunctionString, mainsSubScenarioNoFunctionsRequiredMultiMapsList.get(0));
								subScenarioNoElement.appendChild(mainScenarioNoFunctionElement);
							
								Element mainScenarioNoFunctionModeElement = xmlDocument.createElement("MODE_FIELD");
								mainScenarioNoFunctionModeElement.setAttribute("value",mainsSubScenarioNoFunctionsModeMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString+"_"+mainScenarioNoFunctionString));
								mainScenarioNoFunctionElement.appendChild(mainScenarioNoFunctionModeElement);
						
								Element mainScenarioNoFunctionNoOfRecordsElement = xmlDocument.createElement("NO_OF_RECORDS");
								mainScenarioNoFunctionNoOfRecordsElement.setAttribute("value",Integer.toString(mainsSubScenarioNoFunctionsNoOfRecordsMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString+"_"+mainScenarioNoFunctionString)));
								mainScenarioNoFunctionElement.appendChild(mainScenarioNoFunctionNoOfRecordsElement);
						
								//mainsSubsfunctionsValidationMap.put(mainScenarioName+"_SubScenario"+subScenarioNum+"_"+mainsSubScenarioNoFunctionsString, mainsSubsfunctionsValidationList);
								mainScenarioSubScenarioNoFunctionsValidationList=mainsSubsfunctionsValidationMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString+"_"+mainScenarioNoFunctionString);
								
								if(mainScenarioSubScenarioNoFunctionsValidationList != null && !mainScenarioSubScenarioNoFunctionsValidationList.isEmpty())
								{
								for(String mainsSubsfunctionsValidationString:mainScenarioSubScenarioNoFunctionsValidationList)
								{
									Element mainsSubsfunctionsValidationElement = xmlDocument.createElement("VALIDATIONS");
									mainsSubsfunctionsValidationElement.setAttribute("value",mainsSubsfunctionsValidationString);
									mainsSubsfunctionsValidationElement.setAttribute("required", mainsSubsfunctionsValidationRequiredMap.get(mainScenarioName+"_SubScenario"+mainScenarioSubScenarioString+"_"+mainScenarioNoFunctionString+"_"+mainsSubsfunctionsValidationString));
									mainScenarioNoFunctionElement.appendChild(mainsSubsfunctionsValidationElement);
								}
								}
								
								
							}
						
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(xmlDocument);
						StreamResult result = new StreamResult(new File("CntrlFiles\\"+cntrlFileName));
						transformer.transform(source, result);
						cntrlFileCreated=true;
						//E:\desktop\cloverWorkpace2\FTN_Customer_RegressionScenarios_2
						}
						catch (ParserConfigurationException pce) {
							pce.printStackTrace();
						  } 
						catch (TransformerException tfe) {
							tfe.printStackTrace();
						  }
						catch (Exception e)
						{
							e.printStackTrace();
						}
						
					if (updateXML(mainScenarioName,mainScenarioSubScenarioString,mainsSubScenarioDataSetsListInteger) && updateFileNameString(mainScenarioName)) {

						if (cntrlFileCreated) {
							try {
								//only if all the above operations are sucess then only an entry has to be made to the below file
								singleWorkBookSyncFileBufferedWriter.write(cntrlFileName+ ",\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					}
					
				}
			}
			
			//updating filename param in the swb
			//updateSWBFileNameParam();
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	
	
	try {
		singleWorkBookSyncFileBufferedWriter.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
				



private static boolean generateXML4CodeGeneration()
{
	
	Statement mainsScenarioSTMT;
	ResultSet mainScenarioRS;
	Map<String,Integer> mainScenarioMap=new LinkedHashMap<String,Integer>();
	
	Map<String,List<String>> mainScenarioFunctionsMap=new LinkedHashMap<String,List<String>>();
	
	//to hold the map that contains the scenarios and its related classmethods
	PreparedStatement mainScenarioFunctionsPPDSTMT;
	ResultSet mainScenarioFunctionsRS;
	
	Statement mainScenarioFunctionsLastRowsSTMT;
	ResultSet mainScenarioFunctionsLastRowsRS;
	
	try {
		
		mainsScenarioSTMT = singleWorkBookConnection.createStatement();
		mainScenarioRS = mainsScenarioSTMT.executeQuery("select  Sr_No,MAIN_SCENARIO from [ScenarioMethodPool$] ");
		
		int srNo=0;
		String mainScenarioName;
		
			while (mainScenarioRS.next()) {
				srNo = mainScenarioRS.getInt("Sr_No");
				mainScenarioName = mainScenarioRS.getString("MAIN_SCENARIO");

				if (mainScenarioName != null && !(StringUtils.isBlank(mainScenarioName)) && srNo != 0) {
					mainScenarioMap.put(mainScenarioName, Integer.valueOf(srNo));
				}
			}
		
			
			
			String prevScenarioName=null;
			String currentScenarioName=null;
			String clzzFunctionString=null;
			
			
			List<String> clzzFunctionsList=null;
			
			Integer prevSrNo=0;
			Integer CurrentSrNo=0;
			String fetchClzzMethodQueryString="select  FUNCTIONS_FIELD  from [ScenarioMethodPool$] where " +
					"Sr_No  BETWEEN ? AND ?" ;
			
			for(Entry <String,Integer> mainScenarioDtls : mainScenarioMap.entrySet())
			{
				CurrentSrNo=mainScenarioDtls.getValue();
				currentScenarioName=mainScenarioDtls.getKey();
				
				clzzFunctionsList=new LinkedList<String>();
				
				if(prevSrNo != 0 && CurrentSrNo != 0)
				{
					mainScenarioFunctionsPPDSTMT = singleWorkBookConnection.prepareStatement(fetchClzzMethodQueryString);
					mainScenarioFunctionsPPDSTMT.setInt(1,prevSrNo);
					mainScenarioFunctionsPPDSTMT.setInt(2,(CurrentSrNo-1));
					
					mainScenarioFunctionsRS = mainScenarioFunctionsPPDSTMT.executeQuery();
					
					while (mainScenarioFunctionsRS.next()) {
						clzzFunctionString = mainScenarioFunctionsRS.getString("FUNCTIONS_FIELD");

						if (clzzFunctionString != null && !(StringUtils.isBlank(clzzFunctionString))) {
							clzzFunctionsList.add(clzzFunctionString);
						}
					}
				}
				mainScenarioFunctionsMap.put(prevScenarioName, clzzFunctionsList);
				prevScenarioName=currentScenarioName;
				prevSrNo=CurrentSrNo;
				
			}
			
			
			//for the last rows
			
			String fetchLastRowsClzzMethodQueryString="select  FUNCTIONS_FIELD  from [ScenarioMethodPool$] where " +
			"Sr_No  BETWEEN "+prevSrNo+" AND (SELECT LAST(Sr_No) FROM [ScenarioMethodPool$] ) ";
	
			clzzFunctionsList=new LinkedList<String>();
			mainScenarioFunctionsLastRowsSTMT = singleWorkBookConnection.createStatement();
			mainScenarioFunctionsLastRowsRS = mainScenarioFunctionsLastRowsSTMT.executeQuery(fetchLastRowsClzzMethodQueryString);
			
			while (mainScenarioFunctionsLastRowsRS.next()) {
				clzzFunctionString = mainScenarioFunctionsLastRowsRS.getString("FUNCTIONS_FIELD");

				if (clzzFunctionString != null && !(StringUtils.isBlank(clzzFunctionString))) {
					clzzFunctionsList.add(clzzFunctionString);
				}
			}
			mainScenarioFunctionsMap.put(prevScenarioName, clzzFunctionsList);
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	
	
	
	boolean auotGenXMlCreated=false;
	try
	{

		DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlDocBuilder = xmlDocFactory.newDocumentBuilder();
		Document xmlDocument = xmlDocBuilder.newDocument();
		
		
		Element mainScenarioFunctionsElement = xmlDocument.createElement("MAIN_SCENARIO_FUNCTIONS");
		xmlDocument.appendChild(mainScenarioFunctionsElement);
		
		
		List<String> mainScenarioFunctionsList=new LinkedList<String>();
		String mainScenarioNameString=null;
		
		
		for(Entry<String, List<String>> mainScenarioFunctionsDts : mainScenarioFunctionsMap.entrySet())
		{
			mainScenarioFunctionsList=mainScenarioFunctionsDts.getValue();
			mainScenarioNameString=mainScenarioFunctionsDts.getKey();
			
			Element mainScenarioElement = xmlDocument.createElement("MAIN_SCENARIO");
			mainScenarioFunctionsElement.appendChild(mainScenarioElement);
			mainScenarioElement.setAttribute("name",mainScenarioNameString);
			
			
			
			for(String mainScenarioFunctionDtsString:mainScenarioFunctionsList)
			{
				Element functionDtsElement = xmlDocument.createElement("FUNCTIONS_FIELD");
				mainScenarioElement.appendChild(functionDtsElement);
				functionDtsElement.setAttribute("name",mainScenarioFunctionDtsString);
			}
		}
		
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(xmlDocument);
		StreamResult result = new StreamResult(new File("CntrlFiles\\autoGen.xml"));
		transformer.transform(source, result);
		auotGenXMlCreated=true;
		//E:\desktop\cloverWorkpace2\FTN_Customer_RegressionScenarios_2
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		  } 
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		  }
		catch (Exception e)
		{
			e.printStackTrace();
		}	
	
	return auotGenXMlCreated;
}



	

public static void main(String [] args)
{
	//excludedCounterSet();
	
	
	generateCntrlFiles();
	try
	{
	generateXML4CodeGeneration();
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	
}

			
private static Set<String> fileNameSet=new HashSet<String>();

	private static boolean updateFileNameString(String mainScenarioName)
	{
		boolean fileNameSetUpdated=false;
		try
		{
		if(singleWorkBookPropertiesMap.containsKey(mainScenarioName+"_fileName"))
		{
		
			//fileNameSet=new HashSet<String>();
		    List<String> fileNamesElements = Arrays.asList(singleWorkBookPropertiesMap.get(mainScenarioName+"_fileName").split(","));
		    
		    for(String fileNameSingleElement:fileNamesElements)
		    {
		    	if(StringUtils.isNotBlank(fileNameSingleElement) &&	!fileNameSet.contains(fileNameSingleElement))
		    	{
		    		fileNameSet.add(fileNameSingleElement);
		    	}
		    }
		}
		fileNameSetUpdated=true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return fileNameSetUpdated;
	}
		
	

public static boolean updateXML(String mainScenarioName,int subScenarioNum,int dataSetNo) {
	boolean xmlUpdated=false;
	try {
		String filepath = "CntrlFiles\\SWB.xml";
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepath);

		Node rootElementSuite = doc.getFirstChild();
		
		//creating the test tag
		Element testElement = doc.createElement("test");
		testElement.setAttribute("name", mainScenarioName+"_SubScenario"+subScenarioNum+"_DataSet"+dataSetNo);
		
		//creating the classes tag
		Element classesElement = doc.createElement("classes");
		
		//creating the class tag
		//TODO replace the class element with proper class name
		Element classElement = doc.createElement("class");
		
		//SeleniumWorkBookConstants.SWClassMapperConstants.
		
		classElement.setAttribute("name",singleWorkBookPropertiesMap.get(mainScenarioName+"_class"));
		
		classesElement.appendChild(classElement);
		
		//create methods tag
		Element methodsElement = doc.createElement("methods");
		
		//create includes tag
		Element includeElement = doc.createElement("include");
		includeElement.setAttribute("name", singleWorkBookPropertiesMap.get(mainScenarioName+"_method"));
		methodsElement.appendChild(includeElement);
		classesElement.appendChild(methodsElement);
		testElement.appendChild(classesElement);
		
		rootElementSuite.appendChild(testElement);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filepath));
		transformer.transform(source, result);
		xmlUpdated=true;
		
		
	} catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	} catch (TransformerException tfe) {
		tfe.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	return xmlUpdated;
}


private static boolean updateSWBFileNameParam()
{
	boolean paramUpdated=false;
	
	try
	{
	String filepath = "CntrlFiles\\SWB.xml";
	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	Document doc = docBuilder.parse(filepath);
	
	Node parameterNode = doc.getElementsByTagName("parameter").item(4);
	NamedNodeMap parameterNodeMap = parameterNode.getAttributes();
	Node parameterNodeMapAttr = parameterNodeMap.getNamedItem("value");
	
	String fileNameString="";
	
	for(String fileName:fileNameSet)
	{
		if(StringUtils.isNotEmpty(fileNameString) && StringUtils.isNotEmpty(fileName))
		{
		fileNameString=fileNameString+","+fileName;
		}
		else if(StringUtils.isEmpty(fileNameString) && StringUtils.isNotEmpty(fileName))
		{
			fileNameString=fileName;
		}
	}
	
	parameterNodeMapAttr.setTextContent(fileNameString);
	
	
	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();
	DOMSource source = new DOMSource(doc);
	StreamResult result = new StreamResult(new File(filepath));
	transformer.transform(source, result);
	paramUpdated=true;
	}
 catch (ParserConfigurationException pce) {
	pce.printStackTrace();
} catch (TransformerException tfe) {
	tfe.printStackTrace();
} catch (Exception e) {
	e.printStackTrace();
}
return paramUpdated;
}
	

public static Map<String,String> getControlFileAttributes()
{
Properties singleWorkBookProperties = new Properties();
FileInputStream singleWorkBookPropertiesFile;
HashMap<String, String> singleWorkBookPropertiesMap=null;
try {
	singleWorkBookPropertiesFile = new FileInputStream("SingleWorkBook.properties");

	singleWorkBookProperties.load(singleWorkBookPropertiesFile);

	singleWorkBookPropertiesFile.close();

	singleWorkBookPropertiesMap= new HashMap<String, String>((Map) singleWorkBookProperties);
	
}
catch (FileNotFoundException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
catch (IOException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
return singleWorkBookPropertiesMap;
}


public static void initializeSingleWorkBook()
{
	 
	
	String swb_accesslevel = PropertiesUtil.readProperty("build.properties","swb_accesslevel");
	String browserName = PropertiesUtil.readProperty("build.properties","browserName");
	
	 File singleWorkBookInitialXMLFile =new File("CntrlFiles\\SWB.xml");
	 StringBuffer initialXMLString =new StringBuffer();
	 initialXMLString.append("<suite name=\"singleWorkBook\" verbose=\"1\" preserve-order=\"true\">");
	 initialXMLString.append("<parameter name=\"browserName\"  value=\""+browserName+"\"/>");
	 initialXMLString.append("<parameter  name=\"enableScreenShotOnException\" value=\"no\"/>");
	 initialXMLString.append("<parameter name=\"loginRequired\" value=\"yes\"/>");
	 initialXMLString.append("<parameter name=\"accessLevel\" value=\""+swb_accesslevel+"\"/>");
	 initialXMLString.append("<parameter name=\"fileName\" value=\"\"/>");
	 initialXMLString.append("<parameter name=\"logoutRequired\" value=\"yes\"/></suite>");
	 
	File singleWorkBookSyncfile = new File("CntrlFiles\\.singleworkbooksync");
	try {
		//singleWorkBookSyncfileReader = new BufferedReader(new InputStreamReader(new FileInputStream(singleWorkBookSyncfile)));
		
		FileWriter singleWorkBookSyncFileWriter = new FileWriter(singleWorkBookSyncfile.getAbsoluteFile());
		singleWorkBookSyncFileBufferedWriter = new BufferedWriter(singleWorkBookSyncFileWriter);
		
		FileWriter singleWorkBookInitialXMLFileWriter = new FileWriter(singleWorkBookInitialXMLFile.getAbsoluteFile());
		BufferedWriter singleWorkBookInitialXMLFileBufferedWriter =new BufferedWriter(singleWorkBookInitialXMLFileWriter);
		singleWorkBookInitialXMLFileBufferedWriter.write(initialXMLString.toString());
		singleWorkBookInitialXMLFileBufferedWriter.close();
		
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

}
	

