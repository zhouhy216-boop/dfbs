# ACCTPERM Step-03 Impact Check (Facts Only)

**Request ID:** ACCTPERM-260211-001-03-IMP | **Related:** ACCTPERM-260211-001-03

---

**1) Frontend**
- Role Templates UI: (1) Account & Permissions → Role Templates tab (`RoleTemplatesTab.tsx`); (2) Old `/admin/roles-permissions` (`RolesPermissions/index.tsx`).
- roleKey: Entered in create modal in both (RoleTemplatesTab `CreateRoleModal` roleKey state + input; RolesPermissions `Form.Item name="roleKey"` required). Create calls: `acctPermService.createRole(roleKey, label, true)` and `permService.createRole(roleKey, label)`. Displayed in table under label and in dropdowns as `label (roleKey)`.

**2) Backend**
- `perm_role`: `id`, `role_key` VARCHAR(64) NOT NULL UNIQUE, `label` VARCHAR(128) NOT NULL, `enabled` BOOLEAN NOT NULL DEFAULT true (V0072, V0073). **No description column.**
- Endpoints: account-permissions `POST/PUT/DELETE .../roles`, `PUT .../roles/{id}/template`; allowlist perm `POST/PUT/DELETE .../roles`, `PUT .../roles/{id}/template`. CreateRoleRequest(roleKey, label, enabled); PermRoleService.create() **requires** roleKey non-null/non-blank.

**3) Compatibility**
- **A)** New endpoint (e.g. POST .../roles with auto-key body): old POST and all callers unchanged. **B)** roleKey optional + generate when absent: existing frontends/callers still send roleKey; backend must accept and generate when null/blank. DevPermDefaultRoleTemplatesSeeder uses explicit roleKey — unaffected. Description = new column + entity/DTO.

**4) Clone / enable/disable / save-to-apply**
- Clone: No existing clone API or UI; new surface. Enable/disable: already in entity and PUT /template. Save-to-apply: draft until Save (saveRoleTemplate) in both UIs; must preserve.

**5) Regression watchlist**
- roleKey hidden/auto in new UX; template save/reset semantics unchanged; account override & enforcement unchanged; `/admin/roles-permissions` usable or contract backward-compatible; ACTION_ROLE_TEMPLATE_SAVE audit still written on template save.

**6) Build/test**
- Not executed.
