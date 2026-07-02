# Pet Rehoming Frontend Handoff

## Overview

This feature allows pet owners to submit pets for rehoming, lets approved applicants apply, and gives the owner a way to review and select a new home for the pet.

Base URL:
- `http://localhost:8080/api/rehomings`

## Authentication

- Public reads:
  - `GET /api/rehomings`
  - `GET /api/rehomings/{id}`
- Authenticated actions:
  - Create, update, withdraw, apply, view applications, approve applicant
  - `GET /api/rehomings/my` (owner's own listings)
  - `GET /api/rehomings/my-applications` (applicant's own submitted applications)
- Admin-only actions:
  - Change listing status through `PATCH /api/rehomings/{id}/status`
  - `GET /api/rehomings/admin` (review queue)

## Main Data Shapes

### PetRehoming

```json
{
  "id": 1,
  "owner": {
    "id": 7,
    "name": "Ada",
    "email": "ada@example.com"
  },
  "petName": "Buddy",
  "petType": "DOG",
  "petBreed": "Labrador",
  "petAge": 3,
  "description": "Friendly and house-trained",
  "imageUrl": "https://example.com/pet.jpg",
  "reason": "Moving abroad",
  "medicalHistory": "Vaccinated, no known issues",
  "vaccinated": true,
  "neutered": true,
  "temperament": "Gentle",
  "goodWith": "Children and other pets",
  "specialNeeds": "None",
  "status": "PENDING_REVIEW",
  "approvedBy": null,
  "adoptedBy": null,
  "createdAt": "2026-07-01T10:00:00",
  "updatedAt": "2026-07-01T10:00:00"
}
```

`owner`, `approvedBy`, and `adoptedBy` are all trimmed `User` objects — sensitive fields (`password`, `verificationCode`, `refreshToken`, `resetCode`, etc.) are stripped server-side on all three, so it's safe to log or display any of them. `approvedBy` is set to the admin who approved/rejected the listing; `adoptedBy` is set to the applicant who was accepted, once one is approved (see section 9).

### RehomingApplication

```json
{
  "id": 10,
  "rehoming": {
    "id": 1
  },
  "applicant": {
    "id": 12,
    "name": "John",
    "email": "john@example.com"
  },
  "applicationReason": "I have a large yard and experience with dogs",
  "homeDescription": "Spacious apartment with a fenced balcony",
  "hasOtherPets": true,
  "hasChildren": false,
  "status": "PENDING",
  "createdAt": "2026-07-01T10:15:00"
}
```

## API Contract

### 1) Browse rehoming listings

#### Request
- `GET /api/rehomings`

#### Response
- Array of approved rehoming listings.

#### Frontend behavior
- Show cards on the rehoming marketplace page.
- Each card should show pet name, type, breed, age, location-style description, and image.
- Clicking a card opens the detail page.

### 2) View a rehoming detail

#### Request
- `GET /api/rehomings/{id}`

#### Response
- Full rehoming details.

#### Frontend behavior
- Show the full pet profile, medical history, reason for rehoming, temperament, and application CTA.

### 3) Submit a pet for rehoming

#### Request
- `POST /api/rehomings`

Body example:

```json
{
  "petName": "Milo",
  "petType": "CAT",
  "petBreed": "Siamese",
  "petAge": 2,
  "description": "Very affectionate and calm",
  "imageUrl": "https://example.com/milo.jpg",
  "reason": "Owner is moving",
  "medicalHistory": "Vaccinated and dewormed",
  "vaccinated": true,
  "neutered": true,
  "temperament": "Friendly",
  "goodWith": "Children",
  "specialNeeds": "None"
}
```

#### Response
- Created rehoming record with status `PENDING_REVIEW`.

#### Frontend behavior
- Show a success screen after submission.
- Redirect the owner to their “My Rehomings” page.

### 4) Update a rehoming listing

#### Request
- `PUT /api/rehomings/{id}`

#### Frontend behavior
- Use this for full edits of an existing listing.
- Allow the owner to edit all fields before the listing is approved.

### 5) Withdraw a rehoming listing

#### Request
- `DELETE /api/rehomings/{id}`

#### Frontend behavior
- Show a confirmation dialog before withdrawing.
- Update the owner’s dashboard state to reflect the withdrawn listing.

### 6) Admin review / status approval

#### Request
- `PATCH /api/rehomings/{id}/status`

Body example:

```json
{
  "status": "APPROVED"
}
```

Supported statuses:
- `DRAFT`
- `PENDING_REVIEW`
- `APPROVED`
- `ADOPTED`
- `REJECTED`
- `WITHDRAWN`

#### Frontend behavior
- Admin screens should show a review queue.
- After approval, the listing becomes visible publicly.
- After rejection, show a clear rejected state.

### 6b) Admin review queue

#### Request
- `GET /api/rehomings/admin?status=PENDING_REVIEW`

`status` is optional:
- Omitted → defaults to `PENDING_REVIEW` (the actual "needs action" queue — use this for the main admin dashboard view).
- Any valid status (`APPROVED`, `ADOPTED`, `REJECTED`, `WITHDRAWN`, `DRAFT`) → filters to that status.
- `ALL` → every rehoming regardless of status, for an audit/history view.
- Anything else → `400` with `"Invalid status: {value}"`.

Admin-only. Non-admins get `400` with `"Only admins can view the rehoming review queue"`. Unauthenticated requests get a plain `403` (rejected before reaching app logic).

#### Frontend behavior
- Use this to populate the Admin Review Queue page instead of guessing IDs — this was previously the missing piece: admins had no way to discover which listings were awaiting review.
- Filter to `status=ADOPTED` to build an "adoptions history" view showing which applicant got each pet (via `adoptedBy` on each listing).

### 7) Apply to adopt a rehomed pet

#### Request
- `POST /api/rehomings/{id}/apply`

#### Response
- Creates a pending application.

#### Frontend behavior
- Show a “Apply Now” CTA on the detail page for authenticated users.
- If the user is the owner, block the action.
- After success, show a confirmation state and disable duplicate submission.

### 8) View applications for a rehoming listing

#### Request
- `GET /api/rehomings/{id}/applications`

#### Response
- Array of `RehomingApplication` objects. Each has its own `id` — **this is the value you pass to the approve endpoint below, not the applicant's user id.**

#### Frontend behavior
- Only the owner should see this view.
- Show each applicant with their application reason and household details.

### 9) Approve an applicant

#### Request
- `PATCH /api/rehomings/{id}/approve/{applicationId}`

`{applicationId}` is the `id` field from the `RehomingApplication` object returned by section 8 (`GET /api/rehomings/{id}/applications`) — **not** the applicant's user id. (The URL segment used to be named `{userId}` in an earlier version of this doc/code, which was misleading; it always meant the application id.)

#### Response
- The approved `RehomingApplication`, now with `status: "APPROVED"`.

#### Frontend behavior
- The owner selects one applicant and confirms the handoff.
- After approval, the listing should show an `ADOPTED` state and the selected applicant should be highlighted (`adoptedBy` on the `PetRehoming`).
- **All other pending applications for that listing are automatically rejected server-side** — no separate call needed. If you're polling `GET /api/rehomings/{id}/applications` after approving, the other applicants will already show `status: "REJECTED"`.

### 10) Track your own applications (applicant view)

#### Request
- `GET /api/rehomings/my-applications`

#### Response
- Array of the current user's own `RehomingApplication` objects, across every listing they've applied to, newest first. Each includes the nested `rehoming` (with `owner`/`approvedBy`/`adoptedBy` stripped out of that nested object to keep the payload light — fetch `GET /api/rehomings/{id}` separately if you need the full listing).

#### Frontend behavior
- Build a "My Applications" page/tab so applicants can see the status of everything they've applied for: `PENDING` (still waiting), `APPROVED` (they got it), or `REJECTED` (someone else was chosen, or the owner didn't pick them).
- This is a pull-based check — there's no push notification yet when status changes, so poll or refetch on page focus.

## Suggested UI States

### Listing states
- `DRAFT` → owner is still editing
- `PENDING_REVIEW` → waiting for admin review
- `APPROVED` → public and open for applications
- `ADOPTED` → owner selected an applicant
- `REJECTED` → admin rejected the listing
- `WITHDRAWN` → owner removed the listing

### Application states
- `PENDING` → waiting for owner decision
- `APPROVED` → selected by owner
- `REJECTED` → not selected (either the owner explicitly wasn't going to pick them, or another applicant was approved for the same listing — both cases land here, the API doesn't distinguish them)

## Recommended Frontend Pages

- Rehoming Marketplace
- Rehoming Detail Page
- Submit Rehoming Form
- My Rehomings Dashboard
- Rehoming Applications Page (owner side, per listing)
- My Applications Page (applicant side, across all listings — powered by `GET /api/rehomings/my-applications`)
- Admin Review Queue (powered by `GET /api/rehomings/admin`)

## Important UX Notes

- Public users should be able to browse but not submit or apply unless authenticated.
- Owners should see their own listings with editable status and application management, including who got adopted (`adoptedBy`) once a listing reaches `ADOPTED`.
- Applicants should see their application status clearly after submission, and should be able to check back later via the My Applications page — approving one applicant auto-rejects the rest, so a `REJECTED` status can appear without the applicant taking any action.
- Empty states should be friendly and explicit for “no listings”, “no applications”, and “no matches yet”.

## Error Handling

Common frontend handling should include:
- Show a friendly error banner for failed create/update/apply actions.
- Disable buttons while the request is pending.
- Redirect or show a login prompt for unauthenticated users trying to submit or apply.
- Admin-only endpoints (`GET /api/rehomings/admin`, `PATCH /api/rehomings/{id}/status`) return `400` with a specific message (e.g. `"Only admins can view the rehoming review queue"`) for non-admins who are otherwise authenticated — check `message`, not just status code, to distinguish this from other validation errors. Truly unauthenticated requests get a plain `403` instead.
