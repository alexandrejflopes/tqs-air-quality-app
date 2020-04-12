package com.example.airquality.uitests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class SearchResult extends PageObject {

    @FindBy(id = "back_button")
    private WebElement backButton;

    @FindBy(id = "reportPageHeader")
    private WebElement reportPageHeader;

    @FindBy(id = "noDataMessage")
    private WebElement noDataMessage;

    @FindBy(id = "reportDetails")
    private WebElement reportDataPanel;

    @FindBy(id = "errorMessage")
    private WebElement errorMessage;

    public SearchResult(WebDriver driver) {
        super(driver);
    }

    public boolean isInitialized(){
        return backButton.isDisplayed();
    }

    public boolean existsErrorMessage(){
        return errorMessage.isDisplayed();
    }

    public boolean dataAvailable(){
        return reportDataPanel.isDisplayed();
    }

    public boolean noDataAvailable(){
        return noDataMessage.isDisplayed();
    }


}
