package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.agentInstruction() = "AgentInstruction" {
    title = "AI エージェントへの指示"
    summary = "AI コーディングエージェントが読むプロジェクトの指示・スキル・サブエージェント"
    example("CLAUDE.md", "Claude Code への指示")
    example(".claude/skills/new-screen/SKILL.md", "画面の雛形を作るスキル")
    layout {
        "CLAUDE.md".file()
        ".github" / "copilot-instructions.md".file()
        ".claude" {
            "settings.json".file()
            "agents" / "*.md".file()
            "skills" / "*" {
                "SKILL.md".file()
                "references" / "*.md".file()
            }
        }
    }
}
