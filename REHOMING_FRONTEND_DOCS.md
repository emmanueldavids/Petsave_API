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
- Admin-only action:
  - Change listing status through `PATCH /api/rehomings/{id}/status`

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

#### Frontend behavior
- Only the owner should see this view.
- Show each applicant with their application reason and household details.

### 9) Approve an applicant

#### Request
- `PATCH /api/rehomings/{id}/approve/{userId}`

#### Frontend behavior
- The owner selects one applicant and confirms the handoff.
- After approval, the listing should show an `ADOPTED` state and the selected applicant should be highlighted.

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
- `REJECTED` → not selected

## Recommended Frontend Pages

- Rehoming Marketplace
- Rehoming Detail Page
- Submit Rehoming Form
- My Rehomings Dashboard
- Rehoming Applications Page
- Admin Review Queue

## Important UX Notes

- Public users should be able to browse but not submit or apply unless authenticated.
- Owners should see their own listings with editable status and application management.
- Applicants should see their application status clearly after submission.
- Empty states should be friendly and explicit for “no listings”, “no applications”, and “no matches yet”.

## Error Handling

Common frontend handling should include:
- Show a friendly error banner for failed create/update/apply actions.
- Disable buttons while the request is pending.
- Redirect or show a login prompt for unauthenticated users trying to submit or apply.
