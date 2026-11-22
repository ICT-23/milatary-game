// Verifies that every element id game.js looks up exists in index.html, that the
// scripts load in dependency order, and that the HUD state classes have CSS.
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const html = fs.readFileSync(path.join(ROOT, 'app/src/main/assets/game/index.html'), 'utf8');
const js = fs.readFileSync(path.join(ROOT, 'app/src/main/assets/game/game.js'), 'utf8');

const htmlIds = new Set();
for (const match of html.matchAll(/\bid="([^"]+)"/g)) htmlIds.add(match[1]);

const used = new Set();
for (const match of js.matchAll(/\bel\('([^']+)'\)/g)) used.add(match[1]);

const missing = [...used].filter((id) => !htmlIds.has(id));
const unused = [...htmlIds].filter((id) => !used.has(id));

console.log('ids referenced by game.js: ' + used.size);
console.log('ids present in index.html: ' + htmlIds.size);
console.log('missing ids: ' + (missing.length ? missing.join(', ') : 'none'));
console.log('html-only ids: ' + (unused.length ? unused.join(', ') : 'none'));

const scripts = [...html.matchAll(/<script\b[^>]*src="([^"]+)"/g)].map((m) => m[1]);
console.log('script order: ' + scripts.join(' -> '));
const levelsIndex = scripts.findIndex((s) => s.indexOf('levels.js') >= 0);
const gameIndex = scripts.findIndex((s) => s.indexOf('game.js') >= 0);
if (levelsIndex < 0 || gameIndex < 0 || levelsIndex > gameIndex) {
  console.log('FAIL: levels.js must load before game.js');
  process.exit(1);
}
if (!/<\/body>\s*<\/html>\s*$/.test(html)) {
  console.log('FAIL: index.html does not close body/html');
  process.exit(1);
}

const checks = [
  ['pickup toast css', /\.pickup-toast\s*\{/.test(html) && /\.pickup-toast\.show\s*\{/.test(html)],
  ['boss bar css', /#boss-bar\s*\{/.test(html) && /#boss-bar\.show\s*\{/.test(html) && /#boss-bar-fill\s*\{/.test(html)],
  ['crosshair part css', /\.crosshair\.crit::before\s*\{/.test(html) && /\.crosshair\.legs::before\b/.test(html) && /#crosshair-label\s*\{/.test(html)],
  ['damage number css', /#damage-layer\s*\{/.test(html) && /\.damage-number\s*\{/.test(html)],
  ['deploy screen css', /#deploy-screen\s*\{/.test(html) && /#deploy-screen\.show\s*\{/.test(html) && /\.chapter-card\s*\{/.test(html)],
  ['deploy resume button', htmlIds.has('deploy-resume-btn')],
  ['chapter select button', htmlIds.has('pause-chapter-btn')]
];

let failed = 0;
for (const [name, ok] of checks) {
  if (!ok) failed += 1;
  console.log(name + ': ' + (ok ? 'ok' : 'MISSING'));
}

process.exit(missing.length === 0 && failed === 0 ? 0 : 1);
