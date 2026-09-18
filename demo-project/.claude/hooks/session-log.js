#!/usr/bin/env node
// Stop hook: logs an estimated token/cost summary for the turn that just
// finished, and shows it to the user via systemMessage.
// Estimate only: the transcript format is internal to Claude Code, the file
// can lag behind the final message, and the rates below are illustrative.
const fs = require('fs');
const path = require('path');

const PROJECT_DIR = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const LOG_FILE = path.join(PROJECT_DIR, '.claude', 'hook-logs', 'session-log.log');

// USD per token — illustrative Sonnet-class list prices; confirm before quoting.
const RATE = { input: 3.00 / 1e6, output: 15.00 / 1e6, cacheWrite: 3.75 / 1e6, cacheRead: 0.30 / 1e6 };

function isRealUserPrompt(entry) {
  if (entry.type !== 'user') return false;
  const content = entry.message?.content;
  // Tool results are also recorded as "user" entries; they don't start a new turn.
  return !(Array.isArray(content) && content.some(c => c.type === 'tool_result'));
}

let raw = '';
process.stdin.on('data', chunk => (raw += chunk));
process.stdin.on('end', () => {
  let input = {};
  try { input = JSON.parse(raw); } catch { /* manual test run */ }

  // One API response can be written as several transcript lines with the same
  // message id, so keep one usage record per id, for the current turn only.
  let usageById = new Map();
  const transcriptPath = input.transcript_path;
  if (transcriptPath && fs.existsSync(transcriptPath)) {
    for (const line of fs.readFileSync(transcriptPath, 'utf8').split('\n')) {
      if (!line.trim()) continue;
      try {
        const entry = JSON.parse(line);
        if (isRealUserPrompt(entry)) usageById = new Map();
        const usage = entry.type === 'assistant' && entry.message?.usage;
        if (usage) usageById.set(entry.message.id ?? entry.uuid, usage);
      } catch { /* skip malformed lines */ }
    }
  }

  const t = { input: 0, output: 0, cacheWrite: 0, cacheRead: 0 };
  for (const u of usageById.values()) {
    t.input += u.input_tokens || 0;
    t.output += u.output_tokens || 0;
    t.cacheWrite += u.cache_creation_input_tokens || 0;
    t.cacheRead += u.cache_read_input_tokens || 0;
  }
  const cost = t.input * RATE.input + t.output * RATE.output + t.cacheWrite * RATE.cacheWrite + t.cacheRead * RATE.cacheRead;

  const summary = `calls=${usageById.size} input=${t.input} output=${t.output} cacheWrite=${t.cacheWrite} cacheRead=${t.cacheRead} cost≈$${cost.toFixed(4)}`;
  fs.mkdirSync(path.dirname(LOG_FILE), { recursive: true });
  fs.appendFileSync(LOG_FILE, `[${new Date().toISOString()}] session=${input.session_id ?? 'manual'} ${summary}\n`, 'utf8');

  // Stop hook stdout isn't shown in the terminal; systemMessage is.
  console.log(JSON.stringify({ systemMessage: `Turn estimate: ${summary}` }));
});
