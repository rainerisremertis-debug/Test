---
name: qwen-vl-termux-screen-automation
description: Optimize screen reading and device automation using Qwen VL (Vision-Language) running in Termux, integrated with Gotcha AI. Use when automating device tasks, reading screens with minimal tokens, or combining Gotcha accessibility with a local vision model.
---

# Qwen VL Termux Integration for Gotcha

Optimize screen reading and device interaction using Qwen VL running in Termux, enabling fully automated tasks with minimal token consumption via Gotcha AI.

## Prerequisites

- Termux installed with RUN_COMMAND granted
- Qwen VL model available (via Ollama or llama.cpp)
- Gotcha accessibility service enabled
- Gotcha notification listener enabled

## Setup

### 1. Install Qwen VL in Termux

```bash
pkg update -y
pkg install ollama -y
ollama pull qwen2.5vl:3b
```

### 2. Start Ollama Server

```bash
OLLAMA_HOST=0.0.0.0 nohup ollama serve > ~/ollama.log 2>&1 < /dev/null &
```

### 3. Configure Gotcha

- Enable accessibility service in Gotcha settings
- Enable screen reading in proactive settings
- Set up Termux bridge in Gotcha connectors

## Screen Reading Optimization

### Minimal Token Approach

1. **Cache First**: Before reading screen, check if content is cached (last 5 seconds)
2. **Diff Detection**: Only send changed regions to Qwen VL
3. **Text Extraction**: Use accessibility text first, only use vision for images/icons
4. **Batch Processing**: Group multiple screen reads into single Qwen VL call

### Screen Read Procedure

1. Read accessibility tree (text only, ~100 tokens)
2. If text sufficient → skip vision
3. If images/icons needed → capture screenshot
4. Resize screenshot to 512x512 (reduce tokens)
5. Send to Qwen VL with minimal prompt
6. Cache result for 5 seconds

## Device Interaction Automation

### Action Types

- **Tap**: Tap by text/coordinates
- **Swipe**: Scroll/navigate
- **Type**: Input text
- **Key**: Press system keys
- **Wait**: Pause for UI update

### Automation Loop

1. Read screen (minimal)
2. Analyze with Qwen VL
3. Determine action
4. Execute action via Gotcha
5. Wait for UI update (300ms)
6. Repeat until task complete

## Token Minimization Strategies

### 1. Prompt Optimization

- Use short system prompts
- Avoid repeating context
- Use structured output (JSON)
- Limit response length

### 2. Caching

- Cache screen reads for 5 seconds
- Cache model responses for identical queries
- Reuse analysis across similar screens

### 3. Batching

- Batch multiple elements in one call
- Process screen regions together
- Combine tap targets in single analysis

### 4. Model Selection

- Use qwen2.5vl:3b for simple tasks (faster, fewer tokens)
- Use qwen2.5vl:7b only for complex reasoning
- Fall back to text-only for known UI patterns

## Example Prompts

### Minimal Screen Analysis

```
Screen: [screenshot]
Task: [task description]
Output JSON: {"action": "tap|swipe|type|wait", "target": "text or coords", "value": "optional"}
```

### Element Detection

```
Find: [element description]
Screen: [screenshot]
Output: [{"text": "...", "bounds": [x,y,w,h], "clickable": true/false}]
```

## Qwen VL Termux Commands

### Start Server

```bash
OLLAMA_HOST=0.0.0.0 OLLAMA_KEEP_ALIVE=5m ollama serve > ~/ollama.log 2>&1 < /dev/null &
```

### Query Qwen VL

```bash
curl -s -X POST http://localhost:11434/api/generate \
  -d '{"model":"qwen2.5vl:3b","prompt":"[prompt]","images":["[base64]"],"stream":false}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['response'])"
```

### Screenshot + Analysis (Combined)

```bash
screencap -p /sdcard/screenshot.png
base64 /sdcard/screenshot.png | tr -d '\n' > /sdcard/screenshot.b64
curl -s -X POST http://localhost:11434/api/generate \
  -d "{\"model\":\"qwen2.5vl:3b\",\"prompt\":\"[task]\",\"images\":[\"$(cat /sdcard/screenshot.b64)\"],\"stream\":false}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['response'])"
```

## Gotcha Integration

### Screen Reading Flow

1. Gotcha accessibility service captures screen
2. Text content extracted via accessibility tree
3. If visual analysis needed → screenshot via Termux
4. Screenshot sent to Qwen VL via Termux command
5. Result parsed and action executed via Gotcha

### Device Control Flow

1. Qwen VL analyzes screen and determines action
2. Gotcha executes action via accessibility service
3. Wait for UI update
4. Repeat

### Token Budget

- Screen read: ~100 tokens (text) or ~500 tokens (vision)
- Analysis: ~200 tokens
- Action execution: ~50 tokens
- Total per cycle: ~350-750 tokens
- Target: <500 tokens per cycle average

## Error Handling

### Common Issues

1. **Ollama not running**: Restart with `ollama serve`
2. **Screenshot failed**: Check storage permissions
3. **Model timeout**: Reduce image size or use smaller model
4. **Accessibility denied**: Re-enable in settings

### Fallback Strategy

1. Try text-only analysis first
2. If insufficient, use vision
3. If vision fails, use coordinate-based heuristics
4. If all fail, ask user for input

## Performance Tips

1. **Resize screenshots** to 512x512 before sending
2. **Use JPEG** instead of PNG for smaller size
3. **Crop to relevant region** instead of full screen
4. **Cache aggressively** - screens don't change that fast
5. **Use smaller models** for simple tasks
6. **Batch actions** - plan multiple steps at once

## Security Considerations

1. Never store credentials in automation scripts
2. Validate all actions before execution
3. Provide undo capability for destructive actions
4. Log all automated actions for audit
5. Require confirmation for sensitive operations
6. Respect app permissions and security boundaries

## Troubleshooting

### Qwen VL Not Responding

- Check Ollama: `curl http://localhost:11434/api/version`
- Restart: `pkill ollama; ollama serve &`
- Check logs: `tail -20 ~/ollama.log`

### Screen Reading Fails

- Verify accessibility service is enabled
- Check Gotcha has screen capture permission
- Restart accessibility service
- Check for screen overlay conflicts

### Actions Not Executing

- Verify accessibility service can perform actions
- Check if target element is clickable
- Try coordinate-based tapping as fallback
- Ensure no system dialog is blocking

### High Token Usage

- Enable caching (5 second TTL)
- Reduce screenshot resolution
- Use text-only analysis when possible
- Batch multiple operations
- Use smaller model for simple tasks
