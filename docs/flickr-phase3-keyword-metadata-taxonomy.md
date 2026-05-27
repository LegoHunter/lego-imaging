# Flickr Integration Phase 3 Keyword Metadata Taxonomy

This document defines the gen-2 image metadata contract used by Flickr Integration Phase 3. AlbumManifest JSON files are legacy gen-1 artifacts only. Gen-2 processing should derive source state from image metadata during ingestion, persist durable fields in the database, store photo binaries in S3-compatible storage, and drive Flickr sync from `item_inventory_photo` plus the `external_image*` tables.

## Parsing Rules

Metadata is extracted by `MetadataExtractorService`.

- Keyword tags are any metadata tags whose name contains `keyword`.
- Keyword strings are split on semicolons.
- Keyword keys are normalized to lowercase.
- Keyword pairs use `key:value`.
- Presence-only keywords are treated as boolean `true`.
- Caption text comes from caption or description metadata tags.
- Legacy `cp` is accepted as a caption fallback when no caption or description tag is present.
- Legacy `rmk*` keywords are ignored by gen-2 processing.

## Supported Fields

| Canonical field | Supported keywords and aliases | Type | Persistence target | Durable? | Purpose |
| --- | --- | --- | --- | --- | --- |
| `uuid` | `uuid` | Text | `item_inventory.uuid` | Durable on inventory | Required inventory identity used to find or create the item inventory row. Not stored as photo-local metadata after ingestion. |
| `externalItemNumber` | `bl` | Text | External item and inventory relationship | Durable outside photo metadata | Required BrickLink item number used to link the photo to the catalog item. Not stored as photo-local metadata after ingestion. |
| `primary` | `primary` | Boolean | `item_inventory_photo.is_primary`, then `external_image_album_image.is_primary` | Durable | Marks the preferred photo for the item and Flickr album membership. |
| `sealed` | `sealed` | Boolean | `item_inventory.sealed` | Transient keyword, durable inventory field | Updates the inventory sealed state. |
| `builtOnce` | `bo`, `built_once`, `builtonce`, `built-once` | Boolean | `item_inventory.built_once` | Transient keyword, durable inventory field | Updates whether the set has been built once. |
| `boxCondition` | `bc`, `box_condition`, `boxcondition`, `box-condition` | Condition code | `item_inventory.box_condition_id` | Transient keyword, durable inventory field | Updates box condition. |
| `instructionsCondition` | `ic`, `instructions_condition`, `instructionscondition`, `instructions-condition` | Condition code | `item_inventory.instructions_condition_id` | Transient keyword, durable inventory field | Updates instructions condition. |
| `itemCondition` | `item`, `item_condition`, `itemcondition`, `item-condition` | Condition code | `item_inventory.item_condition_id` | Transient keyword, durable inventory field | Updates item condition. |
| `caption` | Caption/description metadata tag, legacy `cp` fallback | Text | `item_inventory_photo.caption` | Durable | Stores free-form per-photo text. Multiple photos for one item may have captions. |

## Condition Codes

Condition keywords map to `ConditionEnum` values. Current supported codes are:

| Code | Meaning |
| --- | --- |
| `M` | Mint |
| `E` | Excellent |
| `VG` | Very good |
| `G` | Good |
| `P` | Poor |
| `NA` | Not applicable |
| `F` | Fair |
| `MS` | Missing |
| `CC` | Color copy |
| `BW` | Black and white copy |
| `SL` | Sealed |

## Caption and Description Policy

Captions are per-photo durable metadata. The final item or marketplace description should be composed from durable database state, not from local manifest JSON files.

The intended generated description is:

1. A condition paragraph derived from `itemCondition`, `boxCondition`, `instructionsCondition`, `builtOnce`, and `sealed`.
2. The captions from all photos for the item that have nonblank `item_inventory_photo.caption`, in stable photo order.
3. The short Flickr album URL when available from the relevant external album row.

This means caption ingestion should preserve each photo caption independently. Description generation should happen later from the persisted item/photo/external-image state.

## Legacy Metadata Decisions

| Legacy field | Decision |
| --- | --- |
| AlbumManifest JSON | Legacy gen-1 compatibility/import artifact only. Do not require it for gen-2 Flickr sync. |
| `cp` | Supported as a legacy caption fallback and normalized into `ImageMetadata.caption`. |
| `rmk1`, `rmk2`, `rmk*` | Deprecated and ignored by gen-2 extraction. If old data needs migration, convert it explicitly into photo captions before ingestion or via a one-time migration. |

## Gen-2 Source of Truth

The active Flickr sync source of truth should be:

- `item_inventory`
- `item_inventory_photo`
- `external_image`
- `external_image_album`
- `external_image_album_image`
- S3-compatible object storage for photo binaries

Local AlbumManifest JSON files should not be needed for dry-run planning, apply execution, remote reconciliation, or repair workflows.
