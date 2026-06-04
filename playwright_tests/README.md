# Playwright Test Framework (Python)

> Migrated from the Java Selenium + TestNG framework in `src/test/java/io/spring/selenium/`.

## Migration Reference

| Selenium / Java | Playwright / Python | Notes |
|---|---|---|
| `WebDriver` | `playwright.sync_api.Page` | Playwright `Page` replaces the Selenium driver |
| `WebDriverManager` | Built-in | Playwright manages browser binaries natively |
| `WebElement` | `Locator` | Locators are lazy and auto-retry |
| `WebDriverWait` / `ExpectedConditions` | Auto-waiting | Playwright waits automatically before actions |
| `PageFactory` / `@FindBy` | `page.locator()` | No annotation magic needed |
| `TestNG` | `pytest` | Test runner and assertion framework |
| `@Test(groups={"smoke"})` | `@pytest.mark.smoke` | Custom markers replace TestNG groups |
| `@BeforeSuite` / `@AfterSuite` | `session`-scoped fixtures | `conftest.py` manages lifecycle |
| `@BeforeMethod` / `@AfterMethod` | `function`-scoped fixtures | Fresh `page` per test |
| `ExtentReports` | `pytest-html` + JUnit XML | Built-in HTML and XML reporting |
| `TestListener.java` | `ReportListener` plugin | Markdown failure report generation |
| `config.properties` | `config.ini` | Same key-value config, INI format |
| `testng.xml` / `testng-smoke.xml` | `pytest.ini` + markers | `-m smoke` replaces suite XMLs |

## Directory Structure

```
playwright_tests/
├── conftest.py           # Fixtures: browser, context, page, config
├── config.ini            # Test configuration
├── pytest.ini            # pytest settings and markers
├── requirements.txt      # Python dependencies
├── README.md             # This file
├── pages/
│   ├── __init__.py
│   └── base_page.py      # Base page object with common methods
├── tests/
│   ├── __init__.py
│   └── test_setup.py     # Smoke tests (browser launch, setup verification)
├── utils/
│   ├── __init__.py
│   └── report_listener.py  # Failure report plugin (replaces TestListener.java)
└── reports/               # Generated at runtime (gitignored)
    ├── report.html
    ├── junit-results.xml
    └── screenshots/
```

## Setup

```bash
# Install Python dependencies
pip install -r playwright_tests/requirements.txt

# Install Playwright browsers (one-time)
playwright install --with-deps chromium
```

## Running Tests

```bash
# Run all tests
pytest --rootdir=. -c playwright_tests/pytest.ini

# Run smoke tests only (replaces testng-smoke.xml)
pytest --rootdir=. -c playwright_tests/pytest.ini -m smoke

# Run regression tests only
pytest --rootdir=. -c playwright_tests/pytest.ini -m regression

# Run in headed mode (visible browser)
pytest --rootdir=. -c playwright_tests/pytest.ini --headed

# Run with a specific browser
pytest --rootdir=. -c playwright_tests/pytest.ini --browser firefox
```

## Gradle Integration

A Gradle task is available to run Playwright tests alongside the existing build:

```bash
./gradlew playwrightTest
```

## Writing New Tests

### 1. Create a Page Object

```python
# playwright_tests/pages/login_page.py
from playwright.sync_api import Locator, Page
from playwright_tests.pages.base_page import BasePage


class LoginPage(BasePage):
    def __init__(self, page: Page) -> None:
        super().__init__(page)
        self.email_input: Locator = page.locator("[data-testid='email']")
        self.password_input: Locator = page.locator("[data-testid='password']")
        self.submit_button: Locator = page.locator("[data-testid='submit']")

    def login(self, email: str, password: str) -> None:
        self.type_text(self.email_input, email)
        self.type_text(self.password_input, password)
        self.click(self.submit_button)
```

### 2. Create a Test

```python
# playwright_tests/tests/test_login.py
import pytest
from playwright.sync_api import Page
from playwright_tests.pages.login_page import LoginPage


@pytest.mark.regression
def test_user_can_login(page: Page, base_url: str) -> None:
    page.goto(f"{base_url}/#/login")
    login_page = LoginPage(page)
    login_page.login("user@example.com", "password123")
    assert page.url.endswith("/#/")
```

## Key Advantages Over Selenium

- **Auto-waiting**: No need for explicit waits; Playwright waits for elements to be actionable.
- **Browser management**: No WebDriverManager; `playwright install` handles everything.
- **Isolation**: Each test gets a fresh browser context (cookies, storage isolated).
- **Tracing**: Built-in trace viewer for debugging failures (`--tracing retain-on-failure`).
- **Multi-browser**: Run on Chromium, Firefox, and WebKit with the same API.
- **Parallel execution**: pytest-xdist or Playwright's native parallelism.
