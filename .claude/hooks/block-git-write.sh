#!/usr/bin/env bash
# PreToolUse-Hook (ADR 008): blockiert Shell-Befehle, mit denen Claude
# Commits anlegen oder veröffentlichen könnte. Exit 2 = Befehl wird abgelehnt.

input=$(cat)

# Nur das Feld "command" aus dem JSON lesen, nicht die Beschreibung
cmd=$(printf '%s' "$input" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\(\([^"\]\|\.\)*\)".*/\1/p')

git_write='(^|[^[:alnum:]_-])git([[:space:]]+-[^[:space:]]+([[:space:]]+[^-[:space:]][^[:space:]]*)?)*[[:space:]]+(commit|commit-tree|push|merge|rebase|cherry-pick|revert|am|tag)([^[:alnum:]_-]|$)'
gh_write='(^|[^[:alnum:]_-])gh[[:space:]]+(pr[[:space:]]+merge|release[[:space:]]+create)'

if printf '%s' "$cmd" | grep -Eq "$git_write|$gh_write"; then
  echo "Blockiert (ADR 008): Commits, Pushes und Merges macht nur ein Mensch. Schlage stattdessen eine Commit-Message vor." >&2
  exit 2
fi
exit 0
