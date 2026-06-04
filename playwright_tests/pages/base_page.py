"""
Base page object for Playwright tests.

Migrated from: src/test/java/io/spring/selenium/pages/BasePage.java

Mapping:
  - WebDriver         -> playwright.sync_api.Page
  - WebDriverWait     -> Playwright's built-in auto-waiting
  - PageFactory       -> Not needed; Playwright locators are lazy and auto-retry
  - WebElement         -> playwright.sync_api.Locator

Key difference: Playwright locators auto-wait for elements to be actionable
before performing actions (visible, enabled, stable), so explicit waits are
rarely needed. The helpers below preserve the same API surface for familiarity.
"""

from playwright.sync_api import Locator, Page


class BasePage:
    """Base class for all page objects."""

    DEFAULT_TIMEOUT_MS = 10_000

    def __init__(self, page: Page) -> None:
        self.page = page

    # ------------------------------------------------------------------
    # Wait helpers (Playwright auto-waits, but these give explicit control)
    # ------------------------------------------------------------------

    def wait_for_visible(self, locator: Locator, timeout: int | None = None) -> Locator:
        """Wait for a locator to become visible."""
        locator.wait_for(state="visible", timeout=timeout if timeout is not None else self.DEFAULT_TIMEOUT_MS)
        return locator

    def wait_for_clickable(self, locator: Locator, timeout: int | None = None) -> Locator:
        """Wait for a locator to be enabled and visible (clickable)."""
        locator.wait_for(state="visible", timeout=timeout if timeout is not None else self.DEFAULT_TIMEOUT_MS)
        return locator

    # ------------------------------------------------------------------
    # Interaction helpers
    # ------------------------------------------------------------------

    def click(self, locator: Locator, timeout: int | None = None) -> None:
        """Click an element after waiting for it to be actionable."""
        locator.click(timeout=timeout if timeout is not None else self.DEFAULT_TIMEOUT_MS)

    def type_text(self, locator: Locator, text: str, timeout: int | None = None) -> None:
        """
        Clear and type text into an element.

        Replaces: BasePage.type(WebElement, String)
        """
        locator.fill(text, timeout=timeout if timeout is not None else self.DEFAULT_TIMEOUT_MS)

    def get_text(self, locator: Locator, timeout: int | None = None) -> str:
        """
        Get inner text from an element after waiting for visibility.

        Replaces: BasePage.getText(WebElement)
        """
        self.wait_for_visible(locator, timeout)
        return locator.inner_text()

    def is_displayed(self, locator: Locator) -> bool:
        """
        Check if an element is visible on the page.

        Replaces: BasePage.isDisplayed(WebElement)
        """
        return locator.is_visible()

    # ------------------------------------------------------------------
    # Navigation
    # ------------------------------------------------------------------

    def navigate(self, url: str) -> None:
        """Navigate to a URL."""
        self.page.goto(url)

    def get_title(self) -> str:
        return self.page.title()

    def get_url(self) -> str:
        return self.page.url
