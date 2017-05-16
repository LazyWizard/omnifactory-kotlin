@file:JvmName("BlueprintMaster")

package org.lazywizard.omnifactory

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI

enum class BlueprintType { SHIP, WING, WEAPON }

class Blueprint(val id: String, val type: BlueprintType, val displayName: String,
                val daysToAnalyze: Int, val daysToCreate: Int, val limit: Int) {
    fun create(dest: CargoAPI) {
        when (type) {
            BlueprintType.SHIP -> dest.addMothballedShip(FleetMemberType.SHIP, "${id}_Hull", null)
            BlueprintType.WING -> dest.addFighters(id, 1)
            BlueprintType.WEAPON -> dest.addWeapons(id, 1)
            else -> throw RuntimeException("Unsupported blueprint type '$type'")
        }
    }
}

private val blueprints = ArrayList<Blueprint>(90)

fun reloadBlueprints() {
    blueprints.clear()
    val settings = Global.getSettings()
    settings.allVariantIds.filter { it.endsWith("_Hull") }.mapNotNullTo(blueprints) { createBlueprint(settings.getHullSpec(it.dropLast(5))) }
    settings.allFighterWingSpecs.mapNotNullTo(blueprints) { createBlueprint(it) }
    settings.allWeaponSpecs.mapNotNullTo(blueprints) { createBlueprint(it) }
}

private fun createBlueprint(spec: ShipHullSpecAPI): Blueprint {
    println("Ship: ${spec.hullId}")
    return Blueprint(id = spec.hullId, type = BlueprintType.SHIP, displayName = spec.hullName,
            daysToAnalyze = 1, daysToCreate = 1, limit = 1)
}

private fun createBlueprint(spec: FighterWingSpecAPI): Blueprint {
    println("Wing: ${spec.id}")
    return Blueprint(id = spec.id, type = BlueprintType.WING, displayName = spec.variant.displayName,
            daysToAnalyze = 1, daysToCreate = 1, limit = 1)
}

private fun createBlueprint(spec: WeaponSpecAPI): Blueprint {
    println("Weapon: ${spec.weaponId}")
    return Blueprint(id = spec.weaponId, type = BlueprintType.WEAPON, displayName = spec.weaponName,
            daysToAnalyze = 1, daysToCreate = 1, limit = 1)
}