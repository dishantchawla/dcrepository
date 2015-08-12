import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;

import jxl.Sheet;
import jxl.Workbook;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ExecuteTestCases {
	private WebDriver driver;
	private String baseUrl;

	@Test
	public void testChrome() throws Exception {
		Scanner scanner = new Scanner(System.in);
		System.out
				.println("Please enter the excel file name without extension (Only xls format files allowed):");
		String fileName = scanner.next() + ".xls";
		scanner.close();
		Workbook workbook = null;
		ArrayList<String> results = new ArrayList<String>();
		try {
			workbook = Workbook.getWorkbook(new File(fileName));
		} catch (FileNotFoundException fileNotFoundException) {
			System.out.println("No excel file found with the specified name.");
			Thread.sleep(3000);
			System.exit(0);
		}

		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\dishant.a.chawla\\Downloads\\chromedriver.exe");

		// Sheet sheet = workbook.getSheet(0);
		for (Sheet sheet : workbook.getSheets()) {
			driver = new ChromeDriver();
			Actions action = new Actions(driver);

			WebDriverWait wait = new WebDriverWait(driver, 10);
			baseUrl = sheet.getCell(2, 1).getContents().trim();
			driver.get(baseUrl);

			Method findElementMethod;
			WebElement element = null;
			String testCase, eventMethodName, parameters, findElementValue;
			int noOfRows = sheet.getRows();

			for (int r = 2; r < noOfRows; r++) {

				testCase = sheet.getCell(0, r).getContents().trim();
				findElementMethod = By.class.getMethod(sheet.getCell(1, r)
						.getContents().trim(), String.class);
				findElementValue = sheet.getCell(2, r).getContents().trim();
				eventMethodName = sheet.getCell(3, r).getContents().trim();
				parameters = sheet.getCell(4, r).getContents().trim();

				if (testCase.equalsIgnoreCase("Open URL")) {
					driver.navigate().to(findElementValue);
				} else if (eventMethodName.equalsIgnoreCase("scroll")) {
					for (int i = 1; i <= new Integer(parameters); i++) {
						driver.findElement(
								By.className("siteidentifier-wrapper")).click();
						action.sendKeys(Keys.END).perform();						
						
					}
					results.add(testCase + ": Pass");
					continue;
				}

				try {
					element = wait.until(ExpectedConditions
							.elementToBeClickable((By) findElementMethod
									.invoke(null, findElementValue)));
				} catch (TimeoutException exception) {
					results.add(testCase + ": Fail");
				}
				if (null != element) {
					if (eventMethodName.equalsIgnoreCase("sendKeys")) {
						element.getClass()
								.getMethod(eventMethodName,
										CharSequence[].class)
								.invoke(element,
										(Object) new String[] { parameters });
					} else if (eventMethodName.equalsIgnoreCase("contextClick")
							|| eventMethodName.equalsIgnoreCase("doubleClick")) {
						((Actions) action.getClass().getMethod(eventMethodName)
								.invoke(element)).perform();

					} else if (eventMethodName.equalsIgnoreCase("verify")) {
						if (element.getText().equals(parameters))
							results.add(testCase + ": Pass");
						else
							results.add(testCase + ": Fail");
					} else if (eventMethodName.equalsIgnoreCase("exists")) {
						if (element.isDisplayed())
							results.add(testCase + ": Pass");
						else
							results.add(testCase + ": Fail");
					} else if (eventMethodName
							.equalsIgnoreCase("clickUntilHidden")) {
						while (element.isDisplayed()) {
							wait.until(
									ExpectedConditions
											.elementToBeClickable(element))
									.click();
						}
						results.add(testCase + ": Pass");
						Thread.sleep(1000);
					} else {
						element.getClass().getMethod(eventMethodName)
								.invoke(element);
						results.add(testCase + ": Pass");
					}
				}
			}
		}
		SendMail.sendEmail(results);
	}
}