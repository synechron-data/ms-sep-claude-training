/*
 * PreToolUse demo hook: shows the different ways a hook can respond.
 *
 * Pick a mode by editing DEFAULT_MODE below, or set PRETOOL_MODE in the env:
 *   allow    - exit 0, no output: tool call proceeds normally (default)
 *   log      - log the stdin payload, then allow
 *   override - allow, but rewrite the tool input via updatedInput
 *   block    - exit 2 hard block; stderr is shown to Claude as the reason
 *   deny     - JSON permissionDecision 'deny' on stdout, exit 0
 */

const fs = require('fs');
const path = require('path');

const DEFAULT_MODE = 'allow';
const MODE = process.env.PRETOOL_MODE || DEFAULT_MODE;

const LOG_FILE = path.join(process.env.CLAUDE_PROJECT_DIR || '.', '.claude', 'hook-logs', 'pretooluse-demo.log');

function logMessage(message) {
    try {
        fs.mkdirSync(path.dirname(LOG_FILE), { recursive: true });
        fs.appendFileSync(LOG_FILE, message + '\n', 'utf-8');
    } catch {
        // logging must never change the hook's decision
    }
}

function logPayload(payload) {
    const timeStamp = new Date().toLocaleString('sv-SE', { timeZone: 'Asia/Kolkata' });
    logMessage("\n" + "-".repeat(60));
    logMessage(`[${timeStamp}] mode=${MODE} tool=${payload.tool_name}`);
    logMessage('STDIN PAYLOAD:');
    logMessage(JSON.stringify(payload, null, 2));
}

let raw = '';
process.stdin.on('data', (chunk) => {
    raw += chunk;
});

process.stdin.on('end', () => {
    let payload;
    try {
        payload = JSON.parse(raw);
    } catch {
        // Blocking modes fail closed (exit 2); the others fail open (exit 0).
        // Any other exit code (e.g. 1 from an uncaught error) is a non-blocking
        // error in Claude Code, so the tool call would run.
        if (MODE === 'block' || MODE === 'deny') {
            process.stderr.write('[pretooluse-demo] could not parse hook input; blocking.\n');
            process.exit(2);
        }
        process.exit(0);
    }

    switch (MODE) {
        // ---------------------------------- Allow and log input
        case 'log': {
            logPayload(payload);
            process.exit(0);
        }

        // ---------------------------------- Allow and override
        // permissionDecision 'allow' skips the permission prompt, so an 'ask'
        // rule (e.g. git push) would not be asked. Safe here only because the
        // command is replaced with a harmless echo.
        case 'override': {
            logPayload(payload);
            const response = {
                hookSpecificOutput: {
                    hookEventName: 'PreToolUse',
                    permissionDecision: 'allow',
                    permissionDecisionReason: '[pretooluse-demo] hook allowed and overrode this tool call.',
                    updatedInput: { ...payload.tool_input, command: 'echo overridden by custom script' }
                }
            };
            logMessage(JSON.stringify(response, null, 2));
            process.stdout.write(JSON.stringify(response));
            process.exit(0);
        }

        // ---------------------------------- Hard Block
        // process.exit(2) hard-blocks the tool call; Claude Code treats it as an error
        // and shows stderr output (if any) as the reason — no JSON response needed.
        case 'block': {
            logPayload(payload);
            logMessage('BLOCKED: process.exit(2) — see stderr for reason');
            process.stderr.write('[pretooluse-demo] hook blocked this tool call.\n');
            process.exit(2);
        }

        // ---------------------------------- Hard Block with Deny
        // Deny: rejects the tool call via structured JSON on stdout while exiting
        // cleanly (0). permissionDecisionReason is shown to Claude, explaining why
        // the call was denied — unlike exit(2), no stderr text is needed.
        case 'deny': {
            logPayload(payload);
            const response = {
                hookSpecificOutput: {
                    hookEventName: 'PreToolUse',
                    permissionDecision: 'deny',
                    permissionDecisionReason: '[pretooluse-demo] hook blocked this tool call.',
                }
            };
            logMessage('BLOCK RESPONSE:');
            logMessage(JSON.stringify(response, null, 2));
            process.stdout.write(JSON.stringify(response));
            process.exit(0);
        }

        // ---------------------------------- Allow (default)
        default:
            process.exit(0);
    }
});
