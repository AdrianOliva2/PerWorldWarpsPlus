package net.serveminecraft.minecrafteros.perworldwarpsplus.utils

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration

class MessagesUtil {

    companion object{
        fun getFullStringFromConfig(configFile: FileConfiguration, stringPath: String, replaces: Map<String, String>): String {
            val configMessage: String? = configFile.getString(stringPath, "")
            if (configMessage != null){
                var replacedMessage: String = configMessage.toString()
                for (replace: String in replaces.keys) {
                    replacedMessage = replacedMessage.replace(replace, replaces.getValue(replace))
                }
                return replacedMessage
            }
            return ""
        }

        fun getFullStringListFromConfig(configFile: FileConfiguration, stringPath: String, replaces: Map<String, String>): MutableList<String> {
            val configMessages: MutableList<String> = configFile.getStringList(stringPath)
            for (i in 0..<configMessages.size) {
                for (replace: String in replaces.keys) {
                    configMessages[i] = configMessages.elementAt(i).replace(replace, replaces.getValue(replace))
                }
            }
            return configMessages
        }

        fun colorizeList(list: MutableList<String>?): MutableList<String>? {
            val colorizedMessages: MutableList<String>? = list?.toMutableList()
            if (colorizedMessages != null) {
                for (i in 0..<colorizedMessages.size) {
                    if (colorizedMessages.elementAtOrNull(i) != null) {
                        colorizedMessages[i] = LegacyComponentSerializer.legacyAmpersand().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(colorizedMessages.elementAt(i)))
                    }
                }
            }
            return colorizedMessages
        }
    }

}