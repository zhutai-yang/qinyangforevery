import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const eventDetailSource = readFileSync(fileURLToPath(new URL('./EventDetail.vue', import.meta.url)), 'utf8');

describe('Event detail page layout', () => {
  it('does not show redundant schedule count and status overview cards', () => {
    expect(eventDetailSource).not.toContain('赛程数量');
    expect(eventDetailSource).not.toContain('当前状态');
  });
});
