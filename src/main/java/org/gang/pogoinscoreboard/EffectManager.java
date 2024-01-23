package org.gang.pogoinscoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EffectManager {
    private final List<StatusEffect> statusEffects = Arrays.asList(
            new StatusEffect("채굴피로", 3, PotionEffectType.SLOW_DIGGING),
            new StatusEffect("속도감소", 6, PotionEffectType.SLOW),
            new StatusEffect("위더", 2, PotionEffectType.WITHER),
            new StatusEffect("독", 2, PotionEffectType.POISON),
            new StatusEffect("실명", 1, PotionEffectType.BLINDNESS),
            new StatusEffect("어둠", 2, PotionEffectType.DARKNESS),
            new StatusEffect("허기", 1, PotionEffectType.HUNGER),
            new StatusEffect("화염", 1, PotionEffectType.FIRE_RESISTANCE),
            new StatusEffect("재생", 1, PotionEffectType.REGENERATION)
    );

    public void addEffect() {
        try {
            List<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().stream().toList();
            Player p = onlinePlayers.get(0);
            int n = new Random().nextInt(0, statusEffects.size());

            StatusEffect effect = statusEffects.get(n);

            int force = (effect.getMaxLevel() != 1) ?
                    new Random().nextInt(0, effect.getMaxLevel() - 1) :
                    0;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < force + 1; i++) {
                sb.append("I");
            }

            int time = (effect.getPotionType() == PotionEffectType.REGENERATION) ?
                    15 :
                    new Random().nextInt(40, 60);

            int duration = time * 20 + ((p.getActivePotionEffects().stream().filter(e -> e.getType().equals(effect.getPotionType())).findFirst().orElse(null) != null) ?
                    p.getActivePotionEffects().stream().filter(e -> e.getType().equals(effect.getPotionType())).findFirst().get().getDuration() :
                    0);

            PotionEffect potionEffect = new PotionEffect(effect.getPotionType(), duration, force, false, true, true);

            Bukkit.getScheduler().runTask(this, () -> {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.0f);
                p.sendTitle(ChatColor.RED + "디버프 : " + ChatColor.WHITE + effect.getName() + " " + sb.toString(),
                        ChatColor.GRAY + Integer.toString(time), 0, 20, 0);

                if (effect.getPotionType().equals(PotionEffectType.FIRE_RESISTANCE)) {
                    p.setFireTicks(p.getFireTicks() + time * 20);
                } else {
                    p.addPotionEffect(potionEffect);
                }
            });

        } catch (Exception e) {
            // 예외 처리
        }
    }

    public void addEffectTitle() {
        try {
            List<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().stream().toList();
            Player p = onlinePlayers.get(0);
            int n = new Random().nextInt(0, statusEffects.size());

            StatusEffect effect = statusEffects.get(n);

            int force = (effect.getMaxLevel() != 1) ?
                    new Random().nextInt(0, effect.getMaxLevel() - 1) :
                    0;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < force + 1; i++) {
                sb.append("I");
            }

            int time = new Random().nextInt(40, 60);

            Bukkit.getScheduler().runTask(this, () -> {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.0f);
                p.sendTitle(ChatColor.RED + "디버프 : " + ChatColor.WHITE + effect.getName() + " " + sb.toString(),
                        ChatColor.GRAY + Integer.toString(time), 0, 3, 0);
            });

        } catch (Exception e) {
            // 예외 처리
        }
    }
}
public class StatusEffect {
    private final String name;
    private final int maxLevel;
    private final PotionEffectType potionType;

    public StatusEffect(String name, int maxLevel, PotionEffectType potionType) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.potionType = potionType;
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public PotionEffectType getPotionType() {
        return potionType;
    }
}