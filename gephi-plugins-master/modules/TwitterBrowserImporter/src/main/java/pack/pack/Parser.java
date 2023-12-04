package pack.pack;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Parser {
	
	
	WebDriver driver;
	String name;
	HashMap<String,Vertex> DataBase; 
	String html;
	Document doc; 
	String text; 
	Elements one = null;
	Elements frase_noPhoto;
	Elements frase_Photo;
	
	Parser(String url){
		
		System.setProperty("webdriver.chrome.driver", "chromedriver");
		
		driver = new ChromeDriver();
		name = "aladdin"; 
		DataBase = new HashMap<String,Vertex>();
    	driver.get(url);
	}
	
	void closeDriver() {
		driver.quit();
	}
	
	void init () {
		html = driver.getPageSource();
		doc = Jsoup.parse(html);
		text = doc.body().text(); 
		one = null;

    	
    	frase_noPhoto = doc.getElementsByClass("tweet js-stream-tweet js-actionable-tweet js-profile-popup-actionable dismissible-content\n" + 
		  		"      \n" + 
		  		"      \n" + 
		  		"      \n" + 
		  		"       descendant permalink-descendant-tweet\n" + 
		  		"");
    	
    	frase_Photo = doc.getElementsByClass("tweet js-stream-tweet js-actionable-tweet js-profile-popup-actionable dismissible-content\n" + 
		  		"      \n" + 
		  		"      \n" + 
		  		"      \n" + 
		  		"       has-cards descendant permalink-descendant-tweet has-content\n" + 
		  		"");
		
	}
	
	void threadSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void wait (String xpath, Boolean _type) {
		
		if (_type)
			while(true) {
				try {
					driver.findElement(By.xpath(xpath));
					break;
				}catch(Exception e) {}
			}
		else 
			while(true) {
				try {
					driver.findElement(By.xpath(xpath));
					
				}catch(Exception e) {break;}
			}
	}
	
	void parsePhoto() {
    	for(Element head : frase_Photo) {
    		
    		Vertex rep;
    		try {
	    		String value = head.selectFirst("span[class*=username u-dir u-textTruncate]").text();
	    		Element aux = head.select("div[class*=ReplyingToContextBelowAuthor]").first();
	    		String[] tokens = aux.select("span[class*=username u-dir u-textTruncate]").text().split(" ");
	    		
	    		if (DataBase.containsKey(value)) {
	    			DataBase.get(value).connections.add(tokens[0]);
	    		} else {
	    			rep = new Vertex (value);
	    			rep.connections.add(tokens[0]);
	    			DataBase.put(value, rep);
	    		}
    		} catch (Exception e) {continue;}
    	}	
	}
	
	
	
	void parseNoPhoto() {
    	for(Element head : frase_noPhoto) {
    		Vertex rep;
    		try {
	    		String value = head.selectFirst("span[class*=username u-dir u-textTruncate]").text();
	    		Element aux = head.select("div[class*=ReplyingToContextBelowAuthor]").first();
	    		String[] tokens = aux.select("span[class*=username u-dir u-textTruncate]").text().split(" ");
	    		if (DataBase.containsKey(value)) {
	    			DataBase.get(value).connections.add(tokens[0]);
	    		} else {
	    			rep = new Vertex (value);
	    			rep.connections.add(tokens[0]);
	    			DataBase.put(value, rep);
	    		}
    		}catch (Exception e) {continue;}
    	}
	}
	
	void scrollAll() {
		int i = 1;
        int j = 1;
        int aux = 0;
        Boolean pass_j = false;
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        //This will scroll the web page till end.
        while (true) {
        	try {
        		try {
        			js.executeScript("arguments[0].scrollIntoView();",driver.findElement(By.xpath("(//div[@class='tweet js-stream-tweet js-actionable-tweet js-profile-popup-actionable dismissible-content\n" + 
                    		"      \n" + 
                    		"      \n" + 
                    		"      \n" + 
                    		"       has-cards descendant permalink-descendant-tweet has-content\n" + 
                    		"'])[" + Integer.toString(j) +"]")));
        			j++;
        			pass_j = true;
        		} catch(Exception d) {}
        		
        		js.executeScript("arguments[0].scrollIntoView();",driver.findElement(By.xpath("(//div[@class='tweet js-stream-tweet js-actionable-tweet js-profile-popup-actionable dismissible-content\n" + 
                		"      \n" + 
                		"      \n" + 
                		"      \n" + 
                		"       descendant permalink-descendant-tweet\n" + 
                		"'])[" + Integer.toString(i) +"]")));
        		i++;
        		
        		aux = 0;
        		//System.out.println(i);
        	} catch (Exception e) {
        		
        		try {
        			if (pass_j) j--;
        			js.executeScript("arguments[0].scrollIntoView();",driver.findElement(By.xpath("(//div[@class='tweet js-stream-tweet js-actionable-tweet js-profile-popup-actionable dismissible-content\n" + 
                    		"      \n" + 
                    		"      \n" + 
                    		"      \n" + 
                    		"       has-cards descendant permalink-descendant-tweet has-content\n" + 
                    		"'])[" + Integer.toString(j) +"]")));
        			j++;
        			pass_j = false;
        			
        		} catch(Exception d) {
        			threadSleep(1000);
            		aux++;
            		if (aux >= 3) {
            			System.out.println(i);
            			break;
            		}
        		}
        		
        		
        	}
        }
	}
	
	
	void parseComments() {
		this.scrollAll();
		this.init();
		this.parsePhoto();
		this.parseNoPhoto();
	}
	

	void parserLikes() {
		
		driver.findElement(By.xpath("//a[@class='request-favorited-popup']")).click();
		
		wait("//a[@class='SignupDialog-signinLink']",true);
		
		driver.findElement(By.xpath("//a[@class='SignupDialog-signinLink']")).click();

		wait("//div[@class='AppContent wrapper wrapper-login']",false);
		
		driver.findElement(By.xpath("//a[@class='request-favorited-popup']")).click();
		System.out.println("inside-Likes");
		
		threadSleep(3000);
		
		WebElement author = driver.findElement(By.xpath("//a[@class='account-group js-account-group js-action-profile js-user-profile-link js-nav']//span[@class='username u-dir u-textTruncate']"));
		List<WebElement> names = driver.findElements(By.xpath("//a[@class='account-group js-user-profile-link']//span[@class='username u-dir u-textTruncate']"));
		
		Vertex vert = new Vertex(author.getText()); 
		for (WebElement name : names) {
			
			vert.addVertex(name.getText());
			
		}
		
		DataBase.put(author.getText(), vert);
		
	}
	
	void parserRetweet() {
		driver.findElement(By.xpath("//a[@class='request-retweeted-popup']")).click();
		
		wait("//a[@class='SignupDialog-signinLink']",true);

		driver.findElement(By.xpath("//a[@class='SignupDialog-signinLink']")).click();
		
		wait("//div[@class='AppContent wrapper wrapper-login']", false);

		System.out.println("inside-Retweets");

		driver.findElement(By.xpath("//a[@class='request-retweeted-popup']")).click();
		threadSleep(3000);


		WebElement value = driver.findElement(By.xpath("//a[@class='account-group js-account-group js-action-profile js-user-profile-link js-nav']//span[@class='username u-dir u-textTruncate']"));	

		List<WebElement> names = driver.findElements(By.xpath("//a[@class='account-group js-user-profile-link']//span[@class='username u-dir u-textTruncate']"));

		Vertex vert = new Vertex(value.getText()); 

		for(WebElement it : names) {

			vert.addVertex(it.getText());
		}


		DataBase.put(value.getText(), vert);


	}
	
	
	void printData() {
    	for (String i : DataBase.keySet()) {
    		System.out.println("name: " + i + "	Replays:	"  + DataBase.get(i).connections);
    	}
	}
	

}