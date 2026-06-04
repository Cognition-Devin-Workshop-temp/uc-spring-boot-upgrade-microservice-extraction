"""
Smoke tests to verify Playwright setup.

Migrated from: src/test/java/io/spring/selenium/tests/SeleniumSetupTest.java

Mapping:
  - @Test(groups={"smoke"})  -> @pytest.mark.smoke
  - TestNG assertions        -> plain assert / playwright expect()
  - driver.get(url)          -> page.goto(url)
  - driver.getTitle()        -> page.title()
"""

import pytest
from playwright.sync_api import Page, expect


@pytest.mark.smoke
def test_browser_launches(page: Page) -> None:
    """
    Verify browser launches and can navigate.

    Migrated from: SeleniumSetupTest.testBrowserLaunches()
    """
    page.goto("https://www.google.com")

    title = page.title()
    assert title is not None, "Page title should not be null"
    assert len(title) > 0, "Page title should not be empty"


@pytest.mark.smoke
def test_playwright_setup(page: Page) -> None:
    """
    Verify Playwright is properly configured.

    Migrated from: SeleniumSetupTest.testWebDriverManagerSetup()
    """
    assert page is not None, "Page should be initialized"
    assert not page.is_closed(), "Page should be open and usable"


@pytest.mark.smoke
def test_navigation_to_app(page: Page, base_url: str) -> None:
    """
    Verify navigation to the application under test.

    New test: ensures the configured base_url is reachable.
    """
    response = page.goto(base_url)
    if response is not None:
        assert response.ok, f"Expected OK response from {base_url}, got {response.status}"
