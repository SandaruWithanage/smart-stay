# Export guide for the CI6115 coursework report

The repository contains the **source**, design/test documentation, and scripts to prepare an assessed submission, but it does not automatically constitute the final moderated hand-in.

## UML diagram export
1. Open `docs/COURSEWORK-CLASS-DIAGRAM.md` and replace every `KINGSTON_ID_` prefix with your **actual** Kingston University student ID. Check all class names, not just the title.
2. Preview the Mermaid diagrams in a Mermaid-compatible Markdown viewer such as GitHub and export high-resolution SVG/PNG to include in the report. Check arrow direction, multiplicities and readable lettering.
3. Check the diagram against the current source and describe which parts are domain records, persistent entities, services and repository abstractions.

## Generate code appendix
From the repo root, on Windows PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Export-Code-Listings.ps1
```

Output: `submission/SmartStay-Code-Listings.md`. This includes committed backend Java source and tests, migration SQL and frontend TS/TSX/CSS. Always check that your final coursework requires all these listings and whether its prescribed format accepts appendices. Generated output is not committed.

## Source ZIP
Run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Create-Submission-Zip.ps1
```

Output: `submission/SmartStay-source.zip`; contains `HEAD` tracked files only. Do not include live `.env`, password/token values or personal guest data. Review archive content before uploading.

## Report screenshots
Run the Java/Next.js application and capture *real* working screen shots. The Stitch images and any illustration-only mockups are UI references, not evidence of executed functionality. The required screenshot list and test execution template are in `COURSEWORK-SUBMISSION-CHECKLIST.md` and `COURSEWORK-TEST-CASE-MATRIX.md`.

## Final hand-in
Follow the **final moderated** brief for submission rules and deadline. The source draft describes a single report/document plus a separate ZIP, and warns that the diagram class names need a Kingston student-ID prefix. Do not submit the placeholder diagram, a standalone SRS, or a source ZIP alone as the full coursework response.

## Automated GitHub Actions source archive

Every CI run also executes the `coursework-exports` job. Once that job passes, open **Actions → SmartStay CI → the successful run → Artifacts** and download `smartstay-coursework-source-<commit SHA>`. The archive contains a clean source ZIP and generated Markdown code listings. These generated artifacts have a short retention period. They do **not** include the final student-specific report, screenshots or approved UML.
