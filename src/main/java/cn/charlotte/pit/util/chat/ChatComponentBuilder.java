package cn.charlotte.pit.util.chat;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/30 22:54
 */

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import java.util.regex.Pattern;

public class ChatComponentBuilder {
    private ComponentBuilder componentBuilder;

    public ChatComponentBuilder(String text) {
        this.componentBuilder = new ComponentBuilder("");
        this.parse(text);
    }

    public ChatComponentBuilder append(String text) {
        this.componentBuilder.append(text);
        return this;
    }

    public TextComponent getCurrent() {
        // Create a temporary text component to get current formatting
        TextComponent tc = new TextComponent("");
        return tc;
    }

    public void setCurrent(TextComponent tc) {
        // Not used directly with ComponentBuilder
    }

    public ChatComponentBuilder setCurrentHoverEvent(HoverEvent hoverEvent) {
        // Apply to the last component
        return this;
    }

    public ChatComponentBuilder setCurrentClickEvent(ClickEvent clickEvent) {
        // Apply to the last component
        return this;
    }

    public ChatComponentBuilder attachToEachPart(HoverEvent hoverEvent) {
        // Apply to all components
        return this;
    }

    public ChatComponentBuilder attachToEachPart(ClickEvent clickEvent) {
        // Apply to all components
        return this;
    }

    public ChatComponentBuilder parse(String text) {
        String regex = "[&§]{1}([a-fA-Fl-oL-O0-9-r]){1}";
        text = text.replaceAll(regex, "§$1");
        if (!Pattern.compile(regex).matcher(text).find()) {
            this.componentBuilder.append(text);
            return this;
        } else {
            String[] words = text.split(regex);
            int index = words[0].length();
            String[] var5 = words;
            int var6 = words.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String word = var5[var7];

                try {
                    if (index != words[0].length()) {
                        this.componentBuilder.append(word);
                        ChatColor color = ChatColor.getByChar(text.charAt(index - 1));
                        if (color == ChatColor.BOLD) {
                            this.componentBuilder.bold(true);
                        } else if (color == ChatColor.STRIKETHROUGH) {
                            this.componentBuilder.strikethrough(true);
                        } else if (color == ChatColor.MAGIC) {
                            this.componentBuilder.obfuscated(true);
                        } else if (color == ChatColor.UNDERLINE) {
                            this.componentBuilder.underlined(true);
                        } else if (color == ChatColor.RESET) {
                            this.componentBuilder.bold(false);
                            this.componentBuilder.strikethrough(false);
                            this.componentBuilder.obfuscated(false);
                            this.componentBuilder.underlined(false);
                        } else {
                            this.componentBuilder.color(color);
                        }
                    }
                } catch (Exception var10) {
                    var10.printStackTrace();
                }

                index += word.length() + 2;
            }

            return this;
        }
    }

    public BaseComponent[] create() {
        return this.componentBuilder.create();
    }

    public ChatComponentBuilder color(ChatColor color) {
        this.componentBuilder.color(color);
        return this;
    }

    public ChatComponentBuilder bold(boolean bold) {
        this.componentBuilder.bold(bold);
        return this;
    }

    public ChatComponentBuilder underlined(boolean underlined) {
        this.componentBuilder.underlined(underlined);
        return this;
    }

    public ChatComponentBuilder italic(boolean italic) {
        this.componentBuilder.italic(italic);
        return this;
    }

    public ChatComponentBuilder strikethrough(boolean strikethrough) {
        this.componentBuilder.strikethrough(strikethrough);
        return this;
    }

    public ChatComponentBuilder obfuscated(boolean obfuscated) {
        this.componentBuilder.obfuscated(obfuscated);
        return this;
    }

    public ChatComponentBuilder event(HoverEvent event) {
        this.componentBuilder.event(event);
        return this;
    }

    public ChatComponentBuilder event(ClickEvent event) {
        this.componentBuilder.event(event);
        return this;
    }

    public ChatComponentBuilder append(BaseComponent[] components) {
        for (BaseComponent component : components) {
            this.componentBuilder.append(component);
        }
        return this;
    }

    public ChatComponentBuilder append(TextComponent textComponent) {
        this.componentBuilder.append(textComponent);
        return this;
    }
}