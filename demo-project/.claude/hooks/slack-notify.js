#!/usr/bin/env node
// Notification hook: posts to a Slack Incoming Webhook when Claude needs
// attention (a permission prompt, or an idle session waiting on input).
const https = require('https');

let raw = '';
process.stdin.on('data', chunk => (raw += chunk));
process.stdin.on('end', () => {
  const webhookUrl = process.env.SLACK_WEBHOOK_URL;
  if (!webhookUrl) process.exit(0);

  let text = 'Claude Code needs your attention';
  try {
    const input = JSON.parse(raw);
    const project = require('path').basename(input.cwd ?? '');
    text = `[${input.notification_type}] ${input.message ?? text}${project ? ` (${project})` : ''}`;
  } catch { /* use default */ }

  const req = https.request(webhookUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, timeout: 5000 });
  // A failed notification should never disrupt the session, so swallow errors.
  req.on('error', () => process.exit(0));
  req.on('timeout', () => req.destroy());
  req.end(JSON.stringify({ text: `🤖 Claude Code — ${text}` }));
});
