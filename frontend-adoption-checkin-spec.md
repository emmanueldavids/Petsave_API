# PetSave Adoption Follow-up Frontend Spec

## Overview
This document describes the frontend workflow, API endpoints, request/response shapes, and UI behavior for Adoption Welfare Check-ins.

The feature tracks pet welfare at milestone intervals after adoption completion and supports:
- automated email reminders
- adopter health check-in submissions with optional photo upload
- admin alerts for missed check-ins

## Milestones
Check-ins are created for each completed adoption at these milestones:
- 1 week
- 1 month
- 3 months
- 6 months
- 1 year

## API Base Path
All endpoints use the base path:
- `/api/adoptions`

Authentication should use the existing application auth mechanism (JWT bearer token or current session cookie).

---
## Endpoints

### 1. Get all check-ins for an adoption

- Method: `GET`
- URL: `/api/adoptions/{adoptionId}/check-ins`
- Description: Returns all check-ins for the requested adoption, ordered by due date.
- Response: JSON

#### Sample response
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "adoptionId": 42,
      "petName": "Milo",
      "milestone": "ONE_WEEK",
      "status": "PENDING",
      "healthStatus": "NOT_SUBMITTED",
      "dueDate": "2026-07-01T12:00:00",
      "submittedAt": null,
      "adopterNotes": null,
      "photoUrl": null,
      "createdAt": "2026-06-24T12:00:00",
      "updatedAt": "2026-06-24T12:00:00",
      "isOverdue": false,
      "hoursUntilDue": 168
    }
  ],
  "count": 1
}
```

### 2. Get single check-in details

- Method: `GET`
- URL: `/api/adoptions/check-ins/{checkInId}`
- Description: Returns details for one check-in.
- Response: JSON

#### Sample response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "adoptionId": 42,
    "petName": "Milo",
    "milestone": "ONE_WEEK",
    "status": "PENDING",
    "healthStatus": "NOT_SUBMITTED",
    "dueDate": "2026-07-01T12:00:00",
    "submittedAt": null,
    "adopterNotes": null,
    "photoUrl": null,
    "createdAt": "2026-06-24T12:00:00",
    "updatedAt": "2026-06-24T12:00:00",
    "isOverdue": false,
    "hoursUntilDue": 168
  }
}
```

### 3. Submit a check-in

- Method: `POST`
- URL: `/api/adoptions/{adoptionId}/check-ins/{checkInId}/submit`
- Request type: `multipart/form-data`
- Description: Submit adopter notes, health status, and an optional photo.

#### Form fields
- `adopterNotes` (string)
- `healthStatus` (string, required) — allowed values:
  - `HEALTHY`
  - `NEEDS_VET`
  - `CONCERNS`
- `photo` (file, optional)

#### Success response
```json
{
  "success": true,
  "message": "Check-in submitted successfully",
  "data": {
    "id": 1,
    "adoptionId": 42,
    "petName": "Milo",
    "milestone": "ONE_WEEK",
    "status": "SUBMITTED",
    "healthStatus": "HEALTHY",
    "dueDate": "2026-07-01T12:00:00",
    "submittedAt": "2026-06-30T14:23:00",
    "adopterNotes": "Milo is doing great and has been playful.",
    "photoUrl": "https://...",
    "createdAt": "2026-06-24T12:00:00",
    "updatedAt": "2026-06-30T14:23:00",
    "isOverdue": false,
    "hoursUntilDue": -22
  }
}
```

#### Error responses
- `400 Bad Request` for invalid request or invalid `healthStatus`
- `500 Internal Server Error` for upload errors

---
## Admin endpoints

### 4. Get missed check-in alerts

- Method: `GET`
- URL: `/api/adoptions/admin/check-ins/alerts`
- Description: Returns check-ins that are overdue by at least 72 hours and require admin attention.
- Response: JSON

#### Sample response
```json
{
  "success": true,
  "alerts": [
    {
      "checkInId": 5,
      "adoptionId": 42,
      "adopterName": "Jane Doe",
      "adopterEmail": "jane@example.com",
      "petName": "Milo",
      "milestone": "ONE_MONTH",
      "dueDate": "2026-08-01T12:00:00",
      "overdueSinceDate": "2026-08-04T12:00:00",
      "hoursOverdue": 80
    }
  ],
  "count": 1
}
```

### 5. Get check-in stats

- Method: `GET`
- URL: `/api/adoptions/{adoptionId}/check-ins/stats`
- Description: Returns simple stats for check-ins belonging to an adoption.
- Response: JSON

#### Sample response
```json
{
  "success": true,
  "data": {
    "completed": 2,
    "pending": 3,
    "total": 5
  }
}
```

---
## UI Requirements

### Adopter experience
1. **Dashboard or adoption detail page** should display a list of upcoming and completed check-ins.
2. Each check-in should show:
   - milestone label (`1 week`, `1 month`, etc.)
   - due date
   - current status (`PENDING`, `SUBMITTED`, `OVERDUE`)
   - optional note or photo preview after submission
3. For pending check-ins, show a button/link to open the submission form.
4. The form should include:
   - text area: `How is your pet doing?` → `adopterNotes`
   - radio/select: `Health status` with values `HEALTHY`, `NEEDS_VET`, `CONCERNS`
   - photo upload input: `Upload a photo`
5. After successful submission, show a confirmation message and optionally a link back to the adoption summary.

### Admin experience
1. Provide a page to show missed alerts from `/api/adoptions/admin/check-ins/alerts`.
2. Each alert row should include:
   - adopter name and email
   - pet name
   - milestone missed
   - due date
   - hours overdue
3. Provide a way to quickly navigate to the adoption record or contact adopter.

---
## Frontend integration notes

### Auth
- Requests should include authorization headers using your existing app auth approach.
- Admin endpoint should be restricted to admin users; backend currently has a `TODO` note for role-based guard.

### Form submission details
- Use `multipart/form-data` for photo uploads.
- Field names must match backend names exactly:
  - `adopterNotes`
  - `healthStatus`
  - `photo`

### Health statuses
Frontend should send one of these exact values:
- `HEALTHY`
- `NEEDS_VET`
- `CONCERNS`

### Photo handling
- The backend returns `photoUrl` in the response.
- Display the image if the value is returned.

---
## Recommended UI flow

### Adopter flow
1. User logs in and opens their adoption history or adoption detail page.
2. The app calls `GET /api/adoptions/{adoptionId}/check-ins`.
3. Pending check-ins appear with a prompt like:
   - `Week 1 check-in due in 3 days`
   - `Month 1 check-in overdue`
4. User clicks `Submit check-in`.
5. User fills notes, selects health status, optionally uploads a photo.
6. App posts to `/api/adoptions/{adoptionId}/check-ins/{checkInId}/submit`.
7. App displays success or validation error.

### Admin flow
1. Admin opens a missed check-ins dashboard.
2. The app calls `GET /api/adoptions/admin/check-ins/alerts`.
3. Admin sees overdue check-ins and can follow up.

---
## UX copy suggestions

### Reminder card
- Title: `Time for your pet’s check-in`
- Text: `Please submit a quick health update and photo for your pet. This helps us make sure every adoption stays successful.`
- Button: `Submit check-in`

### Submission form
- `How is your pet feeling today?`
- `Upload a photo (optional)`
- `Submit update`

### Confirmation
- `Thank you! Your check-in has been received.`
- `A member of the PetSave team will review your update.`

---
## Notes for frontend handoff
- This feature is built around the `AdoptionCheckIn` backend entity.
- The check-in list is based on the adoption ID.
- The `/submit` endpoint requires `multipart/form-data`.
- Admin endpoint currently returns alerts without a role check inside the controller.

If you want, I can also add a small example React form component spec with field structure and API calls.