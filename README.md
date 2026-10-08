# Resume Tailor

A Java console application that tailors a resume to a job description
and generates a PDF using a LaTeX template.

The application combines skill matching and project-bullet ranking
with optional AI-assisted professional summary rewriting.

## Features

- Loads candidate information from a UTF-8 properties file.
- Matches skills using phrase boundaries and common aliases.
- Prioritizes project bullets based on matched skills.
- Generates LaTeX and compiles it into a PDF.
- Uses Gemini to suggest a tailored professional summary.
- Automatically tries another configured model when one is unavailable.
- Validates selected unsupported claims and formatting problems.
- Requires user review before applying an AI summary.
- Supports original-summary generation when AI is unavailable.
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

Verify your installation:

```bat
java -version
mvn -version
pdflatex --version
```

## Project Files

| Path | Purpose |
|---|---|
| src/main/java/com/resumetailor | Application source code |
| src/test/java/com/resumetailor | Automated tests |
| data/candidate.properties | Local candidate profile |
| templates/resume-template.tex | LaTeX resume template |
| output | Generated resumes and compiler logs |
| pom.xml | Maven configuration |

The personal candidate profile and output directory are excluded from Git.
A fresh checkout requires a compatible local candidate.properties file.

## Run in Eclipse

1. Import the project as an existing Maven project.
2. Ensure the project uses JDK 25.
3. Run the appropriate entry point as a Java Application.
4. Keep the working directory set to the project root.

| Class | Purpose |
|---|---|
| Main | Generate a resume without AI rewriting |
| AiSetupCheck | List models advertising generateContent support |
| AiSummaryPreview | Preview and validate an AI summary |
| AiResumeGenerator | Review a summary and generate a PDF |

## AI Configuration

In Eclipse, open:

Run → Run Configurations → Java Application → selected configuration
→ Environment

Add:

```text
GEMINI_API_KEY=your-api-key
GEMINI_MODELS=gemini-3.1-flash-lite,gemini-3.8-flash,gemini-3-flash-preview
```

Click Apply, then Run directly from that configuration.

Environment settings belong to each run configuration.
Configuring one application does not automatically configure another.

GEMINI_MODELS takes priority over GEMINI_MODEL.
If neither is provided, the client uses its default model list.

Model access and quota can vary by account and change over time.
Do not commit API keys or Eclipse launch configurations.

## Job Description Input

Paste the job description and enter END on a separate line:

```text
Junior QA Automation Engineer

Required skills: Java, Selenium, TestNG, REST Assured, JDBC and SQL.

The role focuses on banking workflow automation, reusable page objects,
UI/API/database checks, screenshot evidence, HTML reports and
GitHub Actions regression workflows.
END
```

Paste only the job description, without previous console output.

## Summary Review

The AI flow displays the original summary, suggestion and validation results.

Choose:

- APPLY: use the suggestion when automated checks pass.
- ORIGINAL: retain the original summary.
- CANCEL: stop without generating a resume.

Review factual accuracy even when automated checks pass.

## Automatic Model Fallback

Models are tried in the configured order, with up to five models.

Fallback is enabled for:

- HTTP 404, 408 and 429.
- HTTP 500, 502, 503 and 504.
- Request timeouts.

The client uses short delays between attempts and an overall
three-minute deadline.

A long Retry-After requirement stops the attempt instead of immediately
calling another model.

Authentication errors, invalid requests, blocked responses and invalid
generated output stop the AI attempt.

When AI generation fails, the review flow still offers ORIGINAL.

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
The original candidate.properties file is not modified.

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

Tests cover:

- Skill matching and aliases.
- Project-bullet ranking.
- Profile loading.
- LaTeX generation and escaping.
- Summary validation.

Live checks verified:

- AI summary generation.
- Rejection of flagged suggestions.
- APPLY and ORIGINAL flows.
- PDF compilation.
- Automatic switching after an unavailable model.

## Troubleshooting

### GEMINI_API_KEY is missing

Add the key to the environment of the exact run configuration being launched.
Click Apply and Run directly from that configuration.

### Model unavailable

Check the console for the attempted model and error details.
Update GEMINI_MODELS using models available to your account.

### PDF compilation fails

Inspect the compiler log in the output directory.
Confirm pdflatex is available and required LaTeX packages are installed.

### Maven cannot delete target

Close active application and test runs.
Check for Eclipse or OneDrive file locks.

If needed, remove generated build files from the project directory:

```bat
rmdir /s /q target
mvn clean test
```

## Limitations

- AI rewriting currently covers the professional summary.
- Project bullets are ranked, not rewritten by AI.
- Validation is heuristic and cannot prove factual accuracy.
- Human review remains necessary.
- One-page layout was verified for the current profile and template.
  Longer content may require layout changes.
- Matching counts are not ATS scores.
- AI requests include the original summary, matched skills and job description.
  Candidate contact fields are not included by the application.
- API calls consume quota and may incur charges according to account settings.

## Author

Subham Mohanty

GitHub: https://github.com/subhammohanty131