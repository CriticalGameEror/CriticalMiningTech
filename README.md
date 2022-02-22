# CriticalMiningTech (1.16 plugin)

This plugin implements custom mining tech into Minecraft. This allows you to customise breaking hardness for blocks by manipulating packets.

### Permissions (all below are given only to operators):

`criticalmining.*`: Gives all permissions under criticalmining

`criticalmining.blockhardness`: Gives the permission to use the blockhardness command

### Commands:

`/blockhardness`: Allows you to set or remove block hardness of a targeted block material, that being the block you are currently looking at (see https://minecraft.fandom.com/wiki/Breaking for info on what block hardness is). Block material is the type of block such as stone or oak logs. Structure: `/blockhardness [set|remove] <blockhardness (this field is only needed if you selected set)>`

### Dependencies:

Please ensure you have installed ProtocolLib on your spigot server (https://www.spigotmc.org/resources/protocollib.1997/)

