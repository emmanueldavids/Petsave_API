# Volunteer Management Frontend Handoff

## Overview

Users can register as volunteers with their skills, availability, and location. Admins vet (approve/suspend) volunteers and assign tasks. Completing tasks logs hours and auto-upgrades a badge level, displayed on the volunteer's public profile.

There is no existing frontend code for this feature (no `volunteerAPI`, no components) — this is a from-scratch integration, unlike Community Posts or Rehoming where partial frontend wiring already existed.

Base URL:
- `http://localhost:8080/api/volunteers`

## Authentication

- Public reads:
  - `GET /api/volunteers` (approved/active volunteers only)
  - `GET /api/volunteers/{id}`
  - `GET /api/volunteers/stats`
- Authenticated actions:
  - `POST /api/volunteers` (register)
  - `PUT /api/volunteers/{id}` (update own profile)
  - `GET /api/volunteers/my`
  - `GET /api/volunteers/tasks`
  - `PATCH /api/volunteers/tasks/{id}/complete` (own assigned task, or admin)
- Admin-only:
  - `PATCH /api/volunteers/{id}/status`
  - `POST /api/volunteers/tasks`
  - `GET /api/volunteers/admin` (review queue)
  - `GET /api/volunteers/{id}/tasks` (a specific volunteer's task history)

## Main Data Shapes

### VolunteerResponse

```json
{
  "id": 1,
  "user": { "id": 4, "name": "Existing Owner", "email": "existingowner1@example.com" },
  "skills": ["TRANSPORT", "FEEDING"],
  "availability": "WEEKENDS",
  "location": "Accra",
  "bio": "Happy to help",
  "emergencyContact": "Jane Doe 555-1234",
  "status": "APPROVED",
  "hoursLogged": 7,
  "totalTasksCompleted": 5,
  "badgeLevel": "BRONZE",
  "createdAt": "2026-07-09 18:31:40",
  "updatedAt": "2026-07-09 18:34:10"
}
```

**Privacy note:** `emergencyContact` is only ever populated when the requester is the volunteer themselves or an admin. On the public list (`GET /api/volunteers`) and public single-volunteer lookup (`GET /api/volunteers/{id}` as a stranger), the field is simply absent from the JSON (not `null` — omitted entirely). Don't rely on it being present unless you're rendering the logged-in user's own profile or an admin view.

`skills` and `availability`/`status`/`badgeLevel` are plain strings (enum names), not nested objects — match them exactly (`"TRANSPORT"`, not `"Transport"`) when building filter UIs.

### VolunteerTaskResponse

```json
{
  "id": 1,
  "volunteerId": 1,
  "volunteerUser": { "id": 4, "name": "Existing Owner", "email": "existingowner1@example.com" },
  "pet": { "id": 12, "name": "Bella", "imageUrl": "https://..." },
  "title": "Transport Bella to vet",
  "description": "Drive Bella to the vet clinic",
  "taskType": "TRANSPORT",
  "status": "COMPLETED",
  "scheduledDate": "2026-07-10 09:00:00",
  "completedDate": "2026-07-09 18:34:11",
  "hoursSpent": 3,
  "adminNotes": null,
  "createdAt": "2026-07-09 18:34:11"
}
```

`pet` is `null`/absent when the task isn't linked to a specific pet (e.g. a fundraising event task).

### VolunteerStatsResponse

```json
{
  "totalVolunteers": 1,
  "totalHoursLogged": 7,
  "totalTasksCompleted": 5,
  "volunteersByBadgeLevel": { "NONE": 0, "BRONZE": 1, "SILVER": 0, "GOLD": 0, "PLATINUM": 0 }
}
```

`totalVolunteers` only counts `APPROVED`/`ACTIVE` volunteers (matches the public listing), not `PENDING`/`SUSPENDED`/`INACTIVE`.

## API Contract

### 1) List approved volunteers

- `GET /api/volunteers`
- Returns volunteers with `status` in `APPROVED` or `ACTIVE` only — `PENDING`, `SUSPENDED`, and `INACTIVE` volunteers never appear here, regardless of who's asking (even the volunteer themselves should use `GET /api/volunteers/my` to see their own pending profile).

### 2) Get a volunteer by ID

- `GET /api/volunteers/{id}`
- Unlike the list endpoint, this does **not** filter by status — a direct link to a `PENDING` volunteer's profile still resolves. `emergencyContact` is still hidden unless you're that volunteer or an admin (see privacy note above).

### 2b) Admin: get a volunteer's full task history

- `GET /api/volunteers/{id}/tasks`
- `{id}` is the **volunteer's profile id** (same one used everywhere else in this API — `VolunteerResponse.id`, `POST /api/volunteers/tasks`'s `volunteerId`), not their user id.
- Returns an array of `VolunteerTaskResponse` (same shape as `GET /api/volunteers/tasks`), any status, newest first — the full history for that one volunteer.
- Admin-only — `400` with `"Only admins can view a volunteer's task history"` for non-admins; unauthenticated requests get a plain `403`.
- `400` with `"Volunteer not found with id: {id}"` if the volunteer profile doesn't exist.
- This is the admin-facing counterpart to `GET /api/volunteers/tasks` (which only returns *your own* tasks) — use it to build a per-volunteer task history view in the admin dashboard, the same way `GET /api/rehomings/{id}/applications` lets an owner see all applicants for one listing.

### 3) Register as a volunteer

- `POST /api/volunteers`

```json
{
  "skills": ["TRANSPORT", "FEEDING"],
  "availability": "WEEKENDS",
  "location": "Accra",
  "bio": "Happy to help",
  "emergencyContact": "Jane Doe 555-1234"
}
```

- `skills`: array of `TRANSPORT | FEEDING | MEDICAL_CARE | PHOTOGRAPHY | FUNDRAISING | FOSTERING | OTHER` (at least one required). Values are case-insensitive server-side (`transport` works) but always respond with uppercase.
- `availability`: one of `WEEKDAYS | WEEKENDS | BOTH | FLEXIBLE` (required).
- `location`: required. `bio`/`emergencyContact`: optional.
- **One volunteer profile per user** — a second registration attempt returns `400` with `"You have already registered as a volunteer"`. Check `GET /api/volunteers/my` first (a `404`-style "not found" response means they haven't registered yet) to decide whether to show a registration form or their existing profile/status.
- New registrations always start at `status: "PENDING"` — they won't appear in the public list until an admin approves them.

### 4) Update volunteer profile

- `PUT /api/volunteers/{id}`
- Same body shape as registration — this replaces skills/availability/location/bio/emergencyContact. It does **not** change `status`, `hoursLogged`, `totalTasksCompleted`, or `badgeLevel` (those are managed separately).
- Only the volunteer themselves or an admin can call this — `400` with `"Only the volunteer can update this profile"` otherwise.

### 5) Admin: approve/suspend a volunteer

- `PATCH /api/volunteers/{id}/status`

```json
{ "status": "APPROVED" }
```

- Valid values: `PENDING | APPROVED | ACTIVE | SUSPENDED | INACTIVE`.
- Admin-only — `400` with `"Only admins can change volunteer status"` for non-admins.
- Use `GET /api/volunteers/admin` (section 5b below) to build the approval queue this acts on — that's the fix for the gap previously noted here.

### 5b) Admin: review queue (list volunteers by status)

- `GET /api/volunteers/admin?status=PENDING`

`status` is optional:
- Omitted → defaults to `PENDING` (the actual "needs approval" queue — use this for the main admin dashboard view).
- Any valid status (`APPROVED`, `ACTIVE`, `SUSPENDED`, `INACTIVE`) → filters to that status.
- `ALL` → every volunteer regardless of status, for an audit view.
- Anything else → `400` with `"Invalid status: {value}"`.

Admin-only — `400` with `"Only admins can view the volunteer review queue"` for non-admins; unauthenticated requests get a plain `403`.

Unlike the public list/detail endpoints, this always includes `emergencyContact` on every result (the requester is confirmed admin, so the privacy restriction from section 2 doesn't apply here). This is the same pattern as the rehoming module's `GET /api/rehomings/admin`.

### 6) Get my volunteer profile

- `GET /api/volunteers/my`
- Returns `404`-style `"You have not registered as a volunteer yet"` if the current user has no volunteer profile — use this to decide whether to show "Become a Volunteer" CTA vs. their dashboard.
- Always includes `emergencyContact` (it's their own data).

### 7) Get my assigned tasks

- `GET /api/volunteers/tasks`
- Returns all tasks (any status: `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`) ever assigned to the current user's volunteer profile, newest first.
- Same `"You have not registered as a volunteer yet"` error if they have no profile.

### 8) Admin: create and assign a task

- `POST /api/volunteers/tasks`

```json
{
  "volunteerId": 1,
  "petId": 12,
  "title": "Transport Bella to vet",
  "description": "Drive Bella to the vet clinic",
  "taskType": "TRANSPORT",
  "scheduledDate": "2026-07-10T09:00:00",
  "adminNotes": null
}
```

- `volunteerId` is the **volunteer's profile id** (from `VolunteerResponse.id`), not their user id.
- `petId` is optional — omit for tasks not tied to a specific pet (events, fundraising).
- `taskType`: `RESCUE | TRANSPORT | FEEDING | MEDICAL_ASSIST | PHOTOGRAPHY | EVENT | OTHER`.
- New tasks always start at `status: "ASSIGNED"`.
- Admin-only — `400` with `"Only admins can create and assign tasks"` otherwise.

### 9) Mark a task completed

- `PATCH /api/volunteers/tasks/{id}/complete`

```json
{ "hoursSpent": 3 }
```

- Only the assigned volunteer (or an admin) can complete it — `400` with `"Only the assigned volunteer can complete this task"` otherwise.
- Sets `status: "COMPLETED"`, `completedDate: now`, `hoursSpent` as given, and atomically updates the volunteer's `hoursLogged` (+= hoursSpent) and `totalTasksCompleted` (+= 1).
- **Badge level auto-recalculates** on every completion based on `totalTasksCompleted`:
  - `NONE`: 0–4 tasks
  - `BRONZE`: 5–14 tasks
  - `SILVER`: 15–29 tasks
  - `GOLD`: 30–49 tasks
  - `PLATINUM`: 50+ tasks
  - These thresholds aren't in the spec — they're a reasonable default. Flag if you want different numbers; it's one method (`calculateBadgeLevel` in `VolunteerService`) to change.
- There's no endpoint to transition a task to `IN_PROGRESS` or `CANCELLED` yet — only `ASSIGNED → COMPLETED` is wired up.

### 10) Volunteer statistics

- `GET /api/volunteers/stats`
- Public, no auth required. Use for an "our volunteers" impact section (total hours, tasks completed, badge distribution).

## Suggested UI States

### Volunteer status
- `PENDING` → "Your application is under review"
- `APPROVED` → active, visible in the public directory
- `ACTIVE` → same visibility as `APPROVED`, distinguish if you want to highlight "currently on a task" vs. "available"
- `SUSPENDED` / `INACTIVE` → hidden from public directory; show the volunteer their own status via `/my` with an explanation

### Badge levels
Display as a visual badge/icon on the public profile: `NONE` (no badge shown) → `BRONZE` → `SILVER` → `GOLD` → `PLATINUM`.

## Error Handling

Same convention as the rest of the API: `{"status":"error","message":"..."}`, `400` for ownership/role violations (not `403`) — check `message` for specifics. Truly unauthenticated requests to protected endpoints get a plain `403`.

Common messages: `"Volunteer not found with id: {id}"`, `"You have already registered as a volunteer"`, `"You have not registered as a volunteer yet"`, `"Only the volunteer can update this profile"`, `"Only admins can change volunteer status"`, `"Only admins can create and assign tasks"`, `"Only the assigned volunteer can complete this task"`, `"Only admins can view a volunteer's task history"`, `"Invalid skill: {value}"` / `"Invalid availability: {value}"` / `"Invalid status: {value}"` / `"Invalid taskType: {value}"` (enum validation failures — check the exact value sent).

## Known Gaps (not implemented)

- No task status transitions beyond `ASSIGNED → COMPLETED` (no `IN_PROGRESS` or `CANCELLED` endpoints).
- No pagination on any list endpoint.
- No notification/WebSocket push when a volunteer is approved or assigned a task — pull-based only (poll or refetch).
- Badge thresholds are a placeholder default, not a spec'd value — confirm before launch.
