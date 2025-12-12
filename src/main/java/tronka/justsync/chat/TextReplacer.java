package tronka.justsync.chat;

import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class TextReplacer {

    private final ParserBuilder builder;
    private final Map<String, TextNode> placeholders;

    private TextReplacer(TagLikeParser.Format format) {
        this.placeholders = new HashMap<>();
        this.builder = NodeParser.builder().globalPlaceholders().placeholders(format, this.placeholders::get)
            .simplifiedTextFormat();
    }

    public static TextReplacer create() {
        return new TextReplacer(TagLikeParser.PLACEHOLDER);
    }

    public static TextReplacer create(TagLikeParser.Format format) {
        return new TextReplacer(format);
    }

    public TextReplacer replace(String key, String value) {
        return this.replace(key, TextNode.of(value));
    }

    public TextReplacer replace(String key, TextNode node) {
        this.placeholders.put(key, node);
        return this;
    }

    private NodeParser getParser() {
        return this.builder.build();
    }

    public Component apply(String text) {
        return this.getParser().parseNode(text).toText();
    }

    public TextNode applyNode(String text) {
        return this.getParser().parseNode(text);
    }
}
