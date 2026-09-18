/*
 * PreToolUse hook: blocks dangerous Bash commands before Claude runs them.
 * Exit 0 = allow, exit 2 = block (Claude sees the printed reason).
*/

const fs = require('fs');
const path = require('path');

const LOG_FILE = path.join('.claude', 'hook-logs', 'bash-guard.log');

function appendLog(msg) {
    fs.mkdirSync(path.dirname(LOG_FILE), { recursive: true });
    fs.appendFileSync(LOG_FILE, msg + '\n', 'utf8');
}

// Regex patterns matching known-dangerous shell commands. Any Bash tool_input
// whose command matches one of these should be hard-blocked by the hook —
// these are destructive or irreversible operations with no legitimate reason
// to run inside an automated/agentic workflow.
const DANGER_PATTERNS = [
    // ── Filesystem / disk ──────────────────────────────────────────
    /\bmkfs(\.\w+)?\b/,                          // format a filesystem
    /\bshred\b/,                                 // unrecoverable file wipe
    />\s*\/dev\/(sd|nvme|hd)/,                   // redirect straight onto a raw disk
    /\bfind\s+\/\s.*-delete\b/,                  // find / ... -delete
    /\bchmod\s+-R\s+777\b/,                      // recursive world-writable
    /\bchown\s+-R\s+\S+\s+\/(\s|$)/,             // recursive chown on root

    // ── Windows equivalents (this machine is win32) ────────────────
    /\bformat\s+[a-z]:/i,                        // format C:
    /\b(rd|rmdir)\s+\/s\s+\/q\s+[a-z]:\\?\s*$/i, // rd /s /q C:\
    /\bdel\s+\/[fsq].*[a-z]:\\/i,                // del /f /s /q C:\...
    /Remove-Item.*-Recurse.*-Force.*[a-z]:\\?(\s|$)/i,

    // ── Remote code execution ──────────────────────────────────────
    /\b(curl|wget)\b[^|]*\|\s*(sudo\s+)?(ba|z)?sh\b/, // curl ... | sh
    /\b(iwr|Invoke-WebRequest)\b.*\|\s*iex\b/i,       // PowerShell download-and-run

    // ── Git: history destruction / CLAUDE.md "never push to main" ──
    /\bgit\s+push\b.*\s(-f|--force)(\s|$)/,
    /\bgit\s+push\b.*\b(main|master)\b/,
    /\bgit\s+reset\s+--hard\b/,
    /\bgit\s+clean\s+-[a-z]*f[a-z]*d/,            // git clean -fd / -fdx
    /\bgit\s+branch\s+-D\b/,

    // ── Secrets (the settings.json Read deny can be bypassed via Bash) ─
    /\b(cat|less|more|head|tail|type|Get-Content)\b.*\.env\b/i,
    /\b(printenv|env)\s*($|\|)/,                  // dumping all env vars
    /\b(curl|wget)\b.*(-d|--data|-F|--upload-file)\s*@?\S*\.env/i, // exfil

    // ── Database (Prisma/Postgres stack) ───────────────────────────
    /\bprisma\s+migrate\s+reset\b/,
    /\bprisma\s+db\s+push\b.*--force-reset/,
    /\b(DROP\s+(DATABASE|TABLE|SCHEMA)|TRUNCATE\s+TABLE)\b/i,

    // ── System ─────────────────────────────────────────────────────
    /\b(shutdown|reboot|halt|poweroff)\b/,
    /\bkill\s+-9\s+-1\b/,                         // kill every process you own
    /\bhistory\s+-c\b/,                           // erase shell history
];

let raw = '';
process.stdin.on('data', chunk => (raw += chunk));
process.stdin.on('end', () => {
    let cmd;
    try {
        cmd = JSON.parse(raw).tool_input?.command ?? '';
    } catch {
        process.exit(0);
    }

    const timestamp = new Date().toLocaleString('sv-SE', { timeZone: 'Asia/Kolkata' });

    for (const pattern of DANGER_PATTERNS) {
        if (pattern.test(cmd)) {
            const reason = `BLOCKED — matches danger pattern: ${pattern}`;
            appendLog(`[${timestamp}] ${reason}`);
            appendLog(`  cmd=${cmd}`);
            appendLog('');
            console.error(reason);
            process.exit(2);
        }
    }

    appendLog(`[${timestamp}] ALLOWED cmd=${cmd}`);
    process.exit(0);
});
