/**
 * Evidence run: PERM UI gating (menu + route guard).
 * Run with CASE=A (allowlist excludes userId) or CASE=B (allowlist includes userId).
 * Requires: frontend on 5173, proxy -> 8081, backend on 8081 with matching config.
 */
import { test, expect } from '@playwright/test';

const CASE_A = process.env.CASE === 'A';
const CASE_B = process.env.CASE === 'B';

test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('用户名').fill('admin');
  await page.getByLabel('密码').fill('dfbs');
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await expect(page).toHaveURL(/\/(dashboard|admin)/, { timeout: 15000 });
  // Wait for perm check (/me) and layout to render
  await page.waitForTimeout(2000);
});

test('PERM menu and route: actual observations', async ({ page }) => {
  const observations: string[] = [];

  // Menu: 角色与权限 (may need to expand 系统 first)
  const menuItem = page.getByText('角色与权限', { exact: true }).first();
  // If not in view, try opening 系统 submenu (ProLayout may collapse)
  const systemItem = page.getByText('系统', { exact: true }).first();
  if (await systemItem.isVisible().catch(() => false)) await systemItem.click();
  await page.waitForTimeout(800);
  const menuVisible = await menuItem.isVisible().catch(() => false);
  observations.push(`Menu "角色与权限" visible: ${menuVisible}`);

  // Direct route
  await page.goto('/admin/roles-permissions');
  await page.waitForTimeout(1500);
  const url = page.url();
  const hasRedirect = url.includes('/dashboard');
  observations.push(`After /admin/roles-permissions, redirect to /dashboard: ${hasRedirect}`);
  const body = await page.locator('body').textContent();
  const hasTitle = body?.includes('角色与权限') ?? false;
  const hasWip = body?.includes('功能开发中') ?? false;
  observations.push(`Page has "角色与权限" title: ${hasTitle}, "功能开发中": ${hasWip}`);

  // Regression: 字典类型 (page is behind SuperAdminGuard; may redirect if user not org super admin)
  await page.goto('/admin/dictionary-types');
  await page.waitForTimeout(2500);
  const dictUrl = page.url();
  const dictOpens = dictUrl.includes('dictionary-types');
  observations.push(`字典类型 page opens: ${dictOpens}`);

  if (CASE_A) {
    expect(menuVisible).toBe(false);
    expect(hasRedirect).toBe(true);
    expect(hasTitle && hasWip).toBe(false);
  }
  if (CASE_B) {
    expect(menuVisible).toBe(true);
    expect(hasTitle && hasWip).toBe(true);
  }
  // Regression: if user can open 字典类型 (org super admin), it should open; otherwise we only record
  if (!dictOpens) observations.push('字典类型: redirected (user may not be org super admin); API regression in fix1.');

  // Log for evidence (no tokens)
  console.log('OBSERVATIONS:', observations.join('; '));
});
