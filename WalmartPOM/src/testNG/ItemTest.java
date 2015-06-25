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

public class ItemTest {
  private RemoteWebDriver driver;
  private ExcelDriver ed;
  private String testName;
  private String testCycle;
  private String deviceDesc;

 @Test (dataProvider = "itemsDP")
public void searchItemsTest(String itemSerial, String itemDescription, String itemPrice) throws Exception {
	boolean testFail = false;
	
	ed.setSheet(this.deviceDesc, true);
	ed.setTestCycle(this.testCycle, true);
    try{
    	
    	//this.driver.get("http://google.com");
    	SearchItemsPageView view = new WalmartBaseView(this.driver).init().searchItem(itemSerial);
    	 
        String actualPrice = view.getItemPriceByIndex(1);
        String actualDescription = view.getItemNameByIndex(1);
        
        if(!actualDescription.equals(itemDescription)){
        	testFail = true;
        	Reporter.log("Value is: " + actualDescription + ", Should be: " + itemDescription);
        	String errorFile = PerfectoUtils.takeScreenshot(driver);
    		Reporter.log("Error screenshot saved in file: " + errorFile);
        }
        if(!actualPrice.equals(itemPrice)){
        	testFail = true;
        	Reporter.log("Value is: " + actualPrice + ", Should be: " + itemPrice);
        	String errorFile = PerfectoUtils.takeScreenshot(driver);
        	Reporter.log("Error screenshot saved in file: " + errorFile);
        }
    }
    catch(Exception e){
    	ed.setResultByTestCycle(false, this.testName, itemSerial, itemDescription, itemPrice);
    	Assert.fail("See Reporter log for details");
    }
    
    if(testFail){
    	ed.setResultByTestCycle(false, this.testName, itemSerial, itemDescription, itemPrice);
    	Assert.fail("See reporter log for details");
    }
    else{
    	ed.setResultByTestCycle(true, this.testName, itemSerial, itemDescription, itemPrice);
    }
    //System.out.println(price);
    //Reporter.log(price);
      
}
 @DataProvider (name = "itemsDP")
 public Object[][] itemsDP() throws Exception{
	// Get Excel file path
		  String filePath = new File("").getAbsolutePath();
		  filePath += "\\data\\book1.xlsx";
		  
		  // Open workbook
		  this.ed = new ExcelDriver();
		  this.ed.setWorkbook(filePath);
		  
		  // Open sheet
		  this.ed.setSheet("items", false);
		  
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