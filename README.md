# Resume Tailor

A Java console application that tailors a candidate's resume to a
job description and generates a PDF using a LaTeX template.

It combines deterministic skill matching and project-bullet ranking
with optional AI-assisted professional summary rewriting.

## Features

- Loads candidate information from a UTF-8 properties file.
- Matches skills using phrase boundaries and common aliases.
- Prioritizes project bullets by matching skills.
- Generates LaTeX and compiles it to PDF.
- Suggests a tailored professional summary using the Gemini API.
- Tries configured models in order when a model is unavailable.
- Flags selected unsupported skills, activities, proficiency claims,
  changed numbers, and removed experience qualifiers.
- Requires user review before applying an AI suggestion.
- Offers original-summary generation when AI is unavailable.
- Preserves the original candidate properties file.

## Technology

- Java 25
- Maven
- TestNG
- Jackson
- Java HttpClient
- Gemini API
- LaTeX and MiKTeX

## Requirements

- JDK 25
- Maven available on PATH
- MiKTeX with pdflatex available on PATH
- Internet access and a Gemini API key for AI features

Check your installation:

```bat
java -version
mvn -version
pdflatex --version
```

## Local configuration

Candidate information is stored in:

```text
data/candidate.properties
```

The resume template is stored in:

```text
templates/resume-template.tex
```

The candidate properties file is local and excluded from Git.
A fresh checkout requires a compatible candidate profile before running.

## Run in Eclipse

Import the project as an existing Maven project.

Use these entry points:

| Class | Purpose |
|---|---|
| Main | Generate a resume without AI rewriting |
| AiSetupCheck | List models that advertise generation support |
| AiSummaryPreview | Preview and validate an AI summary |
| AiResumeGenerator | Review a summary and generate a PDF |

Run the selected class as a Java Application from the project root.

For AI entry points, configure the following environment variables in
that application's Eclipse Run Configuration:

```text
GEMINI_API_KEY=your-api-key
GEMINI_MODELS=gemini-3.1-flash-lite,gemini-3.8-flash,gemini-3-flash-preview
```

GEMINI_MODELS takes priority over GEMINI_MODEL.
Model availability and quota depend on the account and may change.

Do not commit API keys or Eclipse launch configurations.

## Input

Paste a job description and enter END on a separate line.

Example:

```text
Junior QA Automation Engineer

Required skills: Java, Selenium, TestNG, REST Assured, JDBC and SQL.

The role focuses on banking workflow automation, reusable page objects,
UI/API/database checks, screenshot evidence, HTML reports and
GitHub Actions regression workflows.
END
```

For AI resume generation, review the suggestion and choose:

- APPLY: use the suggestion if automated checks passed.
- ORIGINAL: use the existing summary.
- CANCEL: stop without generating a resume.

## Output

Standard generation:

```text
output/tailored-resume.tex
output/tailored-resume.pdf
```

Reviewed generation:

```text
output/reviewed-resume.tex
output/reviewed-resume.pdf
```

Subsequent runs overwrite the corresponding output files.
The source candidate profile is not modified.

## Automatic model fallback

The client tries up to five configured models in order.

Fallback is enabled for HTTP 404, 408, 429, 500, 502, 503 and 504,
and request timeouts.

Attempts use short delays and an overall three-minute deadline.
A longer Retry-After requirement stops the attempt instead of making
another immediate request.

Authentication errors, invalid requests, blocked responses and invalid
generated output stop the AI attempt.

If all models fail, the application offers original-summary generation.

## Testing

Run:

```bat
mvn test
```

Latest verified result:

```text
Tests run: 28
Failures: 0
Errors: 0
Skipped: 0
```

Tests cover skill matching, content ranking, profile loading,
LaTeX generation and summary validation.

Live checks also verified AI summary application, original-summary
fallback, PDF compilation and switching after an unavailable model.

## Limitations

- AI rewriting currently covers the professional summary.
- Project bullets are ranked, not rewritten by AI.
- Validation uses heuristic checks and cannot prove factual accuracy.
- Human review is required even when automated checks pass.
- One-page layout was verified for the current profile and template;
  longer content may require layout adjustments.
- Matching counts describe candidate skills found in the job description,
  not an ATS score.
- AI requests send the summary, matching skills and job description.
  The application does not include candidate contact fields in that request.
- API requests can consume quota or incur charges according to account settings.

## Author

Subham Mohanty