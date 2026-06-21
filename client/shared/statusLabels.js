export function eventStatusText(status) {
  const map = {
    draft: '草稿',
    published: '已发布',
    archived: '已归档'
  };
  return map[status] || status || '—';
}

export function matchStatusText(status) {
  const map = {
    scheduled: '已安排',
    finished: '已结束'
  };
  return map[status] || status || '—';
}

export function scoreText(result) {
  if (!result) return '';
  const home = result.score_home;
  const away = result.score_away;
  if (home == null || away == null) return '';
  return home + ' : ' + away;
}
