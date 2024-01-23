package org.gang.pogoinscoreboard

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerArgument
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard
import kotlin.math.ceil
import kotlin.random.Random


class PogoinScoreboard : JavaPlugin(),Listener {
    data class StatusEffect(val name: String, val maxLevel: Int, val potionType: PotionEffectType)

    companion object{
        var stack = 0
    }
    private var economy : Economy? = null
    private var wait = 0
    private var wait2 = 0
    private lateinit var board: Scoreboard
    private lateinit var obj : Objective
    private lateinit var score: Score
    private var taskId: Int = -1 // 스케줄러 작업 ID를 저장하는 변수
    private val statusEffects = mutableListOf(
        StatusEffect("채굴피로", 3, PotionEffectType.SLOW_DIGGING),
        StatusEffect("속도감소", 6, PotionEffectType.SLOW),
        StatusEffect("위더", 2, PotionEffectType.WITHER),
        StatusEffect("독", 2, PotionEffectType.POISON),
        StatusEffect("실명", 1, PotionEffectType.BLINDNESS),
        StatusEffect("어둠", 2, PotionEffectType.DARKNESS),
        StatusEffect("허기", 1, PotionEffectType.HUNGER),
        StatusEffect("화염", 1, PotionEffectType.FIRE_RESISTANCE),
        StatusEffect("재생", 1, PotionEffectType.REGENERATION)
    )
    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).verboseOutput(true)) // Load with verbose output

        commandAPICommand("addscoreboarddummy") {
            anyExecutor { _, args -> // Command can be executed by anyone and anything (such as entities, the console, etc.)
                wait2+=89
            }
        }
        commandAPICommand("디버프") {
            anyExecutor { _, args -> // Command can be executed by anyone and anything (such as entities, the console, etc.)
                wait+=89
            }
        }
    }
    public fun addDebuff(){
        wait+=89
    }
    public fun increaseWool(){
        wait2+=89
    }
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this)
        val essentialsPlugin = server.pluginManager.getPlugin("Essentials")
        logger.info("enable scoreboard")
        taskId = this.server.scheduler.scheduleAsyncRepeatingTask(this, {
            if (essentialsPlugin != null && essentialsPlugin.isEnabled) {
                if (!setupEconomy() ) {
                    logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                    Bukkit.getScheduler().cancelTask(taskId)
                }
            }
        }, 0L, 1L)
        this.server.scheduler.scheduleAsyncRepeatingTask(this, {
            stack = ceil(wait.toDouble()/90.0).toInt()+ceil(wait2.toDouble()/90.0).toInt()
            if (wait != 0 ){
                val w = wait%90
                if (w in 21..79 && w%3.0 == 0.0)addEffectTitle()
                else if (w == 20) addEffect()


                wait--
            }else if (wait2 !=0){
                wait2--
            }

        }, 0L, 1L)
        Bukkit.getOnlinePlayers().forEach {
            scboard(it)
            this.server.scheduler.scheduleAsyncRepeatingTask(this, {
                update(it)
            }, 0L, 4)
        }

    }
    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(
            Economy::class.java
        ) ?: return false
        economy = rsp.provider
        return economy != null
    }
    override fun onDisable() {
    }


    @EventHandler
    fun onJoin(e : PlayerJoinEvent){
        scboard(e.player)
        this.server.scheduler.scheduleAsyncRepeatingTask(this, {
            update(e.player)
        }, 0L, 4)

    }
    fun scboard(p : Player) {
        val sm = Bukkit.getScoreboardManager()!!
        val list = listOf(
            " ",
            "  ",
            "${ChatColor.WHITE}[ ${ChatColor.DARK_PURPLE}양털 시세 안내 ${ChatColor.WHITE}]",
            "${ChatColor.WHITE}하얀색 ${ChatColor.WHITE}         : 1개",
            "${ChatColor.WHITE}회백색,회색,흑색 ${ChatColor.WHITE}: 3개",
            "${ChatColor.WHITE}갈색 ${ChatColor.WHITE}           : 5개",
            "${ChatColor.LIGHT_PURPLE}핑크색 ${ChatColor.WHITE}         : 20개",
            "   ",
            "     ",
        )

        var n = list.size*3
        board = sm.newScoreboard
        obj= board.registerNewObjective("pogoin_scoreboard", "dummy")
        obj.displayName = "${ChatColor.WHITE}[${ChatColor.GOLD}포고인 서버${ChatColor.WHITE}]"
        obj.displaySlot = DisplaySlot.SIDEBAR

        list.forEach {
            score = obj.getScore(it)
            score.score = n
            n-=3
        }
        val money = board.registerNewTeam("money")
        money.prefix = "소지금 : ${ChatColor.GREEN}${economy?.getBalance(p)?.toInt()}${ChatColor.WHITE}원"
        money.addEntry("${ChatColor.WHITE}")
        val coal = board.registerNewTeam("coal")
        coal.prefix = "${ChatColor.WHITE}남은 룰렛 : ${ChatColor.LIGHT_PURPLE}${PogoinScoreboard.stack}${ChatColor.WHITE}개"
        coal.addEntry("${ChatColor.RED}")
        val firesco = board.registerNewTeam("fire")
        firesco.prefix = "${ChatColor.RED}화염 : ${ChatColor.WHITE}${p.fireTicks}${ChatColor.WHITE}초"
        firesco.addEntry("${ChatColor.GREEN}")
        obj.getScore("${ChatColor.WHITE}").score = 26

        obj.getScore("${ChatColor.RED}").score = 4
        obj.getScore("${ChatColor.GREEN}").score = 0


        p.scoreboard = board
    }
    fun update(p: Player){


        board = p.scoreboard
        p.isVisualFire = false
        val money = board.getTeam("money")
        money?.prefix = "소지금 : ${ChatColor.GREEN}${economy?.getBalance(p)?.toInt()}${ChatColor.WHITE}원"
        val coal = board.getTeam("coal")
        coal?.prefix = "${ChatColor.WHITE}남은 룰렛 : ${ChatColor.LIGHT_PURPLE}${PogoinScoreboard.stack}${ChatColor.WHITE}개"
        val firesco = board.getTeam("fire")
        if (p.fireTicks/20 <= 0){
            firesco?.prefix = ""
        }else{
            firesco?.prefix = "${ChatColor.RED}화염 : ${ChatColor.WHITE}${p.fireTicks/20}${ChatColor.WHITE}초"
        }
        p.scoreboard = board

    }
    fun addEffect(){
        try {
            val p = Bukkit.getOnlinePlayers().toList()[0]
            val n = Random.nextInt(0,statusEffects.size)

            val effect = statusEffects[n]

            val force = if (effect.maxLevel != 1){
                Random.nextInt(0,effect.maxLevel-1)
            }else{
                0
            }

            val sb = StringBuilder()
            repeat(force+1){
                sb.append("I")
            }

            val time = if (effect.potionType == PotionEffectType.REGENERATION){
                15
            }else{
                Random.nextInt(40,60)
            }

            val potionEffect = PotionEffect(effect.potionType, time*20 + (p.activePotionEffects.find { it.type == effect.potionType }?.duration?:0),force,false,true,true)

            Bukkit.getScheduler().runTask(this, Runnable {
                p.playSound(p,Sound.ENTITY_PLAYER_LEVELUP,0.9f,1.0f)
                p.sendTitle("${ChatColor.RED}디버프 : ${ChatColor.WHITE}${effect.name} $sb",
                    "${ChatColor.GRAY}$time", 0, 20, 0)
                if (effect.potionType == PotionEffectType.FIRE_RESISTANCE){
                    p.fireTicks+=time*20
                }else{
                    p.addPotionEffect(potionEffect)
                }
            })

        }catch (e:Exception){

        }

    }
    fun addEffectTitle(){
        try {
            val p = Bukkit.getOnlinePlayers().toList()[0]
            val n = Random.nextInt(0,statusEffects.size)

            val effect = statusEffects[n]

            val force = if (effect.maxLevel != 1){
                Random.nextInt(0,effect.maxLevel-1)
            }else{
                0
            }

            val sb = StringBuilder()
            repeat(force+1){
                sb.append("I")
            }

            val time = Random.nextInt(40,60)


            Bukkit.getScheduler().runTask(this, Runnable {
                p.playSound(p,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,0.9f,1.0f)
                p.sendTitle("${ChatColor.RED}디버프 : ${ChatColor.WHITE}${effect.name} $sb",
                    "${ChatColor.GRAY}$time", 0, 3, 0)

            })

        }catch (e:Exception){

        }

    }
}