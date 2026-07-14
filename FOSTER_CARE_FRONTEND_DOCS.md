# Formal Foster Care Management Frontend Handoff

## Overview

Formalizes the `FOSTER_CARE` pet status that already existed on the shelter `Pet` entity but previously had no application/approval flow behind it. Users apply to foster a specific pet, admins approve or reject the application, and the foster period is tracked through to completion. Approving an application automatically flips the pet's `status` to `FOSTER_CARE`; completing the foster period flips it back to `FOR_ADOPTION` (unless something else already changed it, e.g. the pet was adopted through the normal adoption flow while in foster care — see below).

This is a from-scratch integration — no existing frontend code for this feature.

Base URL:
- `http://localhost:8080/api/foster/applications`

## Authentication

- Admin-only:
  - `GET /api/foster/applications` (review queue)
  - `PATCH /api/foster/applications/{id}/status` (approve/reject)
  - `PATCH /api/foster/applications/{id}/complete`
- Authenticated (any logged-in user):
  - `POST /api/foster/applications` (apply)
  - `GET /api/foster/applications/my`

There are no public/unauthenticated endpoints on this feature — every route requires a JWT.

## Main Data Shape

### FosterApplicationResponse

```json
{
  "id": 1,
  "applicant": { "id": 7, "name": "Foster Tester", "email": "fostertester1@example.com" },
  "pet": {
    "id": 3,
    "name": "FosterTestPet",
    "type": "DOG",
    "breed": "Mixed",
    "imageUrl": "https://...",
    "status": "FOSTER_CARE"
  },
  "startDate": "2026-08-01",
  "expectedEndDate": "2026-09-01",
  "applicationReason": "I want to help while it awaits adoption",
  "homeDescription": "Fenced backyard, no other pets",
  "hasExperience": true,
  "experienceDetails": "Had dogs for 10 years",
  "status": "APPROVED",
  "adminNotes": "Home check passed",
  "createdAt": "2026-07-13 20:43:54",
  "updatedAt": "2026-07-13 20:43:54"
}
```

- `pet` is a lightweight summary (id/name/type/breed/imageUrl/status) of the shelter `Pet` being fostered — not the full pet record. Fetch `GET /api/pets/{id}` separately if you need the rest (description, medical history, etc).
- `pet.status` reflects the **live** current status of the pet, which this feature actively changes (see status flow below) — it's not frozen at application time.
- `startDate`/`expectedEndDate` are plain dates (`yyyy-MM-dd`), no time component.
- `adminNotes` is `null`/absent until an admin sets one via the status-update endpoint.

## Status Flow

```
PENDING --(admin approves)--> APPROVED --(admin, once underway)--> ACTIVE --(admin completes)--> COMPLETED
   |
   +--(admin rejects)--> REJECTED
```

- `PENDING`: initial state on application.
- `APPROVED`: admin has approved the application. **Side effect: the pet's `status` is set to `FOSTER_CARE`.**
- `ACTIVE`: admin has marked the foster period as actually underway (e.g. once `startDate` arrives). Also sets the pet to `FOSTER_CARE` if not already (idempotent).
- `REJECTED`: terminal, no side effects on the pet.
- `COMPLETED`: terminal, set only via the dedicated `/complete` endpoint (not via `/status` — see below). **Side effect: if the pet's status is still `FOSTER_CARE`, it's reverted to `FOR_ADOPTION`.** If the pet was adopted by the foster family (or anything else) while fostered, its status will have already moved on from `FOSTER_CARE` through the normal adoption flow, so this reset is skipped — completion never clobbers a more advanced state.

## API Contract

### 1) Apply to foster a pet

- `POST /api/foster/applications`

```json
{
  "petId": 3,
  "startDate": "2026-08-01",
  "expectedEndDate": "2026-09-01",
  "applicationReason": "I want to help while it awaits adoption",
  "homeDescription": "Fenced backyard, no other pets",
  "hasExperience": true,
  "experienceDetails": "Had dogs for 10 years"
}
```

- Required: `petId`, `startDate`, `expectedEndDate`, `applicationReason`, `homeDescription`. `hasExperience`/`experienceDetails` optional.
- `400` with `"Pet not found with id: {petId}"` if the pet doesn't exist.
- **One open application per pet per user** — if you already have a `PENDING`, `APPROVED`, or `ACTIVE` application for the same pet, a second attempt returns `400` with `"You already have an open foster application for this pet"`. A prior `REJECTED` or `COMPLETED` application doesn't block a fresh one (e.g. re-applying for a later foster period after completing one).
- Always starts at `status: "PENDING"`.

### 2) My applications

- `GET /api/foster/applications/my`
- All of the current user's applications regardless of status, newest first.

### 3) Admin: review queue

- `GET /api/foster/applications?status=PENDING`
- `status` is optional:
  - Omitted → defaults to `PENDING` (the actual review queue).
  - Any valid status (`APPROVED`, `ACTIVE`, `COMPLETED`, `REJECTED`) → filters to that status.
  - `ALL` → every application regardless of status, for an audit view.
  - Anything else → `400` with `"Invalid status: {value}"`.
- Admin-only — `400` with `"Only admins can view foster applications"` for non-admins.

### 4) Admin: approve or reject

- `PATCH /api/foster/applications/{id}/status`

```json
{ "status": "APPROVED", "adminNotes": "Home check passed" }
```

- `adminNotes` is optional; when provided (non-blank), it's saved onto the application.
- Valid `status` values here: `PENDING | APPROVED | ACTIVE | REJECTED`. **`COMPLETED` is deliberately rejected** — `400` with `"Use the complete endpoint to mark a foster period complete"` — so the pet-status-reset side effect only ever happens through the dedicated endpoint (section 5), never accidentally via a generic status PATCH.
- `400` with `"A completed foster application cannot change status"` if the application is already `COMPLETED`.
- Admin-only — `400` with `"Only admins can change a foster application's status"` for non-admins.

### 5) Admin: mark foster period complete

- `PATCH /api/foster/applications/{id}/complete`
- No body needed.
- `400` with `"A foster application that is {status} cannot be completed"` if it's already `COMPLETED` or `REJECTED`.
- Admin-only — `400` with `"Only admins can mark a foster period complete"` for non-admins.
- This is the only path that sets `status: COMPLETED` and the only path that reverts the pet's status back to `FOR_ADOPTION` (conditionally, see Status Flow above).

## Suggested UI States

- `PENDING` → "Awaiting review" on the applicant's side; appears in the admin review queue.
- `APPROVED` → "Approved — foster starts {startDate}"; pet badge shows "In Foster Care".
- `ACTIVE` → "Currently fostering"; same pet badge as `APPROVED`, distinguish with a "since {startDate}" note if useful.
- `REJECTED` → show `adminNotes` if present as the reason.
- `COMPLETED` → "Foster period complete — thank you!"; pet badge reflects its current (possibly changed) status again.
