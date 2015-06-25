import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;






import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import com.perfectomobile.selenium.util.EclipseConnector;

import walmartPOM.*;


public class MobileRemoteTest {

	public static void main(String[] args) throws IOException {

		System.out.println("Run startedd");
		String browserName = "mobileOS";
		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		String host = args[0];
		String user = URLEncoder.encode(args[1], "UTF-8");
		String password = URLEncoder.encode(args[2], "UTF-8");
		capabilities.setCapability("description", args[3]);
		capabilities.setCapability("platformName", "iOS");
		//capabilities.setCapability("deviceName", args[3]);
		
		//The below code shares the test execution with the Eclipse plug-in, thus enabling sharing the devices. 
		try { 
		    EclipseConnector connector = new EclipseConnector(); 
		    String eclipseExecutionId = connector.getExecutionId();                  
		    capabilities.setCapability("eclipseExecutionId", eclipseExecutionId); 
		} catch (IOException ex) { 
		    ex.printStackTrace(); 
		   
		}
		RemoteWebDriver mobileDriver = new RemoteWebDriver(new URL("https://" + user + ':' + password + '@' + host + "/nexperience/wd/hub"), capabilities);
		
		
		DesiredCapabilities dc = DesiredCapabilities.chrome();
        RemoteWebDriver chromeDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
		
		
		
		try {
         // write your code here
			
			
			
			
			// Create 2 views (1 with each driver)
			WalmartBaseView mobileView = new WalmartBaseView(mobileDriver);
			WalmartBaseView desktopView = new WalmartBaseView(chromeDriver);
			
			Thread mobileT = new Thread(new Test(mobileDriver, mobileView));
			mobileT.start();
			
			Thread desktopT = new Thread(new Test(chromeDriver, desktopView));
			desktopT.start();
			//go to login page:
			///////walmartView.init().clickSignInPage().login("shirk@perfectomobile.com", "perfecto1");
			// Load page, search item
			/*
			 * WalmartBaseView walmartView = new WalmartBaseView(mobileDriver);
			SearchResultsPageView itemBL201View = walmartView.init().searchItem("BL201");
			System.out.println(itemBL201View.getItemNameByIndex(1));
			System.out.println(itemBL201View.getItemPriceByIndex(1));
			System.out.println(itemBL201View.getItemNameByIndex(5));
			System.out.println(itemBL201View.getItemPriceByIndex(5));
			
			System.out.println("hello");
			*/
			while(mobileT.isAlive() || desktopT.isAlive()){
				sleep(5000);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			mobileDriver.close();
			chromeDriver.close();
			// Download a pdf version of the execution report
			downloadReport(mobileDriver, "pdf", "C:\\temp\\report.pdf");
			
			// Release the driver
			mobileDriver.quit();
			chromeDriver.quit();
		}
		
		System.out.println("Run ended");
	}
	
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Download the report. 
	 * type - pdf, html, csv, xml
	 * Example: downloadReport(driver, "pdf", "C:\\test\\report");
	 * 
	 */
	private static void downloadReport(RemoteWebDriver driver, String type, String fileName) throws IOException {
		try { 
			String command = "mobile:report:download"; 
			Map<String, Object> params = new HashMap<>(); 
			params.put("type", type); 
			String report = (String)driver.executeScript(command, params); 
			File reportFile = new File(fileName + "." + type); 
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(reportFile)); 
			byte[] reportBytes = OutputType.BYTES.convertFromBase64Png(report); 
			output.write(reportBytes); output.close(); 
		} catch (Exception ex) { 
			System.out.println("Got exception " + ex); }
		}
	
	/*
	// Load page
	nxcView = nxcView.init();
	
	// Put credentials and submit
	nxcView = nxcView.putUsername("John").putPassword("Perfecto1");
	nxcView = nxcView.submit();
	
	// Go to OCR Page. Casting necessary.
	nxcView = ((MainView) nxcView).clickOcrLink();
	*/

	/**
	 * Download all the report attachments with a certain type.
	 * type - video, image, vital, network
	 * Examples:
	 * downloadAttachment("video", "C:\\test\\video", "flv");
	 * downloadAttachment("image", "C:\\test\\Image", "jpg");
	 */
	private void downloadAttachment(RemoteWebDriver driver, String type, String fileName, String suffix) throws IOException {
		try {
			String command = "mobile:report:attachment";
			boolean done = false;
			int index = 0;

			while (!done) {
				Map<String, Object> params = new HashMap<>();	

				params.put("type", type);
				params.put("index", Integer.toString(index));

				String attachment = (String)driver.executeScript(command, params);
				
				if (attachment == null) { 
					done = true; 
				}
				else { 
					File file = new File(fileName + index + "." + suffix); 
					BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file)); 
					byte[] bytes = OutputType.BYTES.convertFromBase64Png(attachment);	
					output.write(bytes); 
					output.close(); 
					index++; }
			}
		} catch (Exception ex) { 
			System.out.println("Got exception " + ex); 
		}
	}


	private static void switchToContext(RemoteWebDriver driver, String context) {
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		Map<String,String> params = new HashMap<String,String>();
		params.put("name", context);
		executeMethod.execute(DriverCommand.SWITCH_TO_CONTEXT, params);
	}

	private String getCurrentContextHandle(RemoteWebDriver driver) {		  
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		String context =  (String) executeMethod.execute(DriverCommand.GET_CURRENT_CONTEXT_HANDLE, null);
		return context;
	}

	private List<String> getContextHandles(RemoteWebDriver driver) {		  
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		List<String> contexts =  (List<String>) executeMethod.execute(DriverCommand.GET_CONTEXT_HANDLES, null);
		return contexts;
	}
	
	static class Test extends Thread{
		
		//RemoteWebDriver driver;
		WalmartBaseView view;
		
		public Test(RemoteWebDriver driver, WalmartBaseView view){
			//this.driver = driver;
			this.view = view;
		}
		public void run(){
			System.out.println("Thread started");
			/*
			SearchResultsPageView Bl201View = view.init().searchItem("BL201");
			System.out.println("Item1: Name="
					+ Bl201View.getItemNameByIndex(2) + " , Price="
					+ Bl201View.getItemPriceByIndex(2));
			*/
			
		}		
	}

}
