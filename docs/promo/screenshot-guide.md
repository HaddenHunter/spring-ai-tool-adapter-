# Screenshot Guide

Use these demo URLs after starting the demo:

```bash
mvn -pl adapter-demo spring-boot:run
```

## Required Shots

| Shot | URL | Purpose |
| --- | --- | --- |
| Approval loop | `/chat?lang=zh&springai=approval-auto` | Shows high-risk tool pending approval. |
| Agent running | `/chat?promo=agentweaver&state=running` | Shows phases and steps during execution. |
| Agent waiting | `/chat?promo=agentweaver&state=waiting` | Shows human node and resume boundary. |
| Agent completed | `/chat?promo=agentweaver&state=completed` | Shows final run, artifacts, and checkpoints. |
| Audit review | `/chat?promo=agentweaver&state=audit` | Shows audit evidence and replay posture. |
| Chinese completed | `/chat?lang=zh&promo=agentweaver&state=completed` | Shows Chinese UI for local promotion. |
| Flow editor | `/chat?promo=agentweaver&state=completed` then open Agent tab | Shows editable YAML/JSON Flow Spec. |
| Schema compare | `/chat` then open Tool Schema tab | Shows provider-specific schema output. |

## Visual Checklist

- The product name or value proposition is visible in the first viewport.
- Governance panel shows user, tenant, permissions, tools, and token usage.
- Agent tab shows Flow, Phase, Step, Artifact, and Checkpoint.
- Approval shot clearly shows pending approval and Approve/Reject controls.
- Audit shot shows filters and context before/after.
- Chinese shot renders Chinese UI without Java comment encoding concerns.

## Suggested Filenames

- `docs/spring-ai-approval-demo-zh.png`
- `docs/agentweaver-demo-running.png`
- `docs/agentweaver-demo-waiting.png`
- `docs/agentweaver-demo-completed.png`
- `docs/agentweaver-demo-audit.png`
- `docs/agentweaver-demo-zh-completed.png`
- `docs/p1-flow-editor.png`
- `docs/p1-schema-compare.png`

