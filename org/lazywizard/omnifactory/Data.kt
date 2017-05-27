@file:JvmName("BlueprintMaster")

package org.lazywizard.omnifactory

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI

enum class BlueprintType { SHIP, WING, WEAPON }

class Blueprint(val type: BlueprintType, val id: String, val displayName: String,
                val daysToAnalyze: Int, val daysToCreate: Int, val limit: Int) {
    override fun hashCode(): Int = 31 * type.hashCode() + id.hashCode()
    fun create(dest: SubmarketAPI) = create(dest.cargo)
    fun create(dest: CargoAPI) = when (type) {
        BlueprintType.SHIP -> dest.addMothballedShip(FleetMemberType.SHIP, "${id}_Hull", null)
        BlueprintType.WING -> dest.addFighters(id, 1)
        BlueprintType.WEAPON -> dest.addWeapons(id, 1)
        else -> throw RuntimeException("Unsupported blueprint type '$type'")
    }
}

private val blueprints = HashSet<Blueprint>(90)

fun reloadBlueprints() {
    blueprints.clear()
    val settings = Global.getSettings()
    settings.allVariantIds.filter { it.endsWith("_Hull") }.mapNotNullTo(blueprints) { createBlueprint(settings.getHullSpec(it.dropLast(5))) }
    settings.allFighterWingSpecs.mapNotNullTo(blueprints) { createBlueprint(it) }
    settings.allWeaponSpecs.mapNotNullTo(blueprints) { createBlueprint(it) }
}

private fun createBlueprint(spec: ShipHullSpecAPI): Blueprint? = with(spec) {
    if (hullSize == ShipAPI.HullSize.FIGHTER || hints.contains(ShipTypeHints.UNBOARDABLE)) return null

    println("Ship: $hullId")

    val createTime = 1
    return Blueprint(type = BlueprintType.SHIP, id = hullId, displayName = hullName,
            daysToAnalyze = createTime * 2, daysToCreate = createTime, limit = 1)
}

private fun createBlueprint(spec: FighterWingSpecAPI): Blueprint? = with(spec) {
    if (variant.hullSpec.hints.contains(ShipTypeHints.UNBOARDABLE)) return null

    println("Wing: $id")

    val createTime = 1
    return Blueprint(type = BlueprintType.WING, id = id, displayName = variant.displayName,
            daysToAnalyze = createTime * 2, daysToCreate = createTime, limit = 1)
}

private val baseWeaponTime: Map<WeaponSize, Int> = mapOf(WeaponSize.SMALL to 10, WeaponSize.MEDIUM to 20, WeaponSize.LARGE to 40)
private val blockedWeaponTypes = listOf(WeaponType.BUILT_IN, WeaponType.DECORATIVE, WeaponType.SYSTEM,
        WeaponType.STATION_MODULE, WeaponType.LAUNCH_BAY)

private fun createBlueprint(spec: WeaponSpecAPI): Blueprint? = with(spec) {
    if (type in blockedWeaponTypes) return null

    println("Weapon: $weaponId")

    val createTime = baseWeaponTime.get(size) ?: 20
    return Blueprint(type = BlueprintType.WEAPON, id = weaponId, displayName = weaponName,
            daysToAnalyze = createTime * 2, daysToCreate = createTime, limit = 1)
}

fun getBlueprint(spec: ShipHullSpecAPI): Blueprint? = blueprints.find { it.type == BlueprintType.SHIP && it.id == spec.hullId }
fun getBlueprint(spec: FighterWingSpecAPI): Blueprint? = blueprints.find { it.type == BlueprintType.WING && it.id == spec.id }
fun getBlueprint(spec: WeaponSpecAPI): Blueprint? = blueprints.find { it.type == BlueprintType.WEAPON && it.id == spec.weaponId }