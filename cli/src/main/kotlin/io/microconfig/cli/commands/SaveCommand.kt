package io.microconfig.cli.commands

import com.fasterxml.jackson.databind.node.ArrayNode
import io.microconfig.cli.FileUtil
import io.microconfig.server.common.json
import java.io.File

class SaveCommand(args: Array<String>) : Command(args) {

    override fun execute(): Int {
        val component = component(helpMessage())
        val request = request(component)
        val json = send(request).json()
        val outDir = FileUtil.getOrCreateDir(flags.dir() ?: ".")
        saveFiles(outDir, json as ArrayNode)
        return 0
    }

    private fun saveFiles(outDir: File, nodes: ArrayNode) {
        for (node in nodes) {
            val filename = node["fileName"].asText()
            val content = node["content"].asText()
            val file = File(outDir, filename)
            FileUtil.writeFile(file, content)
        }
    }

    private fun helpMessage(): String {
        return """
            Usage microctl save [component] [flags]
            Generates configuration for component and saves it to disk
            Flags: 
              -e, --env  [name]: config environment
              -t, --type [name]: config type, all types by default
              -d, --dir  [path]: output directory, current dir by default
              -s, --set  [foo=bar]: override values for placeholders and vars
            """.trimIndent()
    }
}