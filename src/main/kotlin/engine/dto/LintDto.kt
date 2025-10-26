package engine.dto

import error.LinterError

data class LintDto(
    val lintErrors: List<LinterError>,)