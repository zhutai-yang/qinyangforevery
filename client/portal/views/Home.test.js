import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import Home from './Home.vue';

const homeSource = readFileSync(fileURLToPath(new URL('./Home.vue', import.meta.url)), 'utf8');

describe('Home page real data display', () => {
  it('does not fall back to demo articles when backend returns no featured articles', () => {
    const articles = Home.computed.displayArticles.call({ featuredArticles: [] });

    expect(articles).toEqual([]);
  });

  it('does not fall back to demo athletes when backend returns no highlighted athletes', () => {
    const athletes = Home.computed.displayAthletes.call({ featuredPlayers: [] });

    expect(athletes).toEqual([]);
  });

  it('keeps star players in the hero before recommended articles', () => {
    expect(homeSource.indexOf('hero__featured-players')).toBeLessThan(homeSource.indexOf('panel--articles'));
    expect(homeSource).not.toContain('panel--players');
  });

  it('shows star players in the hero instead of mascot fallback art', () => {
    expect(homeSource).toContain('hero__featured-players');
    expect(homeSource).not.toContain('hero__lion-player');
    expect(homeSource).not.toContain('mascot--lion');
  });

  it('does not keep the removed home stats feature wired in the component', () => {
    expect(Home.computed.heroStats).toBeUndefined();
  });
});
