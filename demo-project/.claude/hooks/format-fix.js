// PostToolUse hook: formats any file Claude writes or edits, backend or frontend.
const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const PROJECT_DIR = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const LOG_FILE = path.join(PROJECT_DIR, '.claude', 'hook-logs', 'format-hook.log');
// On Windows, mvn and npx are .cmd files, which Node can only start through a shell.
const IS_WINDOWS = process.platform === 'win32';

function appendLog(msg) {
  fs.mkdirSync(path.dirname(LOG_FILE), { recursive: true });
  fs.appendFileSync(LOG_FILE, msg + '\n', 'utf8');
}

function run(cmd, args, cwd) {
  if (!IS_WINDOWS) return spawnSync(cmd, args, { cwd, encoding: 'utf8' });
  // Through a shell, pass one command string and quote any argument with spaces.
  const line = [cmd, ...args.map(a => (/\s/.test(a) ? `"${a}"` : a))].join(' ');
  return spawnSync(line, { cwd, encoding: 'utf8', shell: true });
}

let raw = '';
process.stdin.on('data', chunk => (raw += chunk));
process.stdin.on('end', () => {
  let filePath;
  try { filePath = JSON.parse(raw).tool_input?.file_path; } catch { process.exit(0); }
  if (!filePath) process.exit(0);

  let result;
  if (filePath.endsWith('.java')) {
    // Match on the basename only: the plugin's filesPathPattern is matched
    // against a backslash-separated path on Windows, where a literal
    // directory prefix (with its own backslashes/spaces) is fragile to
    // build into a regex. ".*<name>$" avoids embedding any separator.
    const basename = path.basename(filePath).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    result = run('mvn', [
      '-q', '-f', 'backend/pom.xml',
      'com.spotify.fmt:fmt-maven-plugin:format',
      `-DfilesPathPattern=.*${basename}$`,
    ], PROJECT_DIR);
  } else if (filePath.endsWith('.ts') || filePath.endsWith('.html')) {
    result = run('npx', ['--no-install', 'prettier', '--write', filePath], path.join(PROJECT_DIR, 'frontend'));
  } else {
    process.exit(0);
  }

  const status = result.error ? `error ${result.error.message}` : result.status === 0 ? 'OK' : `exit ${result.status}`;
  appendLog(`[${new Date().toISOString()}] file=${filePath} status=${status}`);
});
