# Skill Gap Analysis & Course Recommendation System

Minor Project — Deepali Mundra (EN23CS306015) | Guide: Mr. Ashish Kumawat

This is the **extended version** of the original SkillGapAnalyzer, adding authentication,
resume parsing, personalized/history-tracked analysis, and a live-data provider abstraction
on top of the original working app. **All original functionality still works exactly as
before, with zero login required, at `/`.**

---

## ⚠️ Important note on testing (read this first)

This code was written and reviewed carefully (brace-balance checked, dependency paths
verified, Thymeleaf expressions checked against model attributes) but **could not be
compiled or run in the environment it was written in**, because that environment has no
access to Maven Central and cannot start a JVM server process. It has **not** been executed
against a real JVM/Maven build.

**You must run `mvn clean install` and start the app yourself, then report back any
compilation or runtime errors so they can be fixed.** Nothing below should be read as
"tested and confirmed working" — it is "implemented, and believed correct pending your
build."

---

## 1. Implementation Summary

| # | Feature | Status |
|---|---|---|
| 1 | Resume upload + Apache Tika parsing (PDF/DOC/DOCX) | Implemented |
| 2 | Resume validation (type, empty, size, corrupted file) | Implemented |
| 3 | Skill detection from resume text (reuses existing SkillExtractionService) | Implemented |
| 4 | Live job-data provider (Jsoup) with automatic static-dataset fallback | Implemented |
| 5 | Student registration | Implemented |
| 6 | Student login with JWT issuance | Implemented |
| 7 | BCrypt password hashing | Implemented |
| 8 | JWT validation via request filter (cookie or Bearer header) | Implemented |
| 9 | Protected routes (dashboard/resume/skills/analysis require login) | Implemented |
| 10 | Logout (cookie invalidation) | Implemented |
| 11 | Student profile with saved skills (add/remove/view) | Implemented |
| 12 | Resume-derived skills saved to profile | Implemented |
| 13 | Personalized skill-gap analysis (profile skills OR resume skills) | Implemented |
| 14 | Personalized course recommendations (reuses existing RecommendationService) | Implemented |
| 15 | Analysis history (save + list + detail view) | Implemented |
| 16 | Cross-user data isolation (Student A cannot see Student B's data) | Implemented + unit-tested |
| 17 | Centralized exception handling | Implemented |
| 18 | H2 / MySQL switchable via Spring profiles | Implemented |
| 19 | Updated dashboard (profile, resume, analysis, history, courses) | Implemented |
| 20 | Unit tests (extraction, gap calc, auth, resume validation, access control) | Written, **not executed** |
| 21 | Original anonymous flow (`/`, `/analyze`) | Untouched, still works |

---

## 2. Files Changed

### Modified
- `pom.xml` — added Security, JWT, Tika, Jsoup, MySQL connector, validation, thymeleaf-security dependencies
- `src/main/resources/application.properties` — now a base config; DB-specific settings moved to profiles
- `src/main/java/.../service/JobMarketDataService.java` — refactored into an orchestrator (Static/Live + fallback), **public API unchanged**
- `src/main/resources/templates/index.html` — added navbar + link to register
- `src/main/resources/templates/result.html` — added navbar + "saved to history" notice for personalized runs
- `src/main/resources/static/css/style.css` — added nav/card/alert/form styles

### Created — Entities
- `entity/Student.java`, `entity/Resume.java`, `entity/Analysis.java`, `entity/AnalysisMissingSkill.java`

### Created — Repositories
- `repository/StudentRepository.java`, `repository/ResumeRepository.java`, `repository/AnalysisRepository.java`

### Created — DTOs
- `dto/RegisterRequest.java`, `dto/LoginRequest.java`, `dto/StudentProfileDto.java`

### Created — Security
- `security/JwtUtil.java`, `security/JwtAuthenticationFilter.java`, `security/SecurityConfig.java`,
  `security/CustomUserDetailsService.java`, `security/CustomUserPrincipal.java`

### Created — Services
- `service/AuthService.java`, `service/ResumeService.java`, `service/CurrentStudentService.java`,
  `service/StudentSkillService.java`, `service/AnalysisHistoryService.java`,
  `service/JobDataProvider.java` (interface), `service/StaticJobDataProvider.java`,
  `service/LiveJobDataProvider.java`, `service/JobDataFetchException.java`

### Created — Controllers
- `controller/AuthController.java`, `controller/DashboardController.java`,
  `controller/ResumeController.java`, `controller/StudentSkillController.java`,
  `controller/AnalysisController.java`, `controller/AnalysisHistoryController.java`

### Created — Exceptions
- `exception/GlobalExceptionHandler.java`, `exception/DuplicateEmailException.java`,
  `exception/InvalidCredentialsException.java`, `exception/InvalidResumeException.java`,
  `exception/ResourceNotFoundException.java`, `exception/UnauthorizedAccessException.java`

### Created — Templates
- `templates/fragments/nav.html`, `templates/register.html`, `templates/login.html`,
  `templates/dashboard.html`, `templates/resume-upload.html`, `templates/skills-manage.html`,
  `templates/analysis-form.html`, `templates/analysis-history.html`,
  `templates/analysis-history-detail.html`, `templates/error.html`

### Created — Config
- `src/main/resources/application-h2.properties`, `src/main/resources/application-mysql.properties`

### Created — Tests
- `test/.../service/SkillExtractionServiceTest.java`, `GapAnalysisServiceTest.java`,
  `AuthServiceTest.java`, `ResumeServiceTest.java`, `AnalysisHistoryServiceTest.java`

### Deleted
- None. Nothing was removed.

### Untouched (reused as-is, per instructions)
- `entity/Skill.java`, `entity/Course.java`, `repository/SkillRepository.java`, `repository/CourseRepository.java`
- `service/SkillExtractionService.java`, `service/DemandAnalysisService.java`,
  `service/GapAnalysisService.java`, `service/RecommendationService.java`
- `controller/HomeController.java`, `config/DataSeeder.java`
- `data/skills.txt`, `data/job_postings.csv`, `data/courses.csv`

---

## 3. Database Changes

New tables (auto-created by Hibernate via `ddl-auto=update` — no manual migration needed):

| Table | Purpose | Key relationships |
|---|---|---|
| `students` | Registered users | — |
| `student_skills` | Join table: student ↔ skill (many-to-many) | Reuses existing `skills` table |
| `resumes` | One resume per student (extracted text only, no binary file) | One-to-one with `students` |
| `resume_skills` | Join table: resume ↔ detected skill (many-to-many) | Reuses existing `skills` table |
| `analyses` | One row per saved analysis run | Many-to-one with `students` |
| `analysis_missing_skills` | Missing-skill line items for one analysis, with demand % at that time | Many-to-one with `analyses` |

Existing tables (`skills`, `courses`) are **unchanged** in structure and reused directly —
no duplicate skill or course records are created by any new feature.

Course recommendations are **not** persisted as a separate table; they are recomputed on
demand from the saved missing-skills list via the existing `RecommendationService`, to avoid
stale/duplicate data (documented design decision).

---

## 4. Dependencies Added (pom.xml)

| Dependency | Why |
|---|---|
| `spring-boot-starter-security` | Core authentication/authorization framework |
| `io.jsonwebtoken:jjwt-api/impl/jackson` (0.11.5) | JWT generation & validation |
| `org.apache.tika:tika-core` + `tika-parsers-standard-package` (2.9.2) | Extracts text from uploaded PDF/DOC/DOCX resumes |
| `org.jsoup:jsoup` (1.17.2) | HTTP client used by `LiveJobDataProvider` to fetch live job data |
| `spring-boot-starter-validation` | `@Valid`/`@NotBlank`/`@Email` on RegisterRequest/LoginRequest |
| `org.thymeleaf.extras:thymeleaf-extras-springsecurity6` | Lets templates show/hide nav links based on login state (`sec:authorize`) |
| `com.mysql:mysql-connector-j` | Enables the `mysql` Spring profile without extra setup |
| `org.springframework.security:spring-security-test` (test scope) | Supports security-aware tests if extended later |

---

## 5. API / Page Endpoints

| Method | URL | Auth Required | Notes |
|---|---|---|---|
| GET | `/` | No | Original anonymous quick-check flow (unchanged) |
| POST | `/analyze` | No | Original anonymous analysis (unchanged) |
| GET | `/register` | No | Registration form |
| POST | `/register` | No | Creates account, redirects to login |
| GET | `/login` | No | Login form |
| POST | `/login` | No | Validates credentials, sets JWT cookie, redirects to `/dashboard` |
| GET | `/logout` | Yes | Clears JWT cookie |
| GET | `/dashboard` | Yes | Profile + resume status + recent history summary |
| GET | `/resume` | Yes | Resume upload page / current resume info |
| POST | `/resume/upload` | Yes | Multipart upload, Tika parse, skill detection |
| GET | `/skills` | Yes | View/manage saved profile skills |
| POST | `/skills/add` | Yes | Add a skill (form param: `skillName`) |
| POST | `/skills/remove` | Yes | Remove a skill (form param: `skillName`) |
| GET | `/analysis` | Yes | Personalized analysis form (choose role + skill source) |
| POST | `/analysis/run` | Yes | Runs gap analysis, saves to history, shows result |
| GET | `/analysis/history` | Yes | List of past analyses (own only) |
| GET | `/analysis/history/{id}` | Yes | Detail of one past analysis (ownership enforced) |
| GET | `/h2-console` | No (dev only) | H2 database browser, H2 profile only |

**Note on architecture:** these are Spring MVC form-post / Thymeleaf page endpoints, not a
pure JSON REST API — this was a deliberate choice to stay consistent with the rest of this
server-rendered application (see design-decision note below), while still satisfying every
functional requirement (registration, login, JWT issuance/validation, protected pages).

---

## 6. How to Run in IntelliJ IDEA

1. **Unzip** the project folder anywhere.
2. Open IntelliJ → `File → Open` → select the `SkillGapAnalyzer` folder.
3. Click **"Load Maven Project"** when prompted (downloads all dependencies — needs internet
   the first time; this build has noticeably more dependencies than before, so it may take
   a few minutes).
4. `File → Project Structure → Project` → set SDK to **Java 17** (or your installed 21/24 —
   the pom targets `<java.version>17</java.version>`, which is forward-compatible).
5. Open `src/main/java/com/deepali/skillgap/SkillGapApplication.java`.
6. Right-click → **Run 'SkillGapApplication.main()'**.
7. Watch the console for:
   ```
   Skills taxonomy seeded: 39 skills.
   Course catalog seeded: 37 courses.
   Loaded 40 job postings from STATIC dataset.
   Demand analysis computed for roles: [...]
   Tomcat started on port(s): 8080
   ```
8. Visit `http://localhost:8080/` — original anonymous flow still works with no login.
9. Visit `http://localhost:8080/register` to create an account and try the new
   authenticated flow (dashboard → resume upload → skills → analysis → history).

If Maven fails to resolve `tika-parsers-standard-package` or `jjwt-*`, double-check your
internet connection and that IntelliJ's Maven settings aren't pointing at a restricted/offline
repository mirror.

---

## 7. Configuration

### H2 (default — zero setup)
Active via `spring.profiles.active=h2` in `application.properties` (the default).
Config lives in `application-h2.properties`. DB file: `./data/skillgapdb.mv.db`.
Console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/skillgapdb`, user `sa`, no password).

### MySQL
1. `CREATE DATABASE skillgapdb;`
2. Set env vars `MYSQL_USER` / `MYSQL_PASSWORD` (or edit the defaults in
   `application-mysql.properties` for local testing only).
3. Change `spring.profiles.active=mysql` in `application.properties`, or run with
   `--spring.profiles.active=mysql`.

### JWT secret
`jwt.secret` in `application.properties` defaults to a placeholder dev value, but reads from
the `JWT_SECRET` environment variable if set — **never hardcode a real secret**; set
`JWT_SECRET` as an environment variable before any real deployment.

### Live job-data source (optional, off by default)
`app.jobdata.source=static` (default) or `live` — set via `application.properties` or the
`JOBDATA_SOURCE` environment variable. When `live`, the app tries `LiveJobDataProvider`
(a public job-listings JSON endpoint fetched via Jsoup) and **automatically falls back to
the static dataset** if that fails for any reason — network error, timeout, empty/malformed
response. This is off by default so an unreachable external source can never break a demo.

### File upload limits
`spring.servlet.multipart.max-file-size=5MB` / `max-request-size=5MB` in `application.properties`.

---

## 8. Testing

**What was written:** 17 unit tests across 5 test classes, using JUnit 5 + Mockito
(via `spring-boot-starter-test`), covering:
- Skill extraction (matches whole-word skills, rejects partial matches like "Java" inside "Javascript")
- Gap analysis (missing-skill threshold, ranking by demand, cosine-similarity match score at 0% and 100%)
- Auth service (duplicate email rejection, password hashing, wrong-password rejection, successful login token issuance)
- Resume validation (empty file, unsupported extension, oversized file — all rejected before Tika ever runs)
- Cross-user access control (Student A blocked from Student B's saved analysis)

**What was NOT executed:** these tests have **not been run** — `mvn test` was not (and could
not be) executed in the environment this was written in. Please run `mvn test` yourself and
report any failures.

**Not covered by automated tests** (would need either a real PDF/DOCX fixture file or a full
Spring context + MockMvc, both impractical to hand-write blind):
- Actual Tika parsing of a real PDF/DOCX file end-to-end
- JWT filter behavior in a live HTTP request/response cycle
- Live job-data provider against a real network call
- Full registration → login → dashboard browser flow

---

## 9. Remaining Limitations / Design Decisions

- **JWT delivery via HttpOnly cookie, not a pure REST API.** Since this is a server-rendered
  Thymeleaf app (not a JS/SPA frontend), the JWT is stored in an HttpOnly cookie after login so
  normal page navigation stays authenticated, and the same filter also accepts a standard
  `Authorization: Bearer <token>` header for API-style testing (e.g. Postman). This is a
  pragmatic hybrid, not textbook stateless-JWT-for-SPA usage.
- **No server-side token blacklist.** "Logout" clears the cookie client-side; a stolen token
  would technically remain valid until natural expiry (24h by default). A production system
  would add a token-revocation store; out of scope here.
- **Live job-data provider is best-effort and off by default.** It targets one public JSON
  endpoint and does simple keyword-based role inference. It is not a robust, general-purpose
  scraper, and is designed to fail safely (fall back to static data) rather than be fully reliable.
- **No admin panel / no ability to edit the skills taxonomy or course catalog from the UI** —
  both are still seeded from the bundled CSV/txt files at startup, same as the original version.
- **Course recommendations are recomputed, not stored** — see Section 3.
- **Not compiled or run** — see the warning at the top of this document. Expect to spend some
  time fixing build errors; that is anticipated, not a sign that requirements were skipped.

---

## 10. Frontend Redesign (Latest Update)

The entire presentation layer was redesigned into a modern SaaS-style UI. **No backend logic,
controller mappings, model attribute names, form field names, or form actions were changed**
except one small additive change explained below.

### UI files changed (all new or restyled — no logic inside them)
- `static/css/style.css` — completely rewritten as a full design system (colors, typography,
  buttons, cards, navbar, hero, badges, forms, responsive breakpoints)
- `static/js/main.js` — **new**: mobile nav toggle, scroll-reveal animations, password
  show/hide, drag-and-drop resume upload, animated stat counters, button loading states.
  Pure presentation-layer JS; it never changes a form's action, method, or field names.
- `templates/fragments/nav.html` — redesigned navbar with logo mark, mobile hamburger menu
- `templates/fragments/footer.html` — **new**: site footer fragment
- `templates/index.html` — completely redesigned: hero, stats, features, how-it-works,
  career-roles, and the **original anonymous quick-check form is preserved exactly**
  (`action="/analyze"`, field names `role` and `selectedSkills` unchanged) inside a
  "Quick Skill Check" section
- `templates/login.html`, `templates/register.html` — split-screen auth layout;
  `th:object`/`th:field` bindings to `loginRequest`/`registerRequest` unchanged
- `templates/dashboard.html` — KPI cards + redesigned cards; all model attributes
  (`profile`, `hasResume`, `recentAnalyses`, `totalAnalyses`) unchanged
- `templates/resume-upload.html` — drag-and-drop dropzone; form action `/resume/upload`,
  `enctype="multipart/form-data"`, input `name="file"` all unchanged
- `templates/skills-manage.html` — skill badges with inline remove buttons; form actions
  `/skills/add` and `/skills/remove` with `skillName` param unchanged
- `templates/analysis-form.html` — restyled; `role` and `skillSource` fields unchanged
- `templates/result.html` — match-score hero ring, restyled Chart.js bar chart (same
  underlying data-binding via `result.entries.![skillName]`/`.![demandPercent]`), skill
  badges, course cards; all model attributes unchanged
- `templates/analysis-history.html`, `templates/analysis-history-detail.html` — restyled
  tables/cards; model attributes unchanged
- `templates/error.html` — restyled empty-state style error page

### Backend files changed (and exactly why)
- `controller/HomeController.java` — **one additive change only**: added four new model
  attributes (`jobPostingCount`, `skillCount`, `courseCount`, `roleCount`) to the existing
  `index()` method, sourced from `JobMarketDataService`, `SkillRepository`, and
  `CourseRepository` (all already-existing beans). This was necessary so the landing page's
  statistics section could show **real, database-driven numbers** instead of inventing
  figures, per the redesign brief's explicit instruction. No existing attribute, return
  type, or mapping was touched. Actual current values: 40 job postings, 40 skills,
  39 courses, 4 roles.

No other backend file (service, repository, entity, security config) was modified.

### New dependencies
**None.** The redesign uses only what was already on the page: vanilla CSS, vanilla
JavaScript, Google Fonts (loaded via `@import` in the CSS, same pattern as any external
font), and the Chart.js CDN script tag that was already present in `result.html`.

### Features preserved (verified by re-reading each controller/service after every template edit)
- Anonymous quick-check flow (`/`, `/analyze`) — form field names and action untouched
- Login / Registration — DTO bindings (`loginRequest`, `registerRequest`) untouched
- JWT authentication + protected routes — `SecurityConfig` untouched
- Resume upload + parsing — `/resume/upload`, `multipart/form-data`, field `file` untouched
- Skill management — `/skills/add`, `/skills/remove`, field `skillName` untouched
- Personalized analysis — `/analysis`, `/analysis/run`, fields `role`/`skillSource` untouched
- Analysis history — `/analysis/history`, `/analysis/history/{id}` untouched
- Cosine-similarity match score, demand calculation, course recommendation logic — untouched
- Chart.js visualization — same data source (`result.entries`), same inline-JS binding
- H2 / MySQL profile switching — untouched

### How to run
Same as before — no new setup steps:
1. Unzip → open in IntelliJ → let Maven load dependencies.
2. Run `SkillGapApplication.main()`.
3. Open `http://localhost:8080/` and you should now see the redesigned landing page.

**Same compilation caveat as the previous update applies: this was not compiled or run in
the environment it was written in.** Template syntax (Thymeleaf expressions, `th:block`
grouping, conditional `th:text` expressions) was manually reviewed and simplified anywhere
a pattern looked risky, but please run it and report the first error you hit.
