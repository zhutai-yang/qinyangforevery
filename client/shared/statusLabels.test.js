import { describe, expect, it } from 'vitest';
import { eventStatusText, matchStatusText, scoreText } from './statusLabels.js';

describe('statusLabels', () => {
  it('maps public event and match statuses to Chinese labels', () => {
    expect(eventStatusText('published')).toBe('已发布');
    expect(eventStatusText('draft')).toBe('草稿');
    expect(matchStatusText('scheduled')).toBe('已安排');
    expect(matchStatusText('finished')).toBe('已结束');
  });

  it('formats match result scores when present', () => {
    expect(scoreText({ score_home: 3, score_away: 1 })).toBe('3 : 1');
    expect(scoreText(null)).toBe('');
  });
});
