package com.devansh;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutomateWebClockIn {

    public static void main(String[] args) throws Exception {

        //1-> set the system property
        System.setProperty("webdriver.chrome.driver", "C:\\Browser Driver\\chromedriver.exe");

        //2-> create a driver(chrome driver)
        WebDriver driver = new ChromeDriver();

        //3-> opening url(www.keka.com)
        driver.get("https://app.keka.com/Account/Login");
        driver.manage().window().maximize();

        //4-> find elements(by xpath) and enter value and click
        driver.findElement(By.xpath("//*[@id=\"email\"]")).sendKeys("devanshsingh@lambdatest.com");
        driver.findElement(By.xpath("/html/body/div/div[2]/div[1]/div[2]/form[1]/div/button")).click();
        driver.findElement(By.xpath("/html/body/div/div[2]/div[1]/div[2]/div[2]/div[2]/button/div/p")).click();


        driver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys("Lambdatest@123");

        /*
        //5-> now will handle captcha manually (to be automated)
        By captchaSubmitButton = By.xpath("/html/body/div/div[2]/div[1]/div[2]/form/div/button");
        By captchaInput = By.xpath("//*[@id=\"captcha\"]");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(10));
        wait.until(d -> {
            try {
                String val = d.findElement(captchaInput).getAttribute("value");
                return val != null && val.trim().length() == 5;
            } catch (Exception e) {
                return false;
            }
        });

        //6-> now automate further process
        //click submit button
        wait.until(ExpectedConditions.elementToBeClickable(captchaSubmitButton));
        driver.findElement(captchaSubmitButton).click();

         */

        //Read captcha automatically
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(10));

        // Capture captcha image
        WebElement captchaImage = driver.findElement(By.xpath("//*[@id=\"imgCaptcha\"]"));
        File captchaScreenshot = captchaImage.getScreenshotAs(OutputType.FILE);

        // Use CaptchaReader class
        String captchaText = readCaptcha(captchaScreenshot);

        // Enter captcha
        WebElement captchaField = driver.findElement(By.xpath("//*[@id=\"captcha\"]"));
        captchaField.sendKeys(captchaText);

        // Click Login(captcha)
        WebElement loginButton = driver.findElement(By.xpath("/html/body/div/div[2]/div[1]/div[2]/form/div/button"));
        loginButton.click();

        System.out.println("Captcha entered...");


        //7-> click on otp request for mail
        driver.findElement(By.xpath("/html/body/div/div[2]/div[1]/div[2]/div/form[2]/button/span[2]")).click();

        /*
        //8-> enter otp (manual)
        By otpInput = By.xpath("//*[@id=\"code\"]");
        By otpSubmitButton = By.xpath("/html/body/div/div[2]/div[1]/div[2]/form/div/button");
        wait.until(d -> {
           try {
               String otp = d.findElement(otpInput).getAttribute("value");
               return otp != null && otp.trim().length() == 6;
           } catch (Exception e) {
               return false;
           }
        });
         */

        //Auto OTP
        By otpInput = By.xpath("//*[@id=\"code\"]");
        By otpSubmitButton = By.xpath("/html/body/div/div[2]/div[1]/div[2]/form/div/button");
        String otpVal = null;
        for (int i = 0; i < 5; i++) {
            otpVal = getOtp();
            if (otpVal != null && otpVal.trim().length() == 6) {
                break;
            }
            Thread.sleep(5000); // wait 5 sec before retry
        }

        if (otpVal != null) {
            driver.findElement(otpInput).sendKeys(otpVal);
        } else {
            throw new RuntimeException("OTP not received from email!");
        }

        System.out.println("OTP entered successfully");

        //9-> now continue auto script
        wait.until(ExpectedConditions.elementToBeClickable(otpSubmitButton));
        driver.findElement(otpSubmitButton).click();

        System.out.println("Logged in to Keka successfully...");

        //10-> logged-in (now wait to load all elements)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='accordion']")));

        //11-> locate me button to hover upon
        WebElement meButton = driver.findElement(By.xpath("//*[@id=\"accordion\"]/li[2]/a/span[2]"));
        Actions actions = new Actions(driver);
        actions.moveToElement(meButton).perform();

        System.out.println("cursor moved to me...");

        //12-> click attendance in Me tab
        By attendanceXPath = By.xpath("//*[@id='accordion']/li[2]/div/ul/li[1]/a/span");
        wait.until(ExpectedConditions.visibilityOfElementLocated(attendanceXPath));
        wait.until(ExpectedConditions.elementToBeClickable(attendanceXPath)).click();

        System.out.println("Clicked on attendance....");

/*
        //13-> directly enter "web clock in" to search bar
        By searchButton = By.xpath("//*[@id=\"preload\"]/xhr-app-root/xhr-page-header/nav/div/div[1]/form/div/xhr-global-search-typeahead/button");
        //WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        //searchBtn.click();
        driver.findElement(searchButton).click();
        System.out.println("Clicked search button....");



        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("waited for some time");


        By box = By.cssSelector("#pallete-tailwind > div.command-palette > div > div > div.relative.flex.sm\\:w-\\[calc\\(100\\%-2em\\)\\].sm\\:h-\\[85\\%\\].max-h-\\[84vh\\].w-\\[650px\\].items-start.justify-center.sm\\:flex-col > div > div.flex.items-center.space-x-1\\.5.pl-3.relative.border-bottom-light-grey > div > input");
        WebElement dropdown = wait.until(ExpectedConditions.presenceOfElementLocated(box));

        System.out.println("searching xpath...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(box));
        System.out.println("box located");

        WebElement searchBox = driver.findElement(box);
        searchBox.sendKeys("Web Clock In");
        System.out.println("entered text");
        // Optional: Press ENTER to select
        searchBox.sendKeys(Keys.ENTER);
*/

        By notificationButton = By.xpath("//*[@id='preload']/xhr-app-root/xhr-page-header/notification-prompt/div/div/div[2]/button[1]");

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            WebElement notificationButtonElement = wait.until(ExpectedConditions.elementToBeClickable(notificationButton));
            notificationButtonElement.click();
            System.out.println("Notification prompt closed...");
        } catch (Exception e) {
            System.out.println("No notification prompt found.");
        }

        String xpath1 = "//*[@id=\"preload\"]/xhr-app-root/div/employee-me/div/employee-attendance/div/div/div/div/employee-attendance-stats/div/div[3]/employee-attendance-request-actions/div/div/div/div/div[2]/div/div[1]/a[1]";
        String xpath2 = "//*[@id='preload']/xhr-app-root/div/employee-me/div/employee-attendance/div/div/div/div/employee-attendance-stats/div/div[3]/employee-attendance-request-actions/div/div/div/div/div[2]/div/div[1]/div[1]/button";
        String xpath3 = "//*[@id='preload']/xhr-app-root/div/employee-me/div/employee-attendance/div/div/div/div/employee-attendance-stats/div/div[3]/employee-attendance-request-actions/div/div/div/div/div[2]/div/div[1]/div[1]/button[1]";


        try {
            By link1 = By.xpath(xpath1);
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement linkEle1 = wait.until(ExpectedConditions.elementToBeClickable(link1));
            System.out.println("Link found....");
            linkEle1.click();
            System.out.println("Clocked In....");
        } catch (Exception e) {
            By link2 = By.xpath(xpath2);
            By link3 = By.xpath(xpath3);
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement linkEle2 = wait.until(ExpectedConditions.elementToBeClickable(link2));
            linkEle2.click();

            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement linkEle3 = wait.until(ExpectedConditions.elementToBeClickable(link2));
            linkEle3.click();

            System.out.println("Clocked Out....");
        }

        Thread.sleep(5000);
        driver.quit();
        
    }

    public static String getOtp() throws Exception {
        String email = "devanshsingh@lambdatest.com";
        String appPassword = "kwryijztywjltkzn";

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", email, appPassword);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        int count = inbox.getMessageCount();
        for (int i = count; i > Math.max(count - 5, 0); i--) {
            Message message = inbox.getMessage(i);

            String subject = message.getSubject();
            if (subject != null && subject.toLowerCase().contains("otp")) {
                String content = message.getContent().toString();

                Matcher matcher = Pattern.compile("OTP:\\s*(\\d{6})").matcher(content);
                if (matcher.find()) {
                    return matcher.group(1);  // OTP number like 017893
                }

            }
        }

        return null;
    }

    private static final String TESSDATA_PATH = "C:\\Program Files\\Tesseract-OCR\\tessdata";

    public static String readCaptcha(File captchaImage) {
        ITesseract instance = new Tesseract();
        instance.setDatapath(TESSDATA_PATH);

        try {
            String text = instance.doOCR(captchaImage);
            text = text.replaceAll("[^a-zA-Z0-9]", ""); // clean unwanted chars
            return text;
        } catch (TesseractException e) {
            e.printStackTrace();
            return "";
        }
    }
}
