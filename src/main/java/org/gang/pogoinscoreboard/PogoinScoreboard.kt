package org.gang.pogoinscoreboard

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.ServerCommandEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard
import kotlin.math.ceil


class PogoinScoreboard : JavaPlugin(),Listener {
    companion object{
        var stack = 0
    }
    private var economy : Economy? = null
    private var wait = 0

    private lateinit var board: Scoreboard
    private lateinit var obj : Objective
    private lateinit var score: Score
    private var taskId: Int = -1 // 스케줄러 작업 ID를 저장하는 변수

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
            stack = ceil(wait.toDouble()/90.0).toInt()
            if (wait != 0 ){
                wait--
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
    private fun cmd(){
        wait+=90

    }
    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        // 감지하려는 명령어
        var targetCommand = "/양털증가"

        if (event.message.equals(targetCommand, ignoreCase = true)) {
            cmd()
        }
        targetCommand = "/디버프"

        if (event.message.equals(targetCommand, ignoreCase = true)) {
            cmd()
        }
    }
    @EventHandler
    fun onServerCommand(event: ServerCommandEvent) {
        // 감지하려는 명령어
        var targetCommand = "양털증가"

        if (event.command.equals(targetCommand, ignoreCase = true)) {
            cmd()
        }
        targetCommand = "디버프"

        if (event.command.equals(targetCommand, ignoreCase = true)) {
            cmd()
        }
        targetCommand = "/양털증가"

        if (event.command.equals(targetCommand, ignoreCase = true)) {
            cmd()
        }
        targetCommand = "/디버프"

        if (event.command.equals(targetCommand, ignoreCase = true)) {
            cmd()
        }
    }
    @EventHandler
    fun onJoin(e : PlayerJoinEvent){
        scboard(e.player)
        this.server.scheduler.scheduleAsyncRepeatingTask(this, {
            update(e.player)
        }, 0L, 4)
    }

    private fun scboard(p : Player) {
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
        coal.prefix = "${ChatColor.WHITE}남은 룰렛 : ${ChatColor.LIGHT_PURPLE}${stack}${ChatColor.WHITE}개"
        coal.addEntry("${ChatColor.RED}")
        obj.getScore("${ChatColor.WHITE}").score = 22

        obj.getScore("${ChatColor.RED}").score = 0


        p.scoreboard = board
    }
    private fun update(p: Player){


        board = p.scoreboard
        val money = board.getTeam("money")
        money?.prefix = "소지금 : ${ChatColor.GREEN}${economy?.getBalance(p)?.toInt()}${ChatColor.WHITE}원"
        val coal = board.getTeam("coal")
        coal?.prefix = "${ChatColor.WHITE}남은 룰렛 : ${ChatColor.LIGHT_PURPLE}${stack}${ChatColor.WHITE}개"
        p.scoreboard = board
    }
}