const fs = require('fs');
const path = require('path');

const root = process.cwd();
const targetDirs = [path.join(root, 'src'), path.join(root, 'config')];
const filePattern = /\.(jsx?|tsx?)$/;
const riskyPattern = /(label|title|placeholder|headerTitle|message|description)="\\u/i;
const ignoredDirs = new Set(['node_modules', '.git', 'dist', 'coverage']);

const findings = [];

function walk(dir) {
  if (!fs.existsSync(dir)) {
    return;
  }

  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (ignoredDirs.has(entry.name)) {
      continue;
    }

    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(fullPath);
      continue;
    }

    if (!filePattern.test(entry.name)) {
      continue;
    }

    const content = fs.readFileSync(fullPath, 'utf8');
    const lines = content.split(/\r?\n/);
    lines.forEach((line, index) => {
      if (riskyPattern.test(line)) {
        findings.push({
          file: path.relative(root, fullPath),
          line: index + 1,
          text: line.trim(),
        });
      }
    });
  }
}

for (const dir of targetDirs) {
  walk(dir);
}

if (findings.length > 0) {
  console.error('Found raw \\u JSX attribute strings that will render literally:');
  for (const item of findings) {
    console.error(`- ${item.file}:${item.line}`);
    console.error(`  ${item.text}`);
  }
  process.exit(1);
}

console.log('JSX unicode attribute check passed.');