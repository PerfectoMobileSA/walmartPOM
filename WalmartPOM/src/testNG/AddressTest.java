package testNG;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

import walmartPOM.*;
import utils.PerfectoUtils;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.DataProvider;

import com.perfectomobile.selenium.util.EclipseConnector;

import dataDrivers.excelDriver.*;

public class AddressTest {
  private RemoteWebDriver driver;
  private ExcelDriver ed;
  private String testName;
  private String testCycle;
  private String deviceDesc;

  @Test (dataProvider="searchStoresDP")
  public void searchStores(String searchedAddress, String strIdx, String storeAddress) throws Exception{	 
 	 boolean testFail = false;	
 	 ed.setSheet(this.deviceDesc, true);
 	 ed.setTestCycle(this.testCycle, true);
 	 int index = Integer.valueOf(strIdx);
 	 try{
 		WalmartBaseView view = new WalmartBaseView(driver).init();
 	 	 SearchStoresPageView storeView = view.clickLocateStores().searchAddress(searchedAddress);
 	 	 String actualAddress = storeView.getAddressByStoreIndex(index);
 	 	 
 	 	 if(!actualAddress.equals(storeAddress)){
 	      	testFail = true;
 	      	Reporter.log("Value is: " + actualAddress + ", Should be: " + storeAddress);
 	      	String errorFile = PerfectoUtils.takeScreenshot(driver);
 	  		Reporter.log("Error screenshot saved in file: " + errorFile);
 	      }
 	 }
 	 catch(Exception e){
       	ed.setResultByTestCycle(false, this.testName, searchedAddress, strIdx, storeAddress);
       	Assert.fail("See reporter log for details");
 	 }
 	 //this.driver.get("http://google.com");
 	 
 	 if(testFail){
      	ed.setResultByTestCycle(false, this.testName, searchedAddress, strIdx, storeAddress);
      	Assert.fail("See reporter log for details");
      }
      else{
      	ed.setResultByTestCycle(true, this.testName, searchedAddress, strIdx, storeAddress);
      }

  }
  
  @DataProvider (name="searchStoresDP")
  public Object[][] searchStoresDP() throws Exception{
 	// Get Excel file path
 	  String filePath = new File("").getAbsolutePath();
 	  filePath += "\\data\\book1.xlsx";
 	  
 	  // Open workbook
 	  this.ed = new ExcelDriver();
 	  this.ed.setWorkbook(filePath);
 	  
 	  // Open sheet
 	  this.ed.setSheet("addresses", false);
 	  
 	  // Read the sheet into 2 dim (String)Object array.
 	  // "3" is the number of columns to read.
 	  Object[][] s = ed.getData(3);

 	  return s;
  }
  @Parameters({"platform", "mcm", "mcmUser", "mcmPassword", "deviceModel",
		  "deviceDescription", "driverRetries", "testCycle"})
	@BeforeClass 
	public void beforeClass(String platform, String mcm, String mcmUser, String mcmPassword, String deviceModel,
						String deviceDescription, int driverRetries, String testCycle) throws Exception{
	
	  System.out.println("Run started");
	this.testCycle = testCycle;
	this.deviceDesc = deviceModel;
	
	if(platform.toLowerCase().equals("mobile")){
	String browserName = "mobileOS";
	DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "",Platform.ANY);

	capabilities.setCapability("model", deviceModel);
	capabilities.setCapability("description", deviceDescription);
	capabilities.setCapability("takeScreenshot", "false");
	
	// Uncomment this block if you want to use    
	// a device which is opened in the recorder window     
	/*
	try { 
	    EclipseConnector connector = new EclipseConnector();
	    String eclipseExecutionId = connector.getExecutionId();                     
	    capabilities.setCapability("eclipseExecutionId", eclipseExecutionId);                     }
	    catch (IOException ex){
	             ex.printStackTrace();
	    }
	*/
		this.driver = PerfectoUtils.getPerfectoDriver(mcmUser, mcmPassword, mcm, capabilities, 5, 20);
	}
	else if(platform.toLowerCase().equals("desktop")){
		DesiredCapabilities dc = DesiredCapabilities.chrome();
		this.driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
		this.deviceDesc = "Chrome";
	}
}
	
	@BeforeMethod
	public void beforeMethod(Method method){
	this.testName = method.getName();
	}
	
	@AfterClass
	public void afterClass() {
	try{
	if(this.driver == null){
		return;
	}
	this.ed.setAutoSize();
	// Close the browser
	driver.close();
	 
	/*
	// Download a pdf version of the execution report
	PerfectoUtils.downloadReport(driver, "pdf", "C:\\temp\\report.pdf");
	*/
	}
	catch(Exception e){
	    e.printStackTrace();
	}
	
	driver.quit();
	}
}
