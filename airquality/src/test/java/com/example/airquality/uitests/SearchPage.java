package com.example.airquality.uitests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SearchPage extends PageObject {

    @FindBy(id = "location_input")
    private WebElement searchBar;

    @FindBy(id = "search_button")
    private WebElement searchButton;

    @FindBy(id = "search_prompt_header")
    private WebElement searchHeader;

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public boolean isInitialized(){
        return searchHeader.isDisplayed();
    }

    public void enterLocation(String location){
        this.searchBar.clear();
        this.searchBar.sendKeys(location);
    }

    public SearchResult search(){
        searchButton.click();
        return new SearchResult(driver);
    }

}
