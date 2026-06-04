"""
Pytest plugin for generating failure reports.

Migrated from: src/test/java/io/spring/selenium/listeners/TestListener.java

Mapping:
  - ITestListener             -> pytest plugin hooks
  - onTestStart/Success/Fail  -> pytest_runtest_logreport
  - generateFailureReport     -> pytest_sessionfinish
  - Markdown failure report   -> identical output format
"""

import shutil
from datetime import datetime
from pathlib import Path


class ReportListener:
    """Pytest plugin that generates a markdown failure report (mirrors TestListener.java)."""

    def __init__(self, report_dir: str = "playwright_tests/reports") -> None:
        self.report_dir = Path(report_dir)
        self.total = 0
        self.passed = 0
        self.failed = 0
        self.skipped = 0
        self.failure_details: list[str] = []

    # ------------------------------------------------------------------
    # Hooks
    # ------------------------------------------------------------------

    def pytest_runtest_logreport(self, report) -> None:  # type: ignore[no-untyped-def]
        if report.when != "call":
            return

        self.total += 1
        if report.passed:
            self.passed += 1
        elif report.failed:
            self.failed += 1
            self._append_failure(report)
        elif report.skipped:
            self.skipped += 1

    def pytest_sessionfinish(self, session, exitstatus) -> None:  # type: ignore[no-untyped-def]
        if self.failed > 0:
            self._generate_report()

    # ------------------------------------------------------------------
    # Internals
    # ------------------------------------------------------------------

    def _append_failure(self, report) -> None:  # type: ignore[no-untyped-def]
        lines = [
            f"\n### {report.nodeid}\n",
            f"- **Test:** {report.nodeid}\n",
            f"- **Failure Reason:** {report.longreprtext[:500]}\n",
            "- **Details:**\n```\n",
            report.longreprtext,
            "\n```\n",
        ]
        self.failure_details.append("".join(lines))

    def _generate_report(self) -> None:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.report_dir.mkdir(parents=True, exist_ok=True)

        report_path = self.report_dir / f"failure-report-{timestamp}.md"
        with open(report_path, "w") as f:
            f.write(f"# Test Failure Report - {timestamp}\n\n")
            f.write("## Summary\n")
            f.write(f"- Total Tests: {self.total}\n")
            f.write(f"- Passed: {self.passed}\n")
            f.write(f"- Failed: {self.failed}\n")
            f.write(f"- Skipped: {self.skipped}\n\n")
            f.write("## Failed Tests\n")
            f.write("".join(self.failure_details))

        # Archive the report
        archive_dir = self.report_dir / "archive"
        archive_dir.mkdir(parents=True, exist_ok=True)
        shutil.copy2(report_path, archive_dir / f"failure-report-{timestamp}.md")
