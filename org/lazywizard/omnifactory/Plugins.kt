package org.lazywizard.omnifactory

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.ext.json.*

class ModPlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        println("\n\n\n   - - - Starting! - - -\n\n\n")

        reloadBlueprints()

        println("\n\n\n   - - - Successful! - - -\n\n\n")
    }
}