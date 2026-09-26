---
name: review-plugin
description: Guides reviewing, testing, and merge-checking a third-party plugin submission or update pull request against the gephi-plugins repo, from checking out the contributor's fork branch through a final disposable-worktree merge into master-forge to catch pom.xml <modules>-list conflicts before they're real. Enforces this repo's three-branch model so master-forge and master are never pushed to directly, and gates every GitHub-visible or fork-visible action (pushes, PR comments/reviews, merges) on explicit user approval. Use whenever asked to review, test, fix, or merge-check a gephi-plugins pull request; when given a github.com/gephi/gephi-plugins PR URL or number; or when asked to resolve a plugin's pom.xml conflicts against master-forge, check whether a plugin update still builds, or work through a plugin-submission checklist — even if the user doesn't mention branches, forks, or worktrees explicitly.
---

# Reviewing a gephi-plugins PR

This skill is the *process* for reviewing a plugin PR: which branch to stand on, when you're allowed
to push versus only allowed to suggest, and how to rehearse the merge before it's real. It
deliberately does not restate *what* to check in the code — that content already lives in this
repo's own docs and goes stale if duplicated here. Read from these as the relevant phase below calls
for them, don't summarize them in advance:

| Doc | What's there |
|---|---|
| `CONTRIBUTING.md` → "Reviewing a plugin submission PR" | The actual review checklist (build, pom.xml, manifest, SPI correctness, licensing, file hygiene, tests, PR completeness) |
| `CONTRIBUTING.md` → "Documenting a plugin in master-forge's `<modules>` list" | Exact format for the name/origin/status comment block a new `<module>` entry needs |
| `CONTRIBUTING.md` → "Maintaining a plugin after approval" | The unresponsive-fork/branch-adoption exception — a separate maintainer decision, out of scope for a routine review (see Guardrails) |
| `ARCHITECTURE.md` → "Repository / branch model" | What `master` / `master-forge` / `parent-pom` are each for, and why |
| `README.md` → "Submit a plugin" / "Update a plugin" | The fork+PR flow from the contributor's side, including "allow edits from maintainers" |
| `AGENTS.md` | Build/test commands (`mvn clean package`, single-module builds, etc.) |

## Guardrails — read before doing anything

This is someone else's code, on a public OSS project, and the repo has a branch model that's easy to
violate by accident. A few rules, and the reasoning behind each:

- **Never push to `master` or `master-forge` directly, ever — including "just to test."**
  `master-forge` aggregates every plugin into one build; `build.yml` (main CI) explicitly excludes it
  from its triggers, so a direct push skips CI entirely and there's no PR to attribute the change to.
  The only legitimate way something lands on `master-forge` is a merged PR.
- **Your own fix commits belong on the contributor's fork branch, not a branch in this repo.** When
  you have push access (see "Access check" below), you're pushing to *their* branch on *their* fork —
  the same branch the PR is already open from — not creating something new here. That's a different
  thing from the "never push to master/master-forge" rule above; don't let the two blur together.
  `gh pr checkout` sets this up correctly by itself.
- **The branch-adoption exception (`CONTRIBUTING.md`'s "When a plugin's fork stops being a reliable
  source of truth") is not part of a routine review.** If a review surfaces an unresponsive
  maintainer, flag it and stop — adopting a plugin onto a maintained branch is a separate, deliberate
  decision the user makes explicitly, not something to fold into fixing a PR.
- **Prefer suggesting over rewriting.** Even with push access, a substantial rewrite of someone else's
  logic isn't yours to make unilaterally — reserve direct pushes for mechanical fixes (a version bump,
  a pom.xml conflict resolution, a checklist item like a missing license file) and leave anything
  touching the plugin's actual behavior as a review comment for the contributor to act on.
- **Confirm with the user before anything that leaves your machine or is hard to undo**: pushing a
  commit anywhere (including to the fork, even with access), posting a PR comment or review, approving
  or requesting changes, or merging. Checking out, building, and the worktree merge rehearsal in Phase
  2 are local and disposable, so you can move through those without stopping at every step — but say
  what you're about to do before you build code from someone's fork, since that means running their
  Maven/Java code on this machine, not a sandboxed CI runner.
- **The Phase 2 worktree merge is a rehearsal, never a delivery.** Nothing from it gets pushed anywhere
  under any circumstances — it exists purely to prove the PR *would* merge cleanly (or to work out
  what the conflict resolution should look like) and gets deleted afterward win or lose.

## Access check — which mode are you in?

Before touching anything, find out whether you can actually push fixes or only suggest them:

```
gh pr view <N> --repo gephi/gephi-plugins --json baseRefName,headRefName,headRepositoryOwner,headRepository,maintainerCanModify,mergeable
```

- `baseRefName` should be `master-forge`. If it's `master` instead, that's itself a review finding
  (see `CONTRIBUTING.md`'s "PR completeness" section) — flag it to the user before going further,
  since the rest of this workflow assumes the PR targets `master-forge`.
- `maintainerCanModify: true` means the contributor enabled "allow edits from maintainers" and you can
  push fix commits to their branch. `false` means **comment-only mode**: you can build, test, and work
  through the checklist locally, but any fix becomes a suggestion (a review comment, or a `gh pr
  review` with a diff suggestion) rather than something you push.
- `mergeable: CONFLICTING` is common and expected — it usually just means `master-forge`'s
  `<modules>` list has moved since the fork branched off. That's exactly what Phase 2 works out.
- Tell the user which mode you're in and what `mergeable` says before starting Phase 1, so they're not
  surprised later when a fix turns into "here's a comment" instead of "here's a push."

## Phase 1 — Isolated review and test

Work entirely on the contributor's own branch here; nothing in this phase involves `master-forge`.

1. `gh pr checkout <N>` — this fetches the fork, adds a remote for it if needed, and checks out a
   local branch tracking the contributor's branch directly. This *is* "the source branch" the plugin's
   commits live in — anything you commit here, if pushed, goes back to their fork.
2. Work through `CONTRIBUTING.md`'s "Reviewing a plugin submission PR" checklist against this branch,
   including actually running `mvn clean package` from the repo root per `AGENTS.md` rather than
   trusting a green PR description. Since only this contributor's plugin(s) are added to `pom.xml` on
   their branch, this build is naturally isolated from every other plugin.
3. Report findings against specific named checklist items, per `CONTRIBUTING.md`'s own instruction —
   not general impressions.
4. For anything you and the user agree is worth fixing directly (see Guardrails on what's in scope for
   a direct push):
   - **`maintainerCanModify: true`**: commit on this branch, confirm the diff with the user, then push
     to the fork's remote (the one `gh pr checkout` set up) — never to a remote or branch in
     `gephi/gephi-plugins` itself.
   - **`maintainerCanModify: false`**: don't commit anything. Draft the fix as a PR comment or a `gh pr
     review` suggestion instead, and only post it once the user says to.

## Phase 2 — Integration check: does it still merge into master-forge?

This is the final gate, and it's a rehearsal — it happens in a disposable worktree outside your normal
checkout, never in the branch you were just reviewing on and never by touching the real
`master-forge` branch.

1. Fetch the current state: `git fetch origin master-forge`.
2. Create a throwaway worktree off it, in your scratchpad directory rather than inside this checkout:
   `git worktree add <scratchpad>/<pr-number>-integration-check origin/master-forge`.
3. In that worktree, merge the branch you reviewed in Phase 1: `git merge <local-pr-branch>`. Expect
   git to report the *entire* `<modules>` list as one conflict block, not a small localized one — it
   can't tell the two sides only touched different entries, since both diverged from the same
   near-empty list on `master`. The resolution is still mechanical: take `master-forge`'s (`HEAD`)
   copy of the list wholesale and add this plugin's own name/origin/status comment block plus
   `<module>` line into it, per `CONTRIBUTING.md`'s documentation-convention section (link above) —
   don't try to hand-merge the conflict markers line by line, and don't improvise the block's format
   from memory.
   - Only treat a conflict *outside* that list as worth surfacing on its own — but check what it
     actually is before calling it a problem: compare against `master-forge`, not `master`. `master`
     (the template) and `master-forge` can legitimately disagree on shared settings like
     `gephi.version` or root build-plugin versions — `master` moving ahead doesn't mean
     `master-forge` is due for the same bump, that's a deliberate, separate maintainer decision (see
     `CONTRIBUTING.md`/`ARCHITECTURE.md` on the branch model). If you see a difference here, flag it
     as a fact for the user to weigh in on rather than resolving it by picking either side.
4. Build from the worktree. A full `mvn clean package` from the root is the authoritative check — it
   validates and builds *everything* currently on `master-forge`, not just this plugin — but that
   means every other plugin already merged there, so budget real time for it. For a fast first pass,
   `mvn -pl modules/<Folder> -am clean package` proves the plugin itself still builds against the
   merged parent/config without paying for the whole reactor.
5. Report the result (clean merge or not, build passed or not, what the conflict resolution looked
   like, and anything you flagged instead of resolving) to the user. Then remove the worktree
   regardless of outcome — `git worktree remove <path>` (add `--force` if the merge left it dirty) —
   since nothing in it is meant to persist. If the user wants the actual conflict resolution applied
   for real, that's a Phase 1 fix-and-push (or a comment, in comment-only mode) using what you just
   worked out here, not something you push from the worktree itself.

## Wrap-up

Summarize for the user: checklist findings, which mode you were in, whether the integration check
passed, and exactly what (if anything) you pushed or posted and where. If nothing was pushed or
posted, say so explicitly — "reviewed and tested, nothing sent anywhere yet" is a normal, complete
outcome for a comment-only-mode review.
