// Cross-checks the Android campaign layer against the actual chapter data:
// every mission in levels.js needs a dossier, every choice id needs a brief,
// and MissionDefinition.kt has to stay syntactically balanced.
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const levels = fs.readFileSync(path.join(ROOT, 'app/src/main/assets/game/levels.js'), 'utf8');
const kotlin = fs.readFileSync(path.join(ROOT, 'app/src/main/java/com/example/game/MissionDefinition.kt'), 'utf8');

const levelMissionIds = new Set();
for (const match of levels.matchAll(/^\s{4}(M\d+_[A-Z0-9_]+):\s*\{/gm)) levelMissionIds.add(match[1]);

const levelChoiceIds = new Set();
for (const match of levels.matchAll(/\bid:\s*'([A-Z][A-Z0-9_]+)'/g)) {
  if (!/^M\d+_/.test(match[1])) levelChoiceIds.add(match[1]);
}

const dossierIds = new Set();
for (const match of kotlin.matchAll(/id\s*=\s*"(M\d+_[A-Z0-9_]+)"/g)) dossierIds.add(match[1]);

const briefIds = new Set();
for (const match of kotlin.matchAll(/id\s*=\s*"([A-Z][A-Z0-9_]+)"/g)) {
  if (!/^M\d+_/.test(match[1])) briefIds.add(match[1]);
}

const missingDossiers = [...levelMissionIds].filter((id) => !dossierIds.has(id));
const extraDossiers = [...dossierIds].filter((id) => !levelMissionIds.has(id));
const missingBriefs = [...levelChoiceIds].filter((id) => !briefIds.has(id));

console.log('chapters in levels.js: ' + levelMissionIds.size);
console.log('dossiers in MissionDefinition.kt: ' + dossierIds.size);
console.log('choice ids in levels.js: ' + levelChoiceIds.size);
console.log('choice briefs in MissionDefinition.kt: ' + briefIds.size);
console.log('missing dossiers: ' + (missingDossiers.length ? missingDossiers.join(', ') : 'none'));
console.log('orphan dossiers: ' + (extraDossiers.length ? extraDossiers.join(', ') : 'none'));
console.log('missing choice briefs: ' + (missingBriefs.length ? missingBriefs.join(', ') : 'none'));

// Cheap balance check: Kotlin has no string interpolation here, so paren and
// brace counts must match once string literals are removed.
const stripped = kotlin.replace(/"(?:[^"\\]|\\.)*"/g, '""').replace(/\/\/[^\n]*/g, '');
const count = (text, ch) => text.split(ch).length - 1;
const braces = count(stripped, '{') - count(stripped, '}');
const parens = count(stripped, '(') - count(stripped, ')');
console.log('brace balance: ' + braces + '  paren balance: ' + parens);

const ok = missingDossiers.length === 0 && extraDossiers.length === 0 &&
  missingBriefs.length === 0 && braces === 0 && parens === 0;
process.exit(ok ? 0 : 1);
