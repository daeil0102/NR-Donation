package net.teujaem.nrDonation.util;

import net.minecraft.network.chat.Component;

/**
 * NeoForge 1.21.1 용 TextColorFormatter
 * - & 색상 코드 → § 변환
 * - net.minecraft.network.chat.Component 지원
 */
public class TextColorFormatter {

    public static Component toColoredComponent(String message) {
        return Component.literal(toColoredString(message));
    }

    public static String toColoredString(String message) {
        return message.replace("&", "§");
    }

    public static String toUncolored(String message) {
        return message.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }
}
