"""
Pytest fixtures for Playwright test framework.

Migrated from: src/test/java/io/spring/selenium/tests/BaseTest.java

Mapping:
  - @BeforeSuite  -> session-scoped fixtures (browser)
  - @BeforeMethod -> function-scoped fixtures (page, context)
  - @AfterMethod  -> automatic fixture teardown + screenshot on failure
  - @AfterSuite   -> automatic session teardown
  - ExtentReports -> pytest-html / built-in JUnit XML + custom ReportListener
  - WebDriverManager -> Playwright manages browsers natively
  - config.properties -> config.ini parsed via configparser
"""

import configparser
from pathlib import Path
from typing import Generator

import pytest
from playwright.sync_api import Browser, BrowserContext, BrowserType, Page, Playwright, sync_playwright

CONFIG_PATH = Path(__file__).parent / "config.ini"


def load_config() -> configparser.ConfigParser:
    config = configparser.ConfigParser()
    if CONFIG_PATH.exists():
        config.read(CONFIG_PATH)
    return config


@pytest.fixture(scope="session")
def config() -> configparser.ConfigParser:
    """Load test configuration from config.ini."""
    return load_config()


@pytest.fixture(scope="session")
def base_url(config: configparser.ConfigParser) -> str:
    return config.get("application", "base_url", fallback="http://localhost:3000")


@pytest.fixture(scope="session")
def api_url(config: configparser.ConfigParser) -> str:
    return config.get("application", "api_url", fallback="http://localhost:8080")


@pytest.fixture(scope="session")
def playwright_instance() -> Generator[Playwright, None, None]:
    """Provide a Playwright instance for the entire test session."""
    with sync_playwright() as pw:
        yield pw


@pytest.fixture(scope="session")
def browser(
    playwright_instance: Playwright, config: configparser.ConfigParser
) -> Generator[Browser, None, None]:
    """
    Launch a browser for the test session.

    Replaces: BaseTest.initializeDriver() / WebDriverManager setup.
    Playwright manages browser binaries natively — no WebDriverManager needed.
    """
    browser_name = config.get("browser", "browser", fallback="chromium")
    headless = config.getboolean("browser", "headless", fallback=False)
    slow_mo = config.getint("browser", "slow_mo", fallback=0)

    browser_type: BrowserType = getattr(playwright_instance, browser_name)
    browser = browser_type.launch(headless=headless, slow_mo=slow_mo)
    yield browser
    browser.close()


@pytest.fixture
def browser_context(
    browser: Browser, config: configparser.ConfigParser
) -> Generator[BrowserContext, None, None]:
    """
    Create a fresh browser context per test (isolated cookies, storage).

    Replaces: BaseTest.@BeforeMethod driver setup.
    """
    width = config.getint("viewport", "width", fallback=1280)
    height = config.getint("viewport", "height", fallback=720)
    nav_timeout = config.getint("timeouts", "navigation_timeout", fallback=30000)

    context = browser.new_context(viewport={"width": width, "height": height})
    context.set_default_navigation_timeout(nav_timeout)
    yield context
    context.close()


@pytest.fixture
def page(
    browser_context: BrowserContext, config: configparser.ConfigParser
) -> Generator[Page, None, None]:
    """
    Create a new page per test.

    Replaces: BaseTest.@BeforeMethod — individual tests handle their own navigation.
    """
    action_timeout = config.getint("timeouts", "action_timeout", fallback=10000)
    pg = browser_context.new_page()
    pg.set_default_timeout(action_timeout)
    yield pg


# ---------------------------------------------------------------------------
# Hooks: screenshot on failure, tracing
# ---------------------------------------------------------------------------

def _reports_dir(config: configparser.ConfigParser) -> Path:
    return Path(config.get("reporting", "report_dir", fallback="playwright_tests/reports"))


def _screenshot_dir(config: configparser.ConfigParser) -> Path:
    return Path(
        config.get("screenshots", "screenshot_dir", fallback="playwright_tests/reports/screenshots")
    )


@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item: pytest.Item, call: pytest.CallInfo):
    """Capture screenshot on test failure (replaces BaseTest.captureScreenshot)."""
    outcome = yield
    report = outcome.get_result()

    if report.when == "call" and report.failed:
        cfg = load_config()
        screenshot_policy = cfg.get("screenshots", "screenshots", fallback="only-on-failure")

        if screenshot_policy in ("on", "only-on-failure"):
            page: Page | None = item.funcargs.get("page")
            if page and not page.is_closed():
                ss_dir = _screenshot_dir(cfg)
                ss_dir.mkdir(parents=True, exist_ok=True)
                path = ss_dir / f"{item.name}.png"
                page.screenshot(path=str(path))
                if hasattr(report, "extras"):
                    report.extras.append(pytest.html.extras.image(str(path)))
