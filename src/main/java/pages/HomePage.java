package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class HomePage extends BasePage {

    // Locators
    public By acceptAllCookiesButton = By.id("onetrust-accept-btn-handler");
    public By originInputLocator = By.xpath("//input[@id='origin']");
    public By destinationInputLocator = By.xpath("//input[@id='destination']");
    private By dateDepartureInput = By.xpath("//input[@id='first-input']");
    private By onlyDepartureRadioButtonLabel = By.xpath("//label[@for='trip-go']");
    private By onlyDepartureRadioButtonInput = By.xpath("//input[@id='trip-go']");
    private By acceptButtonLocator = By.xpath("//button[contains(text(),'Aceptar')]");
    private By buscarBilleteLocator = By.xpath("//button[@title='Buscar billete']");
    private By nextMonthButton = By.xpath("//button[contains(@class, 'lightpick__next-action')]");
    private By monthYearLabel = By.cssSelector("span.rf-daterange-picker-alternative__month-label");

    // Constructor
    public HomePage(WebDriver webDriver) {
        super(webDriver); // Calls the constructor from parent class and its variable
    }

    // ---------- Helper methods ----------

    /**
     * Returns the "Buscar billete" (search ticket) button.
     */
    private WebElement getSearchButton() {
        waitUntilElementIsDisplayed(buscarBilleteLocator, TIMEOUT);
        return webDriver.findElement(buscarBilleteLocator);
    }

    /**
     * Asserts that the search button's enabled state is consistent with
     * the current origin and destination station fields:
     *
     * - If BOTH origin and destination have a non-empty value → button MUST be enabled.
     * - Otherwise → button MUST be disabled.
     */
    private void assertSearchButtonStateMatchesStations() {
        // Ensure inputs are present
        WebElement originInput = webDriver.findElement(originInputLocator);
        WebElement destinationInput = webDriver.findElement(destinationInputLocator);
        WebElement searchButton = getSearchButton();

        String originValue = originInput.getAttribute("value");
        String destinationValue = destinationInput.getAttribute("value");

        boolean originFilled = originValue != null && !originValue.trim().isEmpty();
        boolean destinationFilled = destinationValue != null && !destinationValue.trim().isEmpty();

        if (originFilled && destinationFilled) {
            Assert.assertTrue(
                    searchButton.isEnabled(),
                    "Search button should be ENABLED when both origin and destination are selected. " +
                            "Origin='" + originValue + "', Destination='" + destinationValue + "'."
            );
        } else {
            Assert.assertFalse(
                    searchButton.isEnabled(),
                    "Search button should be DISABLED when origin or destination is missing. " +
                            "Origin='" + originValue + "', Destination='" + destinationValue + "'."
            );
        }
    }

    // ---------- Page actions with assertions ----------

    /**
     * Accepts all cookies on any page.
     */
    public void clickAcceptAllCookiesButton() {
        waitUntilElementIsDisplayed(acceptAllCookiesButton, TIMEOUT);
        scrollElementIntoView(acceptAllCookiesButton);
        clickElement(acceptAllCookiesButton);

        // After cookies, verify current state rule for the search button
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Types the trip origin and asserts the field value and search button rule.
     *
     * @param originStation station to type in origin field
     */
    public void enterOrigin(String originStation) {
        WebElement originInput = webDriver.findElement(originInputLocator);

        // Enter the origin
        originInput.click();
        originInput.sendKeys(originStation);
        originInput.sendKeys(Keys.DOWN);
        originInput.sendKeys(Keys.ENTER);

        // Asserts the origin station (adapt value if needed)
        Assert.assertEquals(
                originInput.getAttribute("value"),
                "MADRID (TODAS)",
                "Origin station value is not as expected."
        );

        // Verify search button rule after modifying origin
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Types the trip destination and asserts the field value and search button rule.
     *
     * @param destinationStation station to type in destination field
     */
    public void enterDestination(String destinationStation) {
        WebElement destinationInput = webDriver.findElement(destinationInputLocator);

        // Enter the destination
        destinationInput.click();
        destinationInput.sendKeys(destinationStation);
        destinationInput.sendKeys(Keys.DOWN);
        destinationInput.sendKeys(Keys.ENTER);

        // Asserts for the destination station (adapt value if needed)
        Assert.assertEquals(
                destinationInput.getAttribute("value"),
                "BARCELONA (TODAS)",
                "Destination station value is not as expected."
        );

        // Verify search button rule after modifying destination
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Clicks on the departure date calendar on the 'Home' page.
     */
    public void selectDepartureDate() {
        WebDriverWait wait = new WebDriverWait(webDriver, TIMEOUT);
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(dateDepartureInput));
        button.click();

        // Changing date should NOT break the button rule
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Marks the "only go trip" radio button as selected or unselected.
     *
     * @param expectedSelected boolean with the expected selected state of the element
     */
    public void clickSoloIdaButtonSelected(boolean expectedSelected) {
        waitUntilElementIsDisplayed(onlyDepartureRadioButtonLabel, TIMEOUT);
        scrollElementIntoView(onlyDepartureRadioButtonLabel);
        setElementSelected(onlyDepartureRadioButtonInput, onlyDepartureRadioButtonLabel, expectedSelected);

        // Extra assert for radio button itself
        WebElement radioInput = webDriver.findElement(onlyDepartureRadioButtonInput);
        Assert.assertEquals(
                radioInput.isSelected(),
                expectedSelected,
                "The 'solo ida' radio button selected state does not match the expected value."
        );

        // And still, the button rule must hold
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Selects a departure date, a number of days ahead from the current date.
     *
     * @param webDriver driver instance (note: also exists as class field)
     * @param daysAfter number of days to add to the current date
     */
    public void selectDateDaysLater(WebDriver webDriver, int daysAfter) {
        LocalDate targetDate = LocalDate.now().plusDays(daysAfter);
        WebDriverWait wait = new WebDriverWait(webDriver, TIMEOUT);

        // Navigate to the correct month
        while (true) {
            String dateLabel = webDriver.findElement(monthYearLabel).getText().toLowerCase();
            String targetMonthName = targetDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toLowerCase();

            if (dateLabel.contains(targetMonthName)) {
                break;
            }

            webDriver.findElement(nextMonthButton).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(monthYearLabel));
        }

        // Click the correct day
        String dayXpath = String.format(
                "//div[contains(@class, 'lightpick__day') and text()='%d']",
                targetDate.getDayOfMonth()
        );
        WebElement dayElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(dayXpath)));
        dayElement.click();

        // After choosing the day, verify button rule
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Clicks the 'Accept' button on the calendar on the 'Home' page.
     */
    public void clickAcceptButton() {
        waitUntilElementIsDisplayed(acceptButtonLocator, TIMEOUT);
        clickElement(acceptButtonLocator);

        // After closing the calendar, rule must still hold
        assertSearchButtonStateMatchesStations();
    }

    /**
     * Searches the selected ticket on the 'Home' page.
     * This method asserts that:
     *  - Origin and destination are filled
     *  - Search button is enabled
     */
    public void clickSearchTicketButton() {
        WebElement originInput = webDriver.findElement(originInputLocator);
        WebElement destinationInput = webDriver.findElement(destinationInputLocator);
        WebElement searchButton = getSearchButton();

        String originValue = originInput.getAttribute("value");
        String destinationValue = destinationInput.getAttribute("value");

        // Preconditions before clicking
        Assert.assertTrue(
                originValue != null && !originValue.trim().isEmpty(),
                "Origin station must be selected before searching."
        );
        Assert.assertTrue(
                destinationValue != null && !destinationValue.trim().isEmpty(),
                "Destination station must be selected before searching."
        );
        Assert.assertTrue(
                searchButton.isEnabled(),
                "Search button must be enabled before clicking it."
        );

        scrollElementIntoView(buscarBilleteLocator);
        clickElement(buscarBilleteLocator);
    }
}