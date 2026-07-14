# Lost & Found Pets Frontend Handoff

## Overview

A public board where anyone can report a lost or found pet with a photo, last-seen location, and contact info. Other users browse the board and reach out directly via the contact info on the report. Reports auto-expire 60 days after creation if never resolved, and the reporter can mark a report `REUNITED` once the pet is recovered.

This is a from-scratch integration — no existing frontend code for this feature.

Base URL:
- `http://localhost:8080/api/lost-found`

## Authentication

- Public reads:
  - `GET /api/lost-found` (active reports only, filterable)
  - `GET /api/lost-found/{id}`
- Authenticated actions:
  - `POST /api/lost-found` (submit a report)
  - `PUT /api/lost-found/{id}` (update own report)
  - `PATCH /api/lost-found/{id}/reunited` (mark own report reunited)
  - `DELETE /api/lost-found/{id}` (remove own report; admins can remove any)
  - `GET /api/lost-found/my`

## Main Data Shape

### LostFoundPetResponse

```json
{
  "id": 1,
  "reporter": { "id": 7, "name": "Foster Tester", "email": "fostertester1@example.com" },
  "reportType": "LOST",
  "petName": "Rex",
  "petType": "DOG",
  "petBreed": "German Shepherd",
  "description": "Lost near the park, wearing a red collar",
  "imageUrl": "https://res.cloudinary.com/.../rex.jpg",
  "lastSeenLocation": "Accra Sports Stadium area",
  "city": "Accra",
  "latitude": null,
  "longitude": null,
  "contactPhone": "0551234567",
  "contactEmail": null,
  "status": "ACTIVE",
  "expiresAt": "2026-09-11 20:12:14",
  "createdAt": "2026-07-13 20:12:14",
  "updatedAt": "2026-07-13 20:12:14"
}
```

- `reportType`: `LOST | FOUND`.
- `petName` is commonly `null` on `FOUND` reports (the finder doesn't know the pet's name).
- `petType` is a free-text string (e.g. `"DOG"`, `"CAT"`, `"RABBIT"`) — not a fixed enum, so it accepts anything.
- `imageUrl`: upload the photo separately via the existing Cloudinary upload endpoint (`/api/upload/...`) first, then pass the returned URL here — this feature doesn't handle file upload itself.
- `latitude`/`longitude` are optional precise coordinates; `lastSeenLocation`/`city` are the free-text/city fields used for filtering and display. Omit lat/lng if you don't have GPS data.
- `expiresAt` is always `createdAt + 60 days`, set automatically — you can't set it yourself.

## API Contract

### 1) List active reports

- `GET /api/lost-found?city=Accra&type=LOST`
- Both query params are optional and combinable:
  - `city` — case-insensitive exact match.
  - `type` — `LOST` or `FOUND`.
- Only ever returns `status: ACTIVE` reports — `REUNITED` and `EXPIRED` reports never appear here, even to the original reporter (they should use `GET /api/lost-found/my` for that).
- Newest first.

### 2) Get a single report

- `GET /api/lost-found/{id}`
- Unlike the list endpoint, this does **not** filter by status — a direct link to a `REUNITED` report still resolves (useful for a "this pet was found!" success page). `400`-style `"Lost & found report not found with id: {id}"` if it doesn't exist.

### 3) Submit a report

- `POST /api/lost-found`

```json
{
  "reportType": "LOST",
  "petName": "Rex",
  "petType": "DOG",
  "petBreed": "German Shepherd",
  "description": "Lost near the park, wearing a red collar",
  "imageUrl": "https://...",
  "lastSeenLocation": "Accra Sports Stadium area",
  "city": "Accra",
  "latitude": 5.6037,
  "longitude": -0.1870,
  "contactPhone": "0551234567",
  "contactEmail": "owner@example.com"
}
```

- Required: `reportType`, `petType`, `description`, `lastSeenLocation`.
- **At least one of `contactPhone` or `contactEmail` is required** — `400` with `"At least one of contactPhone or contactEmail is required"` if both are blank. This is checked in the service, not bean validation, since either one alone is acceptable.
- Always starts at `status: "ACTIVE"`, `expiresAt` auto-set to 60 days out.

### 4) Update a report

- `PUT /api/lost-found/{id}`
- Same body shape as submission (full replace, not a partial patch).
- Only the reporter can update — `400` with `"You can only update your own report"` otherwise.
- Only while `status: ACTIVE` — `400` with `"Only an active report can be updated — current status: {status}"` if it's already `REUNITED` or `EXPIRED`. This keeps resolved/expired board entries from being silently rewritten.

### 5) Mark reunited

- `PATCH /api/lost-found/{id}/reunited`
- No body needed.
- Reporter or admin only — `400` with `"You can only mark your own report as reunited"` otherwise.
- Only from `status: ACTIVE` — `400` with `"Only an active report can be marked reunited — current status: {status}"` otherwise (can't re-reunite an already-`EXPIRED` report, for example).
- This is a terminal, one-way transition — there's no "un-reunite" endpoint. If someone marks a report reunited by mistake, they'd need to submit a new report.

### 6) Delete a report

- `DELETE /api/lost-found/{id}`
- Reporter or admin only — `400` with `"You can only delete your own report"` otherwise.
- Works regardless of status (`ACTIVE`, `REUNITED`, or `EXPIRED`).

### 7) My reports

- `GET /api/lost-found/my`
- All of the current user's reports regardless of status, newest first. Use this to build "my listings" with badges for `ACTIVE` / `REUNITED` / `EXPIRED`.

## Auto-Expiry

An hourly scheduled sweep (`LostFoundPetService.autoExpireReports`, same pattern as the pet-sitting auto-complete job) finds every `ACTIVE` report whose `expiresAt` has passed and flips it to `EXPIRED`. There's no user-facing action for this — it just happens in the background. Expired reports drop off the public board (`GET /api/lost-found`) but remain visible via `GET /api/lost-found/{id}` and `GET /api/lost-found/my`.

## Suggested UI States

- `ACTIVE` → shown on the public board, "Contact reporter" CTA visible.
- `REUNITED` → show a celebratory badge ("🎉 Reunited!"), hide contact info if you want (it's a resolved case).
- `EXPIRED` → only visible in "my reports"; offer a "repost" flow (pre-fill a new report form from the expired one) since there's no reactivation endpoint.
