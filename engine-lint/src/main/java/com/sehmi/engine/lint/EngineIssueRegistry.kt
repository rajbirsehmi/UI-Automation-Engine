package com.sehmi.engine.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class EngineIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            DirectComposeTestUsageDetector.ISSUE,
            UiTestEngineSetupDetector.ISSUE
        )

    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = 14 // Works with older Lint versions if needed

    override val vendor: Vendor = Vendor(
        vendorName = "Sehmi",
        feedbackUrl = "https://github.com/sehmi/UIAutomationEngine/issues",
        contact = "https://github.com/sehmi/UIAutomationEngine"
    )
}
