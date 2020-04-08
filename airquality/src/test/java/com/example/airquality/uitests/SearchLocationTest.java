package com.example.airquality.uitests;

import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SearchLocationTest extends FunctionalTest {

    public SearchLocationTest() {
        super();
        setUp();
    }


    @Test
    public void searchGotNoData(){
        driver.get("http://localhost:8080/");

        SearchPage searchPage = new SearchPage(driver);
        assertTrue(searchPage.isInitialized());

        searchPage.enterLocation("North Pole");
        SearchResult resultPage = searchPage.search();
        assertTrue(resultPage.isInitialized());
        assertTrue(resultPage.noDataAvailable());

        /*
         * if there's no data available and no errors then the resultPage
         * cannot find elements related to unexpected error message and
         * report details as they don't exist in the report details page
         * */
        assertThrows(NoSuchElementException.class, resultPage::existsErrorMessage);
        assertThrows(NoSuchElementException.class, resultPage::dataAvailable);

    }

    @Test
    public void searchGotData(){
        driver.get("http://localhost:8080/");

        SearchPage searchPage = new SearchPage(driver);
        assertTrue(searchPage.isInitialized());

        searchPage.enterLocation("Gafanha da Nazare");
        SearchResult resultPage = searchPage.search();
        assertTrue(resultPage.isInitialized());
        assertTrue(resultPage.dataAvailable());

        /*
         * if there's data available, then the resultPage
         * cannot find elements related to unexpected error and no data
         * messages, as they don't exist in the report data page
         * */
        assertThrows(NoSuchElementException.class, resultPage::existsErrorMessage);
        assertThrows(NoSuchElementException.class, resultPage::noDataAvailable);

    }

    @Test
    public void searchGotUnexpectedError(){
        driver.get("http://localhost:8080/");

        SearchPage searchPage = new SearchPage(driver);
        assertTrue(searchPage.isInitialized());

        // empty location
        searchPage.enterLocation("");

        SearchResult resultPage = searchPage.search();
        assertTrue(resultPage.isInitialized());
        assertTrue(resultPage.existsErrorMessage());

        /*
         * if there's an unexpected error, then the resultPage
         * cannot find elements related to report data as they
         * don't exist in the error page
         * */
        assertThrows(NoSuchElementException.class, resultPage::dataAvailable);
        assertThrows(NoSuchElementException.class, resultPage::noDataAvailable);

    }

}
